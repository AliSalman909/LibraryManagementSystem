package com.library.service;

import com.library.entity.BorrowRecord;
import com.library.entity.enums.FineStatus;
import com.library.exception.BusinessRuleException;
import com.library.repository.BorrowRecordRepository;
import com.library.repository.FineRepository;
import com.library.repository.ReservationRepository;
import com.library.entity.enums.ReservationStatus;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Handles loan renewals (extending the due date).
 * <p>Business rules:</p>
 * <ul>
 *   <li>Loan must be active (not returned).</li>
 *   <li>Max 1 renewal per loan.</li>
 *   <li>Blocked if student has any unpaid fine.</li>
 *   <li>Blocked if book has pending/ready reservations by other users.</li>
 *   <li>Extension duration: 7 or 14 days from current due date.</li>
 * </ul>
 */
@Service
public class RenewalService {

    private static final int MAX_RENEWALS = 1;
    private static final int DEFAULT_RENEW_DAYS = 14;
    private static final int ALT_RENEW_DAYS = 7;

    private static final List<ReservationStatus> ACTIVE_RES_STATUSES =
            List.of(ReservationStatus.PENDING, ReservationStatus.READY);

    private final BorrowRecordRepository borrowRecordRepository;
    private final FineRepository fineRepository;
    private final ReservationRepository reservationRepository;

    public RenewalService(
            BorrowRecordRepository borrowRecordRepository,
            FineRepository fineRepository,
            ReservationRepository reservationRepository) {
        this.borrowRecordRepository = borrowRecordRepository;
        this.fineRepository = fineRepository;
        this.reservationRepository = reservationRepository;
    }

    /**
     * Renew an active loan. Extends the due date by the specified number of days.
     *
     * @param recordId     the borrow record to renew
     * @param studentUserId the student requesting renewal (for fine check)
     * @param durationDays 7 or 14 (nullable → defaults to 14)
     * @return the updated BorrowRecord
     */
    @Transactional
    public BorrowRecord renewLoan(String recordId, String studentUserId, Integer durationDays) {
        int days = normalizeDuration(durationDays);

        BorrowRecord record = borrowRecordRepository.findByIdForUpdate(recordId)
                .orElseThrow(() -> new BusinessRuleException("Borrow record not found."));

        // Must be this student's own record
        if (!record.getStudent().getUserId().equals(studentUserId)) {
            throw new BusinessRuleException("You can only renew your own loans.");
        }

        // Must be active (not returned)
        if (record.getReturnedAt() != null) {
            throw new BusinessRuleException("This loan has already been returned and cannot be renewed.");
        }

        // Max renewals check
        if (record.getRenewCount() >= MAX_RENEWALS) {
            throw new BusinessRuleException(
                    "This loan has already been renewed the maximum number of times (" + MAX_RENEWALS + ").");
        }

        // Unpaid fine blocker
        if (fineRepository.existsByStudentUserIdAndStatus(studentUserId, FineStatus.UNPAID)) {
            throw new BusinessRuleException(
                    "You have unpaid fines. Please clear all outstanding fines before renewing a loan.");
        }

        // Reservation blocker — block if another student is waiting for this book
        if (reservationRepository.countByBookAndStatusIn(
                record.getBook().getBookId(), ACTIVE_RES_STATUSES) > 0) {
            throw new BusinessRuleException(
                    "This book has active reservations from other students. Renewal is not allowed.");
        }

        Instant now = Instant.now();

        // Preserve original due date on first renewal
        if (record.getOriginalDueDate() == null) {
            record.setOriginalDueDate(record.getDueDate());
        }

        // Extend from current due date (or from today if already overdue, to avoid extending from the past)
        LocalDate today = LocalDate.now();
        LocalDate base = record.getDueDate().isBefore(today) ? today : record.getDueDate();
        record.setDueDate(base.plusDays(days));
        record.setRenewCount(record.getRenewCount() + 1);
        record.setLastRenewedAt(now);

        return borrowRecordRepository.save(record);
    }

    @Transactional(readOnly = true)
    public List<BorrowRecord> listActiveLoansForStudent(String studentUserId) {
        return borrowRecordRepository.findActiveByStudentWithDetails(studentUserId);
    }

    /**
     * Checks if renewal is possible for a specific record (used by templates to show/hide button).
     */
    @Transactional(readOnly = true)
    public boolean canRenew(BorrowRecord record, String studentUserId) {
        if (record.getReturnedAt() != null) return false;
        if (record.getRenewCount() >= MAX_RENEWALS) return false;
        if (fineRepository.existsByStudentUserIdAndStatus(studentUserId, FineStatus.UNPAID)) return false;
        if (reservationRepository.countByBookAndStatusIn(
                record.getBook().getBookId(), ACTIVE_RES_STATUSES) > 0) return false;
        return true;
    }

    private int normalizeDuration(Integer durationDays) {
        if (durationDays == null) {
            return DEFAULT_RENEW_DAYS;
        }
        if (durationDays != DEFAULT_RENEW_DAYS && durationDays != ALT_RENEW_DAYS) {
            throw new BusinessRuleException("Renewal duration must be 7 or 14 days.");
        }
        return durationDays;
    }
}
