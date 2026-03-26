package com.library.service;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.library.entity.Book;
import com.library.entity.BookCopy;
import com.library.entity.BorrowRecord;
import com.library.entity.BorrowRequest;
import com.library.entity.Librarian;
import com.library.entity.Student;
import com.library.entity.enums.BorrowRequestStatus;
import com.library.exception.BusinessRuleException;
import com.library.repository.BookCopyRepository;
import com.library.repository.BookRepository;
import com.library.repository.BorrowRecordRepository;
import com.library.repository.BorrowRequestRepository;
import com.library.repository.LibrarianRepository;
import com.library.repository.StudentRepository;

@Service
public class BorrowRequestService {
    private static final int MAX_ACTIVE_UNIQUE_BOOKS = 3;
    private static final int DEFAULT_LOAN_DAYS = 14;
    private static final int ALT_LOAN_DAYS = 7;

    private final BorrowRequestRepository borrowRequestRepository;
    private final BorrowRecordRepository borrowRecordRepository;
    private final BookRepository bookRepository;
    private final BookCopyRepository bookCopyRepository;
    private final StudentRepository studentRepository;
    private final LibrarianRepository librarianRepository;

    public BorrowRequestService(
            BorrowRequestRepository borrowRequestRepository,
            BorrowRecordRepository borrowRecordRepository,
            BookRepository bookRepository,
            BookCopyRepository bookCopyRepository,
            StudentRepository studentRepository,
            LibrarianRepository librarianRepository) {
        this.borrowRequestRepository = borrowRequestRepository;
        this.borrowRecordRepository = borrowRecordRepository;
        this.bookRepository = bookRepository;
        this.bookCopyRepository = bookCopyRepository;
        this.studentRepository = studentRepository;
        this.librarianRepository = librarianRepository;
    }

    @Transactional
    public void createRequest(String studentUserId, String bookId) {
        createPendingRequest(studentUserId, bookId, null);
    }

    @Transactional
    public BorrowRequest createPendingRequest(String studentUserId, String bookId) {
        return createPendingRequest(studentUserId, bookId, null);
    }

    @Transactional
    public BorrowRequest createPendingRequest(String studentUserId, String bookId, Integer durationDays) {
        Student student = studentRepository.findByUserIdWithUserForUpdate(studentUserId)
                .orElseThrow(() -> new BusinessRuleException("Student account not found."));
        if (!student.isCanBorrow()) {
            throw new BusinessRuleException("Borrowing is disabled for this student account.");
        }

        if (borrowRecordRepository.existsByStudentUserIdAndBookBookIdAndReturnedAtIsNull(studentUserId, bookId)) {
            throw new BusinessRuleException("You already have an active loan for this book.");
        }

        if (borrowRequestRepository.existsByStudentUserIdAndBookBookIdAndStatus(
                studentUserId, bookId, BorrowRequestStatus.PENDING)) {
            throw new BusinessRuleException("You already have a pending request for this book.");
        }

        long activeUniqueBooks =
                borrowRecordRepository.countDistinctBookBookIdByStudentUserIdAndReturnedAtIsNull(studentUserId);
        long pendingUniqueBooks =
                borrowRequestRepository.countDistinctBookBookIdByStudentUserIdAndStatus(
                        studentUserId, BorrowRequestStatus.PENDING);
        long totalUniqueRequestedBooks = activeUniqueBooks + pendingUniqueBooks;

        int maxBooks = Math.min(student.getMaxBorrowLimit(), MAX_ACTIVE_UNIQUE_BOOKS);
        if (totalUniqueRequestedBooks >= maxBooks) {
            throw new BusinessRuleException(
                    "BORROW_LIMIT:You can request at most "
                            + maxBooks
                            + " books at a time. Return one to request a new book.");
        }

        @SuppressWarnings("null")
        Book book = bookRepository.findById(bookId).orElseThrow(() -> new BusinessRuleException("Book not found."));
        if (book.getAvailableCopies() <= 0) {
            throw new BusinessRuleException("This book is currently unavailable.");
        }

        BorrowRequest request = new BorrowRequest();
        request.setRequestId(UUID.randomUUID().toString());
        request.setBook(book);
        request.setStudent(student);
        request.setStatus(BorrowRequestStatus.PENDING);
        request.setRequestedAt(Instant.now());
        request.setRequestedDurationDays(normalizeDuration(durationDays));
        return borrowRequestRepository.save(request);
    }

    @Transactional(readOnly = true)
    public List<BorrowRequest> listPendingForLibrarian() {
        return borrowRequestRepository.findAllByStatusWithDetails(BorrowRequestStatus.PENDING);
    }

    @Transactional(readOnly = true)
    public List<BorrowRequest> listForStudent(String studentUserId) {
        return borrowRequestRepository.findAllByStudentUserIdWithBook(studentUserId);
    }

    @Transactional
    public BorrowRecord approve(String requestId, String librarianUserId, LocalDate dueDate) {
        BorrowRequest request = borrowRequestRepository.findByIdWithDetailsForUpdate(requestId)
                .orElseThrow(() -> new BusinessRuleException("Borrow request not found."));
        if (request.getStatus() != BorrowRequestStatus.PENDING) {
            throw new BusinessRuleException("Only pending requests can be approved.");
        }
        if (borrowRecordRepository.existsByBorrowRequestRequestId(requestId)) {
            throw new BusinessRuleException("This request has already been approved.");
        }

        Integer requestedDuration = request.getRequestedDurationDays() > 0 ? request.getRequestedDurationDays() : DEFAULT_LOAN_DAYS;
        LocalDate finalDueDate = resolveDueDate(dueDate, requestedDuration);

        Librarian librarian = librarianRepository.findByUserIdWithUser(librarianUserId)
                .orElseThrow(() -> new BusinessRuleException("Librarian account not found."));

        Book book = request.getBook();
        // Lock the book row so concurrent book edits (update/delete) can't race against approvals.
        bookRepository
                .findByIdForUpdate(book.getBookId())
                .orElseThrow(() -> new BusinessRuleException("Book not found."));
        int updatedRows = bookRepository.decrementAvailableCopiesIfAvailable(book.getBookId(), Instant.now());
        if (updatedRows == 0) {
            throw new BusinessRuleException("Book is no longer available.");
        }
        List<BookCopy> lockedAvailableCopies =
                bookCopyRepository.findAvailableCopiesForUpdateOrderByCopyNumberAsc(book.getBookId());
        BookCopy availableCopy = lockedAvailableCopies.stream()
                .findFirst()
                .orElseThrow(() -> new BusinessRuleException("No available copy found for this book."));
        availableCopy.setAvailable(false);

        request.setStatus(BorrowRequestStatus.APPROVED);
        request.setReviewedAt(Instant.now());
        request.setDueDate(finalDueDate);
        request.setProcessedBy(librarian);

        BorrowRecord record = new BorrowRecord();
        record.setRecordId(UUID.randomUUID().toString());
        record.setBorrowRequest(request);
        record.setBook(book);
        record.setCopy(availableCopy);
        record.setStudent(request.getStudent());
        record.setIssuedBy(librarian);
        record.setIssuedAt(Instant.now());
        record.setDueDate(finalDueDate);

        borrowRequestRepository.save(request);
        bookCopyRepository.save(availableCopy);
        return borrowRecordRepository.save(record);
    }

    @Transactional
    public BorrowRecord approveWithDuration(
            String requestId,
            String librarianUserId,
            LocalDate dueDate,
            Integer durationDays) {
        LocalDate finalDueDate = resolveDueDate(dueDate, durationDays);
        return approve(requestId, librarianUserId, finalDueDate);
    }

    @Transactional
    public void reject(String requestId, String librarianUserId) {
        BorrowRequest request = borrowRequestRepository.findByIdWithDetailsForUpdate(requestId)
                .orElseThrow(() -> new BusinessRuleException("Borrow request not found."));
        if (request.getStatus() != BorrowRequestStatus.PENDING) {
            throw new BusinessRuleException("Only pending requests can be rejected.");
        }

        Librarian librarian = librarianRepository.findByUserIdWithUser(librarianUserId)
                .orElseThrow(() -> new BusinessRuleException("Librarian account not found."));

        request.setStatus(BorrowRequestStatus.REJECTED);
        request.setReviewedAt(Instant.now());
        request.setProcessedBy(librarian);
        borrowRequestRepository.save(request);
    }

    private LocalDate resolveDueDate(LocalDate dueDate, Integer durationDays) {
        LocalDate today = LocalDate.now();
        if (dueDate != null) {
            if (!dueDate.isAfter(today)) {
                throw new BusinessRuleException("Due date must be a future date.");
            }
            return dueDate;
        }
        if (durationDays == null) {
            return today.plusDays(DEFAULT_LOAN_DAYS);
        }
        if (!isSupportedDuration(durationDays)) {
            throw new BusinessRuleException("durationDays must be either 7 or 14.");
        }
        return today.plusDays(durationDays);
    }

    private int normalizeDuration(Integer durationDays) {
        if (durationDays == null) {
            return DEFAULT_LOAN_DAYS;
        }
        if (!isSupportedDuration(durationDays)) {
            throw new BusinessRuleException("Borrow duration must be 7 or 14 days.");
        }
        return durationDays;
    }

    private boolean isSupportedDuration(int days) {
        return days == DEFAULT_LOAN_DAYS || days == ALT_LOAN_DAYS;
    }
}
