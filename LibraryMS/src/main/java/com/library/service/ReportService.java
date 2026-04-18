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
        long unpaid = fineRepository.countByStatus(FineStatus.UNPAID);
        long paid = fineRepository.countByStatus(FineStatus.PAID);
        long waived = fineRepository.countByStatus(FineStatus.WAIVED);
        BigDecimal unpaidAmount = fineRepository.sumAmountByStatus(FineStatus.UNPAID);
        BigDecimal paidAmount = fineRepository.sumAmountByStatus(FineStatus.PAID);
        BigDecimal waivedAmount = fineRepository.sumAmountByStatus(FineStatus.WAIVED);
        return new FineReport(unpaid, paid, waived, unpaidAmount, paidAmount, waivedAmount);
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
            long unpaidCount, long paidCount, long waivedCount,
            BigDecimal unpaidAmount, BigDecimal paidAmount, BigDecimal waivedAmount) {

        public long totalCount() { return unpaidCount + paidCount + waivedCount; }
        public BigDecimal totalAmount() { return unpaidAmount.add(paidAmount).add(waivedAmount); }
    }

    public record ActivityReport(
            long totalUsers, long activeLoans, long completedLoans, long overdueLoans) {

        public long totalLoans() { return activeLoans + completedLoans; }
    }
}
