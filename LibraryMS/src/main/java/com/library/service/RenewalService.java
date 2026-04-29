package com.library.service;

import com.library.entity.BorrowRecord;
import com.library.exception.BusinessRuleException;
import com.library.repository.BorrowRecordRepository;
import com.library.repository.ReservationRepository;
import com.library.entity.enums.ReservationStatus;
import java.time.Instant;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Handles loan renewals (extending the due date).
 * <p>Business rules:</p>
 * <ul>
 *   <li>Loan must be active (not returned).</li>
 *   <li>Max 3 renewals per loan.</li>
 *   <li>Blocked if student has any unpaid fine.</li>
 *   <li>Blocked if loan is overdue.</li>
 *   <li>Blocked if book has pending/ready reservations by other users.</li>
 *   <li>Extension duration options: 7/14/21/28 days, capped by the book's max borrow days.</li>
 * </ul>
 */
@Service
public class RenewalService {

    private static final int MAX_RENEWALS = 3;
    private static final int DEFAULT_RENEW_DAYS = 14;
    private static final List<Integer> SUPPORTED_RENEW_DURATIONS = List.of(7, 14, 21, 28);

    private static final List<ReservationStatus> ACTIVE_RES_STATUSES =
            List.of(ReservationStatus.PENDING, ReservationStatus.READY);

    private final BorrowRecordRepository borrowRecordRepository;
    private final ReservationRepository reservationRepository;

    public RenewalService(
            BorrowRecordRepository borrowRecordRepository,
            ReservationRepository reservationRepository) {
        this.borrowRecordRepository = borrowRecordRepository;
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
        BorrowRecord record = borrowRecordRepository.findByIdForUpdate(recordId)
                .orElseThrow(() -> new BusinessRuleException("Borrow record not found."));
        int days = normalizeDuration(durationDays, record);

        // Must be this student's own record
        if (!record.getStudent().getUserId().equals(studentUserId)) {
            throw new BusinessRuleException("You can only renew your own loans.");
        }

        // Must be active (not returned)
        if (record.getReturnedAt() != null) {
            throw new BusinessRuleException("This loan has already been returned and cannot be renewed.");
        }

        LocalDate today = LocalDate.now();
        if (record.getDueDate().isBefore(today)) {
            throw new BusinessRuleException(
                    "This loan is overdue. Please return the book and clear any fine before requesting it again.");
        }

        // Max renewals check
        if (record.getRenewCount() >= MAX_RENEWALS) {
            throw new BusinessRuleException(
                    "This loan has already been renewed the maximum number of times (" + MAX_RENEWALS + ").");
        }

        // Reservation blocker — block if another student is waiting for this book
        if (reservationRepository.countByBookAndStatusInAndStudentUserIdNot(
                record.getBook().getBookId(), ACTIVE_RES_STATUSES, studentUserId) > 0) {
            throw new BusinessRuleException(
                    "This book has active reservations from other students. Renewal is not allowed.");
        }

        Instant now = Instant.now();

        // Preserve original due date on first renewal
        if (record.getOriginalDueDate() == null) {
            record.setOriginalDueDate(record.getDueDate());
        }

        // Extend from the current due date.
        LocalDate base = record.getDueDate();
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
        return renewBlockReason(record, studentUserId) == null;
    }

    @Transactional(readOnly = true)
    public String renewBlockReason(BorrowRecord record, String studentUserId) {
        LocalDate today = LocalDate.now();
        if (record.getReturnedAt() != null) {
            return "This loan is already returned.";
        }
        if (record.getDueDate().isBefore(today)) {
            return "Overdue: renew is unavailable until this book is returned.";
        }
        if (record.getRenewCount() >= MAX_RENEWALS) {
            return "No renewals remaining for this loan.";
        }
        if (reservationRepository.countByBookAndStatusInAndStudentUserIdNot(
                record.getBook().getBookId(), ACTIVE_RES_STATUSES, studentUserId) > 0) {
            return "Renewal blocked: this title is currently reserved by another student.";
        }
        return null;
    }

    @Transactional(readOnly = true)
    public List<Integer> allowedRenewDurations(BorrowRecord record) {
        int maxBorrowDays = record.getBook().getMaxBorrowDays();
        List<Integer> allowed = new ArrayList<>();
        for (Integer d : SUPPORTED_RENEW_DURATIONS) {
            if (d <= maxBorrowDays) {
                allowed.add(d);
            }
        }
        if (allowed.isEmpty()) {
            allowed.add(Math.min(DEFAULT_RENEW_DAYS, maxBorrowDays));
        }
        return allowed;
    }

    public int getMaxRenewals() {
        return MAX_RENEWALS;
    }

    private int normalizeDuration(Integer durationDays, BorrowRecord record) {
        List<Integer> allowedDurations = allowedRenewDurations(record);
        int selectedDays = durationDays == null ? DEFAULT_RENEW_DAYS : durationDays;
        if (!allowedDurations.contains(selectedDays)) {
            throw new BusinessRuleException(
                    "Renewal duration is not allowed for this book. Please choose one of: "
                            + allowedDurations
                            + " days.");
        }
        return selectedDays;
    }
}
