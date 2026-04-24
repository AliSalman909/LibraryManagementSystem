package com.library.repository;

import com.library.entity.Fine;
import com.library.entity.enums.FineStatus;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface FineRepository extends JpaRepository<Fine, String> {

    /** Guard: prevent creating two fines for the same borrow record. */
    boolean existsByBorrowRecordRecordId(String recordId);

    /**
     * All fines for a student, newest first, with book info eagerly fetched
     * to avoid N+1 on the student fines page.
     */
    @Query("""
            select f from Fine f
            join fetch f.borrowRecord r
            join fetch r.book
            where f.student.userId = :studentUserId
            order by f.issuedAt desc
            """)
    List<Fine> findAllByStudentUserIdOrderByIssuedAtDesc(@Param("studentUserId") String studentUserId);

    /**
     * All UNPAID fines ordered by issue date ascending (oldest debt first)
     * for the librarian payment queue.
     */
    @Query("""
            select f from Fine f
            join fetch f.borrowRecord r
            join fetch r.book
            join fetch f.student s
            join fetch s.user
            left join fetch f.resolvedBy lb
            left join fetch lb.user
            where f.status = :status
            order by f.issuedAt asc
            """)
    List<Fine> findAllByStatusWithDetails(@Param("status") FineStatus status);

    /**
     * All fines (any status) with full details for the librarian history view.
     */
    @Query("""
            select f from Fine f
            join fetch f.borrowRecord r
            join fetch r.book
            join fetch f.student s
            join fetch s.user
            left join fetch f.resolvedBy lb
            left join fetch lb.user
            order by f.issuedAt desc
            """)
    List<Fine> findAllWithDetailsOrderByIssuedAtDesc();

    @Query("""
            select f from Fine f
            join fetch f.borrowRecord r
            join fetch r.book
            join fetch f.student s
            join fetch s.user
            left join fetch f.resolvedBy lb
            left join fetch lb.user
            where f.fineId = :fineId
            """)
    Optional<Fine> findByIdWithDetails(@Param("fineId") String fineId);

    /** Check if a student has any fine in a given status (used as renewal blocker). */
    boolean existsByStudentUserIdAndStatus(String studentUserId, FineStatus status);

    /** Count fines by status (for admin report summaries). */
    long countByStatus(FineStatus status);

    /** Sum of amounts by status (for admin fine collection report). */
    @Query("select coalesce(sum(f.amount), 0) from Fine f where f.status = :status")
    java.math.BigDecimal sumAmountByStatus(@Param("status") FineStatus status);
}
