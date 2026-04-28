package com.library.repository;

import com.library.entity.BorrowRequest;
import com.library.entity.enums.BorrowRequestStatus;
import jakarta.persistence.LockModeType;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface BorrowRequestRepository extends JpaRepository<BorrowRequest, String> {

    @Query("""
            select br
            from BorrowRequest br
            join fetch br.book b
            join fetch br.student s
            join fetch s.user su
            where br.status = :status
            order by br.requestedAt asc
            """)
    List<BorrowRequest> findAllByStatusWithDetails(@Param("status") BorrowRequestStatus status);

    @Query("""
            select br
            from BorrowRequest br
            join fetch br.book b
            join fetch br.student s
            where s.userId = :studentUserId
            order by br.requestedAt desc
            """)
    List<BorrowRequest> findAllByStudentUserIdWithBook(@Param("studentUserId") String studentUserId);

    boolean existsByStudentUserIdAndBookBookIdAndStatus(String studentUserId, String bookId, BorrowRequestStatus status);

    long countDistinctBookBookIdByStudentUserIdAndStatus(String studentUserId, BorrowRequestStatus status);

    @Query("""
            select br
            from BorrowRequest br
            join fetch br.book
            join fetch br.student s
            join fetch s.user
            left join fetch br.processedBy p
            left join fetch p.user
            where br.requestId = :requestId
            """)
    Optional<BorrowRequest> findByIdWithDetails(@Param("requestId") String requestId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("""
            select br
            from BorrowRequest br
            join fetch br.book
            join fetch br.student s
            join fetch s.user
            left join fetch br.processedBy p
            left join fetch p.user
            where br.requestId = :requestId
            """)
    Optional<BorrowRequest> findByIdWithDetailsForUpdate(@Param("requestId") String requestId);

    long deleteByBookBookIdAndStatus(String bookId, BorrowRequestStatus status);

    long deleteByBookBookId(String bookId);
}
