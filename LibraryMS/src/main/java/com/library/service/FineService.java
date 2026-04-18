package com.library.service;

import com.library.entity.Fine;
import com.library.entity.Librarian;
import com.library.entity.enums.FineStatus;
import com.library.exception.BusinessRuleException;
import com.library.repository.FineRepository;
import com.library.repository.LibrarianRepository;
import java.time.Instant;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Fine management: listing, marking paid, waiving.
 * The creation of fines is handled by {@link ReturnService}.
 */
@Service
public class FineService {

    private final FineRepository fineRepository;
    private final LibrarianRepository librarianRepository;

    public FineService(FineRepository fineRepository, LibrarianRepository librarianRepository) {
        this.fineRepository = fineRepository;
        this.librarianRepository = librarianRepository;
    }

    // -----------------------------------------------------------------------
    // Read queries
    // -----------------------------------------------------------------------

    @Transactional(readOnly = true)
    public List<Fine> listAllFines() {
        return fineRepository.findAllWithDetailsOrderByIssuedAtDesc();
    }

    @Transactional(readOnly = true)
    public List<Fine> listUnpaidFines() {
        return fineRepository.findAllByStatusWithDetails(FineStatus.UNPAID);
    }

    @Transactional(readOnly = true)
    public List<Fine> listFinesForStudent(String studentUserId) {
        return fineRepository.findAllByStudentUserIdOrderByIssuedAtDesc(studentUserId);
    }

    // -----------------------------------------------------------------------
    // State transitions (librarian actions)
    // -----------------------------------------------------------------------

    /**
     * Mark a fine as PAID.
     *
     * @throws BusinessRuleException if the fine is already resolved
     */
    @Transactional
    public Fine markPaid(String fineId, String librarianUserId, String notes) {
        Fine fine = loadFineForUpdate(fineId);
        ensureUnpaid(fine);
        Librarian librarian = resolveLibrarian(librarianUserId);

        fine.setStatus(FineStatus.PAID);
        fine.setResolvedAt(Instant.now());
        fine.setResolvedBy(librarian);
        if (notes != null && !notes.isBlank()) {
            fine.setNotes(notes.strip());
        }
        return fineRepository.save(fine);
    }

    /**
     * Waive a fine (e.g. goodwill gesture, system error, etc.).
     *
     * @throws BusinessRuleException if the fine is already resolved
     */
    @Transactional
    public Fine waive(String fineId, String librarianUserId, String notes) {
        Fine fine = loadFineForUpdate(fineId);
        ensureUnpaid(fine);
        Librarian librarian = resolveLibrarian(librarianUserId);

        fine.setStatus(FineStatus.WAIVED);
        fine.setResolvedAt(Instant.now());
        fine.setResolvedBy(librarian);
        if (notes != null && !notes.isBlank()) {
            fine.setNotes(notes.strip());
        }
        return fineRepository.save(fine);
    }

    // -----------------------------------------------------------------------
    // Private helpers
    // -----------------------------------------------------------------------

    private Fine loadFineForUpdate(String fineId) {
        return fineRepository.findById(fineId)
                .orElseThrow(() -> new BusinessRuleException("Fine not found."));
    }

    private void ensureUnpaid(Fine fine) {
        if (fine.getStatus() != FineStatus.UNPAID) {
            throw new BusinessRuleException(
                    "Fine is already " + fine.getStatus().name().toLowerCase() + " and cannot be modified.");
        }
    }

    private Librarian resolveLibrarian(String librarianUserId) {
        return librarianRepository.findByUserIdWithUser(librarianUserId)
                .orElseThrow(() -> new BusinessRuleException("Librarian account not found."));
    }
}
