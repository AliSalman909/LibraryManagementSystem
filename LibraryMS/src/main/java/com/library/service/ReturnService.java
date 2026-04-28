package com.library.service;

import com.library.entity.BorrowRecord;
import com.library.entity.Fine;
import com.library.entity.enums.FineStatus;
import com.library.exception.BusinessRuleException;
import com.library.repository.BookRepository;
import com.library.repository.BorrowRecordRepository;
import com.library.repository.FineRepository;
import com.library.repository.LibrarianRepository;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Handles the book-return workflow atomically:
 *   1. Lock the BorrowRecord row (pessimistic write) to prevent race conditions.
 *   2. Mark returnedAt = now.
 *   3. Mark BookCopy.available = true.
 *   4. Increment Book.availableCopies via bulk update.
 *   5. If returned late, create a Fine row (UNPAID).
 */
@Service
public class ReturnService {
    private static final int MAX_ACTIVE_LOANS = 3;

    private final BorrowRecordRepository borrowRecordRepository;
    private final BookRepository bookRepository;
    private final FineRepository fineRepository;
    private final LibrarianRepository librarianRepository;
    private final ReservationService reservationService;

    public ReturnService(
            BorrowRecordRepository borrowRecordRepository,
            BookRepository bookRepository,
            FineRepository fineRepository,
            LibrarianRepository librarianRepository,
            ReservationService reservationService) {
        this.borrowRecordRepository = borrowRecordRepository;
        this.bookRepository = bookRepository;
        this.fineRepository = fineRepository;
        this.librarianRepository = librarianRepository;
        this.reservationService = reservationService;
    }

    /**
     * Returns a borrowed book. If overdue, a Fine is created and returned.
     * Returns {@code null} if the book was returned on time (no fine).
     *
     * @param recordId        the BorrowRecord to close
     * @param librarianUserId the librarian performing the return
     * @return the persisted Fine, or null if no fine was generated
     * @throws BusinessRuleException if already returned or record/librarian not found
     */
    @Transactional
    public Fine returnBook(String recordId, String librarianUserId) {
        // 1. Lock the row – prevents double-returns under concurrent requests
        BorrowRecord record = borrowRecordRepository.findByIdForUpdate(recordId)
                .orElseThrow(() -> new BusinessRuleException("Borrow record not found."));

        if (record.getReturnedAt() != null) {
            throw new BusinessRuleException("This book has already been returned.");
        }

        Instant now = Instant.now();
        LocalDate today = now.atZone(ZoneOffset.UTC).toLocalDate();

        // 2. Mark the record as returned
        record.setReturnedAt(now);
        borrowRecordRepository.save(record);

        // 3. Mark the physical copy as available again
        record.getCopy().setAvailable(true);
        // (BookCopy is cascade-none, but it's already loaded in the persistence context
        //  after findByIdForUpdate — dirty-checking will flush it automatically)

        // 4. Atomically increment availableCopies on the Book row
        bookRepository.incrementAvailableCopies(record.getBook().getBookId(), now);

        // 4b. Notify the next person in the reservation queue (if any)
        reservationService.notifyNextInQueue(record.getBook().getBookId());

        // 5. Compute overdue fine
        long daysLate = Math.max(0, ChronoUnit.DAYS.between(record.getDueDate(), today));
        if (daysLate <= 0) {
            return null; // on time — no fine
        }

        // Guard: should not happen under normal flow, but defensive check
        if (fineRepository.existsByBorrowRecordRecordId(recordId)) {
            throw new BusinessRuleException("A fine already exists for this borrow record.");
        }

        if (librarianRepository.findByUserIdWithUser(librarianUserId).isEmpty()) {
            throw new BusinessRuleException("Librarian account not found.");
        }

        BigDecimal amount = BigDecimal.valueOf(daysLate * (long) record.getBook().getFinePerDayPkr())
                .setScale(2, RoundingMode.HALF_UP);

        Fine fine = new Fine();
        fine.setFineId(UUID.randomUUID().toString());
        fine.setBorrowRecord(record);
        fine.setStudent(record.getStudent());
        fine.setAmount(amount);
        fine.setDaysLate((int) daysLate);
        fine.setStatus(FineStatus.UNPAID);
        fine.setIssuedAt(now);
        // This field is only for paid/waived actions.
        fine.setResolvedBy(null);

        return fineRepository.save(fine);
    }

    // -----------------------------------------------------------------------
    // Read-only helpers used by controllers
    // -----------------------------------------------------------------------

    @Transactional(readOnly = true)
    public List<BorrowRecord> listActiveLoans() {
        return borrowRecordRepository.findAllActiveWithDetails();
    }

    @Transactional(readOnly = true)
    public List<BorrowRecord> listRecentlyReturnedLoans() {
        return borrowRecordRepository.findAllRecentlyReturnedWithDetails().stream().limit(30).toList();
    }

    /**
     * Rows for the librarian “Recently Returned (Undo)” table: every returned loan that still has
     * an unpaid fine (receipt line items), plus other recent returns without duplicates, newest first.
     */
    @Transactional(readOnly = true)
    public List<BorrowRecord> listReturnsForUndoSection() {
        Map<String, BorrowRecord> byId = new LinkedHashMap<>();
        for (BorrowRecord r : borrowRecordRepository.findReturnedWithUnpaidFineWithDetails(FineStatus.UNPAID)) {
            byId.put(r.getRecordId(), r);
        }
        borrowRecordRepository.findAllRecentlyReturnedWithDetails().stream()
                .limit(80)
                .forEach(r -> byId.putIfAbsent(r.getRecordId(), r));
        List<BorrowRecord> out = new ArrayList<>(byId.values());
        out.sort(Comparator.comparing(BorrowRecord::getReturnedAt, Comparator.nullsLast(Comparator.naturalOrder()))
                .reversed());
        return out;
    }

    @Transactional
    public void undoReturn(String recordId, String librarianUserId) {
        BorrowRecord record = borrowRecordRepository.findByIdForUpdate(recordId)
                .orElseThrow(() -> new BusinessRuleException("Borrow record not found."));

        if (record.getReturnedAt() == null) {
            throw new BusinessRuleException("This loan is still active and cannot be undone.");
        }
        if (librarianRepository.findByUserIdWithUser(librarianUserId).isEmpty()) {
            throw new BusinessRuleException("Librarian account not found.");
        }

        Fine fine = fineRepository.findByBorrowRecordRecordIdForUpdate(recordId).orElse(null);
        if (fine != null && (fine.getStatus() == FineStatus.PAID || fine.getStatus() == FineStatus.WAIVED)) {
            throw new BusinessRuleException("Undo is locked because this fine is already paid or waived.");
        }

        String copyBlock = physicalCopyUnavailableUndoMessage(record);
        if (copyBlock != null) {
            throw new BusinessRuleException(copyBlock);
        }
        String loanLimitBlock = activeLoanLimitUndoMessage(record);
        if (loanLimitBlock != null) {
            throw new BusinessRuleException(loanLimitBlock);
        }

        Instant returnedAt = record.getReturnedAt();
        if (reservationService.hasUndoBlockingReadyReservation(record.getBook().getBookId(), returnedAt)) {
            throw new BusinessRuleException(
                    "Undo is not possible because another patron already has this title on hold (ready for pickup) from an earlier return.");
        }

        int updated = bookRepository.decrementAvailableCopiesIfAvailable(record.getBook().getBookId(), Instant.now());
        if (updated == 0) {
            throw new BusinessRuleException("Undo failed because available copies cannot be reduced safely.");
        }

        record.getCopy().setAvailable(false);
        record.setReturnedAt(null);
        borrowRecordRepository.save(record);

        if (fine != null) {
            fineRepository.delete(fine);
        }

        reservationService.revertReadyPromotionFromReturn(record.getBook().getBookId(), returnedAt);
    }

    @Transactional(readOnly = true)
    public boolean canUndoReturn(BorrowRecord returnedLoan) {
        return explainUndoLockReason(returnedLoan) == null;
    }

    @Transactional(readOnly = true)
    public String fineStatusLabel(BorrowRecord returnedLoan) {
        return fineRepository.findByBorrowRecordRecordId(returnedLoan.getRecordId())
                .map(f -> f.getStatus().name())
                .orElse("NO_FINE");
    }

    @Transactional(readOnly = true)
    public BigDecimal fineAmountForLoan(BorrowRecord returnedLoan) {
        return fineRepository.findByBorrowRecordRecordId(returnedLoan.getRecordId())
                .map(Fine::getAmount)
                .orElse(BigDecimal.ZERO);
    }

    @Transactional(readOnly = true)
    public String explainUndoLockReason(BorrowRecord returnedLoan) {
        if (returnedLoan.getReturnedAt() == null) {
            return "Undo locked: loan is still active.";
        }
        Fine fine = fineRepository.findByBorrowRecordRecordId(returnedLoan.getRecordId()).orElse(null);
        if (fine != null && (fine.getStatus() == FineStatus.PAID || fine.getStatus() == FineStatus.WAIVED)) {
            return "Undo locked: fine is already " + fine.getStatus().name().toLowerCase() + ".";
        }
        String copyBlock = physicalCopyUnavailableUndoMessage(returnedLoan);
        if (copyBlock != null) {
            return copyBlock;
        }
        String loanLimitBlock = activeLoanLimitUndoMessage(returnedLoan);
        if (loanLimitBlock != null) {
            return loanLimitBlock;
        }
        if (reservationService.hasUndoBlockingReadyReservation(
                returnedLoan.getBook().getBookId(), returnedLoan.getReturnedAt())) {
            return "Undo locked: another student already has this book ready for pickup from an earlier return.";
        }
        return null;
    }

    /**
     * When non-null, undo cannot proceed because the physical copy from this returned loan is not
     * sitting available (typically checked out again on a newer loan).
     */
    private String physicalCopyUnavailableUndoMessage(BorrowRecord returnedLoan) {
        if (returnedLoan.getCopy() == null) {
            return "Undo locked: the physical copy link for this loan is missing.";
        }
        if (returnedLoan.getCopy().isAvailable()) {
            return null;
        }
        Optional<BorrowRecord> newer =
                borrowRecordRepository.findActiveByCopyCopyId(returnedLoan.getCopy().getCopyId());
        if (newer.isPresent()) {
            boolean sameStudent = newer.get()
                    .getStudent()
                    .getUserId()
                    .equals(returnedLoan.getStudent().getUserId());
            if (sameStudent) {
                return "Undo locked: this copy is out again on a newer loan to the same student. "
                        + "Return that newer loan first if you need to reverse this return.";
            }
            return "Undo locked: this copy was checked out again by another patron after the return.";
        }
        return "Undo locked: this copy is not marked available (no active loan found — check copy data).";
    }

    private String activeLoanLimitUndoMessage(BorrowRecord returnedLoan) {
        String studentUserId = returnedLoan.getStudent().getUserId();
        long activeLoans = borrowRecordRepository.countByStudentUserIdAndReturnedAtIsNull(studentUserId);
        if (activeLoans >= MAX_ACTIVE_LOANS) {
            return "Undo locked: student already has "
                    + activeLoans
                    + " active loans (maximum allowed is "
                    + MAX_ACTIVE_LOANS
                    + ").";
        }
        return null;
    }
}
