package com.library.repository;

import com.library.entity.BorrowRecord;
import com.library.entity.enums.FineStatus;
import jakarta.persistence.LockModeType;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface BorrowRecordRepository extends JpaRepository<BorrowRecord, String> {

    boolean existsByBorrowRequestRequestId(String requestId);

    long countByStudentUserIdAndReturnedAtIsNull(String studentUserId);

    long countDistinctBookBookIdByStudentUserIdAndReturnedAtIsNull(String studentUserId);

    boolean existsByStudentUserIdAndBookBookIdAndReturnedAtIsNull(String studentUserId, String bookId);

    /**
     * Fetch all active (not yet returned) loans with their book, copy, student and user
     * in a single query to avoid N+1 on the librarian active-loans page.
     */
    @Query("""
            select r from BorrowRecord r
            join fetch r.book
            join fetch r.copy
            join fetch r.student s
            join fetch s.user
            where r.returnedAt is null
            order by r.dueDate asc
            """)
    List<BorrowRecord> findAllActiveWithDetails();

    @Query("""
            select r from BorrowRecord r
            join fetch r.book
            join fetch r.copy
            join fetch r.student s
            join fetch s.user
            where r.returnedAt is not null
            order by r.returnedAt desc
            """)
    List<BorrowRecord> findAllRecentlyReturnedWithDetails();

    /**
     * Returned loans that still have an UNPAID fine (same rows as receipt line items),
     * with book, copy, student for the librarian undo table.
     */
    @Query("""
            select r from BorrowRecord r
            join fetch r.book
            join fetch r.copy
            join fetch r.student s
            join fetch s.user
            where r.returnedAt is not null
              and exists (select 1 from Fine f
                          where f.borrowRecord.recordId = r.recordId
                            and f.status = :unpaid)
            order by r.returnedAt desc
            """)
    List<BorrowRecord> findReturnedWithUnpaidFineWithDetails(@Param("unpaid") FineStatus unpaid);

    /**
     * Fetch all loans for a student (returned + active) with book eagerly loaded,
     * used on the student's own borrow-history / fines page.
     */
    @Query("""
            select r from BorrowRecord r
            join fetch r.book
            join fetch r.student s
            join fetch s.user
            where s.userId = :studentUserId
            order by r.dueDate desc
            """)
    List<BorrowRecord> findAllByStudentUserIdWithBook(@Param("studentUserId") String studentUserId);

    /**
     * Pessimistic write lock used by ReturnService to prevent concurrent double-returns.
     */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("""
            select r from BorrowRecord r
            join fetch r.book
            join fetch r.copy c
            join fetch c.book
            join fetch r.student s
            join fetch s.user
            where r.recordId = :recordId
            """)
    Optional<BorrowRecord> findByIdForUpdate(@Param("recordId") String recordId);

    /**
     * Active (unreturned) loans for a specific student with book + copy info,
     * used on the Student "My Loans" page.
     */
    @Query("""
            select r from BorrowRecord r
            join fetch r.book
            join fetch r.copy
            join fetch r.student s
            join fetch s.user
            where s.userId = :studentUserId and r.returnedAt is null
            order by r.dueDate asc
            """)
    List<BorrowRecord> findActiveByStudentWithDetails(@Param("studentUserId") String studentUserId);

    /**
     * All overdue active loans (dueDate < today, not returned) for admin reports.
     */
    @Query("""
            select r from BorrowRecord r
            join fetch r.book
            join fetch r.copy
            join fetch r.student s
            join fetch s.user
            where r.returnedAt is null and r.dueDate < :today
            order by r.dueDate asc
            """)
    List<BorrowRecord> findAllOverdueWithDetails(@Param("today") java.time.LocalDate today);

    /** At most one active loan per physical copy; used for undo-return messaging. */
    @Query("""
            select r from BorrowRecord r
            join fetch r.student s
            where r.copy.copyId = :copyId and r.returnedAt is null
            """)
    Optional<BorrowRecord> findActiveByCopyCopyId(@Param("copyId") String copyId);

    /**
     * Count all active (unreturned) loans.
     */
    long countByReturnedAtIsNull();

    /**
     * Count loans returned within a date range (for reports).
     */
    @Query("select count(r) from BorrowRecord r where r.returnedAt is not null")
    long countReturned();
}

