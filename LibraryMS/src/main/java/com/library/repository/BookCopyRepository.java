package com.library.repository;

import com.library.entity.BookCopy;
import jakarta.persistence.LockModeType;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface BookCopyRepository extends JpaRepository<BookCopy, String> {

    boolean existsByIsbnCode(String isbnCode);

    long countByBookBookId(String bookId);

    List<BookCopy> findByBookBookIdOrderByCopyNumberAsc(String bookId);

    Optional<BookCopy> findFirstByBookBookIdAndAvailableTrueOrderByCopyNumberAsc(String bookId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("""
            select bc
            from BookCopy bc
            where bc.book.bookId = :bookId and bc.available = true
            order by bc.copyNumber asc
            """)
    List<BookCopy> findAvailableCopiesForUpdateOrderByCopyNumberAsc(@Param("bookId") String bookId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("""
            select bc
            from BookCopy bc
            where bc.book.bookId = :bookId
            order by bc.copyNumber asc
            """)
    List<BookCopy> findAllCopiesForUpdateOrderByCopyNumberAsc(@Param("bookId") String bookId);
}
