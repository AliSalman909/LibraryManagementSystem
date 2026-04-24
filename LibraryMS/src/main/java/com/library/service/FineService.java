package com.library.service;

import com.library.entity.Fine;
import com.library.entity.Librarian;
import com.library.entity.enums.FineStatus;
import com.library.exception.BusinessRuleException;
import com.library.repository.BorrowRecordRepository;
import com.library.repository.FineRepository;
import com.library.repository.LibrarianRepository;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.Instant;
import java.util.List;
import java.util.Objects;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** Fine management: listing and librarian status updates. */
@Service
public class FineService {

    private final FineRepository fineRepository;
    private final LibrarianRepository librarianRepository;
    private final BorrowRecordRepository borrowRecordRepository;

    public FineService(
            FineRepository fineRepository,
            LibrarianRepository librarianRepository,
            BorrowRecordRepository borrowRecordRepository) {
        this.fineRepository = fineRepository;
        this.librarianRepository = librarianRepository;
        this.borrowRecordRepository = borrowRecordRepository;
    }

    // -----------------------------------------------------------------------
    // Read queries
    // -----------------------------------------------------------------------

    @Transactional(readOnly = true)
    public List<Fine> listAllFines() {
        return fineRepository.findAllWithDetailsOrderByIssuedAtDesc();
    }

    @Transactional(readOnly = true)
    public List<Fine> listFinesByFilter(String filter) {
        if (filter == null || filter.isBlank() || "all".equalsIgnoreCase(filter)) {
            return fineRepository.findAllWithDetailsOrderByIssuedAtDesc();
        }
        FineStatus status = parseFilterStatus(filter);
        return fineRepository.findAllByStatusWithDetails(status);
    }

    @Transactional(readOnly = true)
    public List<Fine> listFinesForStudent(String studentUserId) {
        return fineRepository.findAllByStudentUserIdOrderByIssuedAtDesc(studentUserId);
    }

    @Transactional(readOnly = true)
    public Fine getFineByIdWithDetails(String fineId) {
        return fineRepository.findByIdWithDetails(Objects.requireNonNull(fineId))
                .orElseThrow(() -> new BusinessRuleException("Fine not found."));
    }

    @Transactional(readOnly = true)
    public List<Fine> listUnpaidWithDetailsForStudent(String studentUserId) {
        return fineRepository.findAllUnpaidWithDetailsByStudentUserId(Objects.requireNonNull(studentUserId));
    }

    @Transactional(readOnly = true)
    public List<com.library.entity.BorrowRecord> listLiveOverdueLoans() {
        return borrowRecordRepository.findAllOverdueWithDetails(LocalDate.now());
    }

    // -----------------------------------------------------------------------
    // State transitions (librarian actions)
    // -----------------------------------------------------------------------

    @Transactional
    public Fine updateStatus(
            String fineId,
            FineStatus targetStatus,
            String librarianUserId,
            String notes,
            BigDecimal waivedAdjustment) {
        if (targetStatus == null) {
            throw new BusinessRuleException("Status is required.");
        }
        Fine fine = loadFineForUpdate(fineId);
        fine.setStatus(targetStatus);
        BigDecimal normalizedWaived = normalizeWaivedAmount(waivedAdjustment, fine.getAmount());

        if (targetStatus == FineStatus.UNPAID) {
            fine.setResolvedAt(null);
            fine.setResolvedBy(null);
            fine.setWaivedAmount(BigDecimal.ZERO);
        } else {
            Librarian librarian = resolveLibrarian(librarianUserId);
            fine.setResolvedAt(Instant.now());
            fine.setResolvedBy(librarian);
            fine.setWaivedAmount(targetStatus == FineStatus.WAIVED ? normalizedWaived : BigDecimal.ZERO);
        }

        fine.setNotes((notes != null && !notes.isBlank()) ? notes.strip() : null);
        return fineRepository.save(fine);
    }

    @Transactional
    public Fine updateWaivedAdjustment(String fineId, BigDecimal waivedAdjustment) {
        Fine fine = loadFineForUpdate(fineId);
        BigDecimal normalizedWaived = normalizeWaivedAmount(waivedAdjustment, fine.getAmount());
        fine.setWaivedAmount(normalizedWaived);
        return fineRepository.save(fine);
    }

    // -----------------------------------------------------------------------
    // Private helpers
    // -----------------------------------------------------------------------

    private Fine loadFineForUpdate(String fineId) {
        return fineRepository.findById(Objects.requireNonNull(fineId))
                .orElseThrow(() -> new BusinessRuleException("Fine not found."));
    }

    private Librarian resolveLibrarian(String librarianUserId) {
        return librarianRepository.findByUserIdWithUser(Objects.requireNonNull(librarianUserId))
                .orElseThrow(() -> new BusinessRuleException("Librarian account not found."));
    }

    private FineStatus parseFilterStatus(String filter) {
        return switch (filter.toLowerCase()) {
            case "unpaid" -> FineStatus.UNPAID;
            case "paid" -> FineStatus.PAID;
            case "waived" -> FineStatus.WAIVED;
            default -> throw new BusinessRuleException("Unsupported fine filter.");
        };
    }

    private BigDecimal normalizeWaivedAmount(BigDecimal waivedAdjustment, BigDecimal fineAmount) {
        BigDecimal amount = waivedAdjustment == null ? BigDecimal.ZERO : waivedAdjustment;
        amount = amount.setScale(2, RoundingMode.HALF_UP);
        if (amount.signum() < 0) {
            throw new BusinessRuleException("Waived adjustment cannot be negative.");
        }
        if (amount.compareTo(fineAmount) > 0) {
            throw new BusinessRuleException("Waived adjustment cannot exceed total fine amount.");
        }
        return amount;
    }
}
