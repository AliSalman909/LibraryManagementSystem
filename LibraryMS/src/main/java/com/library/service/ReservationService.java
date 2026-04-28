package com.library.service;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.library.entity.Book;
import com.library.entity.BookCopy;
import com.library.entity.BorrowRecord;
import com.library.entity.BorrowRequest;
import com.library.entity.Librarian;
import com.library.entity.Reservation;
import com.library.entity.Student;
import com.library.entity.enums.BorrowRequestStatus;
import com.library.entity.enums.ReservationStatus;
import com.library.exception.BusinessRuleException;
import com.library.repository.BookCopyRepository;
import com.library.repository.BookRepository;
import com.library.repository.BorrowRequestRepository;
import com.library.repository.BorrowRecordRepository;
import com.library.repository.LibrarianRepository;
import com.library.repository.ReservationRepository;
import com.library.repository.StudentRepository;
import java.time.LocalDate;

/**
 * Manages the book reservation / waitlist system.
 * <p>Workflow: Student reserves an unavailable book → PENDING in FIFO queue →
 * When a copy is returned, the next PENDING moves to READY with a pickup window →
 * Librarian marks FULFILLED (after issuing) or the reservation EXPIRES / is CANCELLED.</p>
 */
@Service
public class ReservationService {

    private static final List<ReservationStatus> ACTIVE_STATUSES =
            List.of(ReservationStatus.PENDING, ReservationStatus.READY);
    private static final List<Integer> SUPPORTED_DURATIONS = List.of(7, 14, 21, 28);
    private static final int DEFAULT_DURATION_DAYS = 14;

    @Value("${app.reservation.pickup-window-hours:48}")
    private int pickupWindowHours;

    private final ReservationRepository reservationRepository;
    private final BookRepository bookRepository;
    private final StudentRepository studentRepository;
    private final BorrowRecordRepository borrowRecordRepository;
    private final BookCopyRepository bookCopyRepository;
    private final BorrowRequestRepository borrowRequestRepository;
    private final LibrarianRepository librarianRepository;

    public ReservationService(
            ReservationRepository reservationRepository,
            BookRepository bookRepository,
            StudentRepository studentRepository,
            BorrowRecordRepository borrowRecordRepository,
            BookCopyRepository bookCopyRepository,
            BorrowRequestRepository borrowRequestRepository,
            LibrarianRepository librarianRepository) {
        this.reservationRepository = reservationRepository;
        this.bookRepository = bookRepository;
        this.studentRepository = studentRepository;
        this.borrowRecordRepository = borrowRecordRepository;
        this.bookCopyRepository = bookCopyRepository;
        this.borrowRequestRepository = borrowRequestRepository;
        this.librarianRepository = librarianRepository;
    }

    // -----------------------------------------------------------------------
    // Student actions
    // -----------------------------------------------------------------------

    /**
     * Create a reservation for a book when it has no available copies.
     */
    @Transactional
    public Reservation createReservation(String studentUserId, String bookId, Integer durationDays) {
        Student student = studentRepository.findByUserIdWithUser(studentUserId)
                .orElseThrow(() -> new BusinessRuleException("Student account not found."));

        @SuppressWarnings("null")
        Book book = bookRepository.findById(bookId)
                .orElseThrow(() -> new BusinessRuleException("Book not found."));

        // Must be unavailable to reserve
        if (book.getAvailableCopies() > 0) {
            throw new BusinessRuleException(
                    "This book is currently available. You can borrow it directly instead of reserving.");
        }

        // No duplicate active reservation
        if (reservationRepository.existsByStudentAndBookAndStatusIn(studentUserId, bookId, ACTIVE_STATUSES)) {
            throw new BusinessRuleException("You already have an active reservation for this book.");
        }

        // No active loan for this book
        if (borrowRecordRepository.existsByStudentUserIdAndBookBookIdAndReturnedAtIsNull(studentUserId, bookId)) {
            throw new BusinessRuleException("You already have this book on loan.");
        }

        int nextPosition = reservationRepository.findMaxQueuePositionByBookId(bookId) + 1;

        Reservation reservation = new Reservation();
        reservation.setReservationId(UUID.randomUUID().toString());
        reservation.setStudent(student);
        reservation.setBook(book);
        reservation.setStatus(ReservationStatus.PENDING);
        reservation.setQueuePosition(nextPosition);
        reservation.setRequestedDurationDays(normalizeRequestedDuration(durationDays, book.getMaxBorrowDays()));
        reservation.setCreatedAt(Instant.now());
        return reservationRepository.save(reservation);
    }

    /**
     * Student cancels their own reservation (only PENDING or READY).
     */
    @Transactional
    public void cancelByStudent(String reservationId, String studentUserId) {
        @SuppressWarnings("null")
        Reservation reservation = reservationRepository.findById(reservationId)
                .orElseThrow(() -> new BusinessRuleException("Reservation not found."));
        if (!reservation.getStudent().getUserId().equals(studentUserId)) {
            throw new BusinessRuleException("You can only cancel your own reservations.");
        }
        if (reservation.getStatus() != ReservationStatus.PENDING &&
            reservation.getStatus() != ReservationStatus.READY) {
            throw new BusinessRuleException("Only pending or ready reservations can be cancelled.");
        }
        reservation.setStatus(ReservationStatus.CANCELLED);
        reservationRepository.save(reservation);
    }

    // -----------------------------------------------------------------------
    // System actions (called after a book return)
    // -----------------------------------------------------------------------

    /**
     * Promote the next PENDING reservation to READY with a pickup window.
     * Called by ReturnService after a successful return.
     * If no pending reservations exist, this is a no-op.
     */
    @Transactional
    public void notifyNextInQueue(String bookId) {
        reconcileReadyForBookAvailability(bookId);
    }

    /**
     * If undo return is blocked because unrelated READY holds exist.
     * READY rows created by {@link #notifyNextInQueue} for this return fall inside a time window around
     * {@code returnedAt}; those can be reverted on undo.
     */
    @Transactional(readOnly = true)
    public boolean hasUndoBlockingReadyReservation(String bookId, Instant returnedAt) {
        Instant from = returnedAt.minusSeconds(120);
        Instant to = returnedAt.plus(15, ChronoUnit.MINUTES);
        return reservationRepository.existsReadyOutsideNotifiedWindow(bookId, from, to);
    }

    /**
     * Demotes the READY reservation that was most likely promoted when this copy was returned,
     * so undo return can safely restore the previous loan state.
     */
    @Transactional
    public void revertReadyPromotionFromReturn(String bookId, Instant returnedAt) {
        Instant from = returnedAt.minusSeconds(120);
        Instant to = returnedAt.plus(15, ChronoUnit.MINUTES);
        List<Reservation> candidates = reservationRepository.findReadyNotifiedInWindowForUpdate(bookId, from, to);
        if (candidates.isEmpty()) {
            return;
        }
        Reservation r = candidates.get(0);
        r.setStatus(ReservationStatus.PENDING);
        r.setNotifiedAt(null);
        r.setExpiresAt(null);
        reservationRepository.save(r);
    }

    // -----------------------------------------------------------------------
    // Librarian actions
    // -----------------------------------------------------------------------

    @Transactional
    public void markFulfilled(String reservationId, String librarianUserId) {
        @SuppressWarnings("null")
        Reservation reservation = reservationRepository.findById(reservationId)
                .orElseThrow(() -> new BusinessRuleException("Reservation not found."));
        if (reservation.getStatus() != ReservationStatus.READY) {
            throw new BusinessRuleException("Only READY reservations can be marked as fulfilled.");
        }
        if (borrowRecordRepository.existsByStudentUserIdAndBookBookIdAndReturnedAtIsNull(
                reservation.getStudent().getUserId(), reservation.getBook().getBookId())) {
            throw new BusinessRuleException("This student already has an active loan for this book.");
        }
        Librarian librarian = librarianRepository.findByUserIdWithUser(librarianUserId)
                .orElseThrow(() -> new BusinessRuleException("Librarian account not found."));
        // Guard grant action: reservation can only be fulfilled if inventory still has a free copy.
        int updated = bookRepository.decrementAvailableCopiesIfAvailable(
                reservation.getBook().getBookId(), Instant.now());
        if (updated == 0) {
            // Inventory changed before this action (e.g., copy was loaned/allocated elsewhere).
            // Move back to queue so reservation is not left in READY while unavailable.
            reservation.setStatus(ReservationStatus.PENDING);
            reservation.setNotifiedAt(null);
            reservation.setExpiresAt(null);
            reservationRepository.save(reservation);
            throw new BusinessRuleException(
                    "This book is currently on loan, so the reservation was moved back to pending.");
        }
        List<BookCopy> lockedAvailableCopies =
                bookCopyRepository.findAvailableCopiesForUpdateOrderByCopyNumberAsc(reservation.getBook().getBookId());
        BookCopy availableCopy = lockedAvailableCopies.stream()
                .findFirst()
                .orElseThrow(() -> new BusinessRuleException("No available copy found for this book."));
        availableCopy.setAvailable(false);
        bookCopyRepository.save(availableCopy);

        int maxBorrowDays = reservation.getBook().getMaxBorrowDays();
        int requestedDays = reservation.getRequestedDurationDays();
        int durationDays = requestedDays > 0 ? Math.min(requestedDays, maxBorrowDays) : Math.min(DEFAULT_DURATION_DAYS, maxBorrowDays);
        LocalDate dueDate = LocalDate.now().plusDays(durationDays);

        BorrowRequest autoRequest = new BorrowRequest();
        autoRequest.setRequestId(UUID.randomUUID().toString());
        autoRequest.setBook(reservation.getBook());
        autoRequest.setStudent(reservation.getStudent());
        autoRequest.setStatus(BorrowRequestStatus.APPROVED);
        autoRequest.setRequestedAt(reservation.getCreatedAt() != null ? reservation.getCreatedAt() : Instant.now());
        autoRequest.setReviewedAt(Instant.now());
        autoRequest.setDueDate(dueDate);
        autoRequest.setRequestedDurationDays(durationDays);
        autoRequest.setProcessedBy(librarian);
        borrowRequestRepository.save(autoRequest);

        BorrowRecord record = new BorrowRecord();
        record.setRecordId(UUID.randomUUID().toString());
        record.setBorrowRequest(autoRequest);
        record.setBook(reservation.getBook());
        record.setCopy(availableCopy);
        record.setStudent(reservation.getStudent());
        record.setIssuedBy(librarian);
        record.setIssuedAt(Instant.now());
        record.setDueDate(dueDate);
        borrowRecordRepository.save(record);

        reservation.setStatus(ReservationStatus.FULFILLED);
        reservation.setFulfilledAt(Instant.now());
        reservationRepository.save(reservation);
    }

    @Transactional
    public void cancelByLibrarian(String reservationId) {
        @SuppressWarnings("null")
        Reservation reservation = reservationRepository.findById(reservationId)
                .orElseThrow(() -> new BusinessRuleException("Reservation not found."));
        if (reservation.getStatus() == ReservationStatus.FULFILLED ||
            reservation.getStatus() == ReservationStatus.EXPIRED) {
            throw new BusinessRuleException("Cannot cancel a reservation that is already " +
                    reservation.getStatus().name().toLowerCase() + ".");
        }
        reservation.setStatus(ReservationStatus.CANCELLED);
        reservationRepository.save(reservation);
    }

    /**
     * Expire all READY reservations whose pickup window has passed.
     * Can be called from a scheduled job or manually.
     */
    @SuppressWarnings("null")
    @Transactional
    public int expireOverdueReservations() {
        List<Reservation> expired = reservationRepository.findExpiredReady(Instant.now());
        for (Reservation r : expired) {
            r.setStatus(ReservationStatus.EXPIRED);
        }
        reservationRepository.saveAll(expired);
        return expired.size();
    }

    // -----------------------------------------------------------------------
    // Read queries
    // -----------------------------------------------------------------------

    @Transactional
    public List<Reservation> listForStudent(String studentUserId) {
        reconcileAllActiveReservationBooks();
        return reservationRepository.findAllByStudentWithBook(studentUserId);
    }

    @Transactional
    public List<Reservation> listAll() {
        reconcileAllActiveReservationBooks();
        return reservationRepository.findAllWithDetails();
    }

    @Transactional
    public List<Reservation> listReady() {
        reconcileAllActiveReservationBooks();
        return reservationRepository.findAllReadyWithDetails();
    }

    /**
     * Check if a book has active reservations (used by renewal blocker).
     */
    @Transactional(readOnly = true)
    public boolean hasActiveReservationsForBook(String bookId) {
        return reservationRepository.countByBookAndStatusIn(bookId, ACTIVE_STATUSES) > 0;
    }

    @Transactional
    public void reconcileReadyForBookAvailability(String bookId) {
        Book book = bookRepository.findByIdForUpdate(bookId)
                .orElseThrow(() -> new BusinessRuleException("Book not found."));
        List<Reservation> readyReservations = reservationRepository.findReadyByBookForUpdate(bookId);
        if (book.getAvailableCopies() <= 0) {
            if (!readyReservations.isEmpty()) {
                for (Reservation reservation : readyReservations) {
                    reservation.setStatus(ReservationStatus.PENDING);
                    reservation.setNotifiedAt(null);
                    reservation.setExpiresAt(null);
                }
                reservationRepository.saveAll(readyReservations);
            }
            return;
        }
        if (!readyReservations.isEmpty()) {
            return;
        }
        List<Reservation> pending = reservationRepository.findPendingByBookOrderByPosition(bookId);
        if (pending.isEmpty()) {
            return;
        }
        Reservation next = pending.get(0);
        Instant now = Instant.now();
        next.setStatus(ReservationStatus.READY);
        next.setNotifiedAt(now);
        next.setExpiresAt(now.plus(pickupWindowHours, ChronoUnit.HOURS));
        reservationRepository.save(next);
    }

    private void reconcileAllActiveReservationBooks() {
        List<String> bookIds = reservationRepository.findBookIdsWithActiveReservations();
        for (String bookId : bookIds) {
            reconcileReadyForBookAvailability(bookId);
        }
    }

    private int normalizeRequestedDuration(Integer durationDays, int maxBorrowDays) {
        int selectedDays = durationDays == null ? DEFAULT_DURATION_DAYS : durationDays;
        if (!SUPPORTED_DURATIONS.contains(selectedDays)) {
            throw new BusinessRuleException("Reservation duration must be one of: 7, 14, 21, or 28 days.");
        }
        if (selectedDays > maxBorrowDays) {
            throw new BusinessRuleException(
                    "This book can be granted for up to "
                            + maxBorrowDays
                            + " days. Please choose a smaller duration.");
        }
        return selectedDays;
    }
}
