package com.library.repository;

import com.library.entity.Book;
import java.util.List;
import java.util.Optional;
import java.time.Instant;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface BookRepository extends JpaRepository<Book, String> {

    Optional<Book> findByIsbn(String isbn);

    boolean existsByIsbn(String isbn);

    @Query("""
            select b
            from Book b
            where lower(b.title) like lower(concat('%', :query, '%'))
               or lower(b.author) like lower(concat('%', :query, '%'))
               or lower(coalesce(b.category, '')) like lower(concat('%', :query, '%'))
               or lower(coalesce(b.isbn, '')) like lower(concat('%', :query, '%'))
            order by b.title asc
            """)
    List<Book> search(@Param("query") String query);

    @Query("""
            select b
            from Book b
            where (:title is null or lower(b.title) like lower(concat('%', :title, '%')))
              and (:author is null or lower(b.author) like lower(concat('%', :author, '%')))
            order by b.title asc
            """)
    List<Book> searchByTitleAndAuthor(
            @Param("title") String title,
            @Param("author") String author);

    @Modifying
    @Query("""
            update Book b
            set b.availableCopies = b.availableCopies - 1,
                b.updatedAt = :updatedAt
            where b.bookId = :bookId
              and b.availableCopies > 0
            """)
    int decrementAvailableCopiesIfAvailable(
            @Param("bookId") String bookId,
            @Param("updatedAt") Instant updatedAt);
}
