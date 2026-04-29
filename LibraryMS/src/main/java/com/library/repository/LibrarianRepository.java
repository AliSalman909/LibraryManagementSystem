package com.library.repository;

import com.library.entity.Librarian;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface LibrarianRepository extends JpaRepository<Librarian, String> {

    @Query("""
            select l
            from Librarian l
            join fetch l.user
            where l.userId = :userId
            """)
    Optional<Librarian> findByUserIdWithUser(@Param("userId") String userId);
}
