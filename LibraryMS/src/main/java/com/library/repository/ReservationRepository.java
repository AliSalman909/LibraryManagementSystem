package com.library.repository;

import com.library.entity.Reservation;
import com.library.entity.enums.ReservationStatus;
import jakarta.persistence.LockModeType;
import java.time.Instant;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ReservationRepository extends JpaRepository<Reservation, String> {

    /**
     * Find the next PENDING reservation for a book (lowest queue_position first).
     * Used when a copy is returned to promote to READY.
     */
    @Query("""
            select r from Reservation r
            join fetch r.student s
            join fetch s.user
            join fetch r.book
            where r.book.bookId = :bookId and r.status = 'PENDING'
            order by r.queuePosition asc
            """)
    List<Reservation> findPendingByBookOrderByPosition(@Param("bookId") String bookId);

    /**
     * Check if a student already has an active reservation (PENDING or READY) for a book.
     */
    @Query("""
            select count(r) > 0 from Reservation r
            where r.student.userId = :studentUserId
              and r.book.bookId = :bookId
              and r.status in :statuses
            """)
    boolean existsByStudentAndBookAndStatusIn(
            @Param("studentUserId") String studentUserId,
            @Param("bookId") String bookId,
            @Param("statuses") List<ReservationStatus> statuses);

    /**
     * Count active reservations (PENDING or READY) for a book.
     * Used by RenewalService to block renewals when others are waiting.
     */
    @Query("""
            select count(r) from Reservation r
            where r.book.bookId = :bookId
              and r.status in :statuses
            """)
    long countByBookAndStatusIn(
            @Param("bookId") String bookId,
            @Param("statuses") List<ReservationStatus> statuses);

    /**
     * Find max queue position for a book (to assign the next FIFO slot).
     */
    @Query("select coalesce(max(r.queuePosition), 0) from Reservation r where r.book.bookId = :bookId")
    int findMaxQueuePositionByBookId(@Param("bookId") String bookId);

    /**
     * Student's own reservations (all statuses) with book info, newest first.
     */
    @Query("""
            select r from Reservation r
            join fetch r.book
            where r.student.userId = :studentUserId
            order by r.createdAt desc
            """)
    List<Reservation> findAllByStudentWithBook(@Param("studentUserId") String studentUserId);

    /**
     * All reservations with student + book details for the librarian queue view.
     * Ordered by book title then queue position.
     */
    @Query("""
            select r from Reservation r
            join fetch r.book b
            join fetch r.student s
            join fetch s.user
            order by b.title asc, r.queuePosition asc
            """)
    List<Reservation> findAllWithDetails();

    /**
     * All READY reservations for the librarian to act on.
     */
    @Query("""
            select r from Reservation r
            join fetch r.book b
            join fetch r.student s
            join fetch s.user
            where r.status = 'READY'
            order by r.expiresAt asc
            """)
    List<Reservation> findAllReadyWithDetails();

    /**
     * Expired READY reservations (pickup window passed).
     */
    @Query("""
            select r from Reservation r
            where r.status = 'READY' and r.expiresAt < :now
            """)
    List<Reservation> findExpiredReady(@Param("now") Instant now);

    /**
     * READY reservations for a book whose notification time is outside the undo window
     * for a given return (blocks undo — not caused by this return).
     */
    @Query("""
            select count(r) > 0 from Reservation r
            where r.book.bookId = :bookId
              and r.status = 'READY'
              and (r.notifiedAt is null or r.notifiedAt < :fromInclusive or r.notifiedAt > :toInclusive)
            """)
    boolean existsReadyOutsideNotifiedWindow(
            @Param("bookId") String bookId,
            @Param("fromInclusive") Instant fromInclusive,
            @Param("toInclusive") Instant toInclusive);

    /**
     * READY reservations likely promoted by this return (same notify batch), ordered oldest first.
     */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("""
            select r from Reservation r
            where r.book.bookId = :bookId
              and r.status = 'READY'
              and r.notifiedAt >= :fromInclusive
              and r.notifiedAt <= :toInclusive
            order by r.notifiedAt asc
            """)
    List<Reservation> findReadyNotifiedInWindowForUpdate(
            @Param("bookId") String bookId,
            @Param("fromInclusive") Instant fromInclusive,
            @Param("toInclusive") Instant toInclusive);
}
