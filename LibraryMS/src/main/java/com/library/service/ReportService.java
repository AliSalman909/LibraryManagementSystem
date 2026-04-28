package com.library.service;

import com.library.entity.BorrowRecord;
import com.library.entity.enums.FineStatus;
import com.library.repository.BorrowRecordRepository;
import com.library.repository.FineRepository;
import com.library.repository.UserRepository;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Read-only service for admin reporting pages.
 */
@Service
public class ReportService {

    private final BorrowRecordRepository borrowRecordRepository;
    private final FineRepository fineRepository;
    private final UserRepository userRepository;

    public ReportService(
            BorrowRecordRepository borrowRecordRepository,
            FineRepository fineRepository,
            UserRepository userRepository) {
        this.borrowRecordRepository = borrowRecordRepository;
        this.fineRepository = fineRepository;
        this.userRepository = userRepository;
    }

    // -----------------------------------------------------------------------
    // Overdue report
    // -----------------------------------------------------------------------

    @Transactional(readOnly = true)
    public List<BorrowRecord> getOverdueLoans() {
        return borrowRecordRepository.findAllOverdueWithDetails(LocalDate.now());
    }

    // -----------------------------------------------------------------------
    // Issued books report (all active loans)
    // -----------------------------------------------------------------------

    @Transactional(readOnly = true)
    public List<BorrowRecord> getActiveLoans() {
        return borrowRecordRepository.findAllActiveWithDetails();
    }

    // -----------------------------------------------------------------------
    // Fine collection report (summary)
    // -----------------------------------------------------------------------

    @Transactional(readOnly = true)
    public FineReport getFineReport() {
        long unpaidIssued = fineRepository.countByStatus(FineStatus.UNPAID);
        long paid = fineRepository.countByStatus(FineStatus.PAID);
        long waived = fineRepository.countByStatus(FineStatus.WAIVED);
        BigDecimal unpaidIssuedAmount = fineRepository.sumAmountByStatus(FineStatus.UNPAID);
        BigDecimal paidGrossAmount = fineRepository.sumAmountByStatus(FineStatus.PAID);
        BigDecimal waivedGrossAmount = fineRepository.sumAmountByStatus(FineStatus.WAIVED);
        BigDecimal paidNetAmount = nonNegative(paidGrossAmount.subtract(fineRepository.sumWaivedAmountByStatus(FineStatus.PAID)));
        BigDecimal paidWaivedAdjustment = fineRepository.sumWaivedAmountByStatus(FineStatus.PAID);
        BigDecimal waivedNetAmount = nonNegative(waivedGrossAmount.subtract(fineRepository.sumWaivedAmountByStatus(FineStatus.WAIVED)));
        BigDecimal waivedAdjustmentAmount = fineRepository.sumWaivedAmountByStatus(FineStatus.WAIVED);
        BigDecimal paidAmount = paidNetAmount;
        BigDecimal waivedAmount = paidWaivedAdjustment.add(waivedAdjustmentAmount);

        List<BorrowRecord> liveOverdue = borrowRecordRepository.findAllOverdueWithDetails(LocalDate.now());
        long unpaidNotIssued = liveOverdue.size();
        BigDecimal unpaidNotIssuedAmount = liveOverdue.stream()
                .map(loan -> {
                    long daysLate = java.time.temporal.ChronoUnit.DAYS.between(loan.getDueDate(), LocalDate.now());
                    if (daysLate < 0) {
                        return BigDecimal.ZERO;
                    }
                    return BigDecimal.valueOf(loan.getBook().getFinePerDayPkr())
                            .multiply(BigDecimal.valueOf(daysLate));
                })
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        return new FineReport(
                unpaidIssued,
                unpaidNotIssued,
                paid,
                waived,
                unpaidIssuedAmount,
                unpaidNotIssuedAmount,
                paidAmount,
                waivedAmount,
                paidNetAmount,
                paidWaivedAdjustment,
                waivedNetAmount,
                waivedAdjustmentAmount);
    }

    // -----------------------------------------------------------------------
    // Activity report (counts)
    // -----------------------------------------------------------------------

    @Transactional(readOnly = true)
    public ActivityReport getActivityReport() {
        long totalUsers = userRepository.count();
        long activeLoans = borrowRecordRepository.countByReturnedAtIsNull();
        long completedLoans = borrowRecordRepository.countReturned();
        long overdueLoans = borrowRecordRepository.findAllOverdueWithDetails(LocalDate.now()).size();
        return new ActivityReport(totalUsers, activeLoans, completedLoans, overdueLoans);
    }

    // -----------------------------------------------------------------------
    // Report DTOs (inner records)
    // -----------------------------------------------------------------------

    public record FineReport(
            long unpaidIssuedCount,
            long unpaidNotIssuedCount,
            long paidCount,
            long waivedCount,
            BigDecimal unpaidIssuedAmount,
            BigDecimal unpaidNotIssuedAmount,
            BigDecimal paidAmount,
            BigDecimal waivedAmount,
            BigDecimal paidNetAmount,
            BigDecimal paidWaivedAdjustment,
            BigDecimal waivedNetAmount,
            BigDecimal waivedAdjustmentAmount) {

        public long unpaidTotalCount() {
            return unpaidIssuedCount + unpaidNotIssuedCount;
        }

        public BigDecimal unpaidTotalAmount() {
            return unpaidIssuedAmount.add(unpaidNotIssuedAmount);
        }

        public long totalCount() {
            return unpaidIssuedCount + unpaidNotIssuedCount + paidCount + waivedCount;
        }

        public BigDecimal totalAmount() {
            return unpaidIssuedAmount.add(unpaidNotIssuedAmount).add(paidAmount).add(waivedAmount);
        }
    }

    public record ActivityReport(
            long totalUsers, long activeLoans, long completedLoans, long overdueLoans) {

        public long totalLoans() { return activeLoans + completedLoans; }
    }

    private static BigDecimal nonNegative(BigDecimal value) {
        if (value == null) {
            return BigDecimal.ZERO;
        }
        return value.max(BigDecimal.ZERO);
    }
}
