package com.library.service;

import com.library.entity.BorrowRecord;
import com.library.entity.Fine;
import com.library.entity.Librarian;
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
import java.util.List;
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

        Librarian librarian = librarianRepository.findByUserIdWithUser(librarianUserId)
                .orElseThrow(() -> new BusinessRuleException("Librarian account not found."));

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
        fine.setResolvedBy(librarian); // issuing librarian tracks who processed the return

        return fineRepository.save(fine);
    }

    // -----------------------------------------------------------------------
    // Read-only helpers used by controllers
    // -----------------------------------------------------------------------

    @Transactional(readOnly = true)
    public List<BorrowRecord> listActiveLoans() {
        return borrowRecordRepository.findAllActiveWithDetails();
    }
}
