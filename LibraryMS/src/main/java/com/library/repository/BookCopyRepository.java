package com.library.repository;

import com.library.entity.BookCopy;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BookCopyRepository extends JpaRepository<BookCopy, String> {

    boolean existsByIsbnCode(String isbnCode);

    long countByBookBookId(String bookId);

    List<BookCopy> findByBookBookIdOrderByCopyNumberAsc(String bookId);

    Optional<BookCopy> findFirstByBookBookIdAndAvailableTrueOrderByCopyNumberAsc(String bookId);
}
