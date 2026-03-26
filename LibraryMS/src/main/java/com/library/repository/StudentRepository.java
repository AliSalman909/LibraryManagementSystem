package com.library.repository;

import com.library.entity.Student;
import jakarta.persistence.LockModeType;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface StudentRepository extends JpaRepository<Student, String> {

    @Query("""
            select s
            from Student s
            join fetch s.user
            where s.userId = :userId
            """)
    Optional<Student> findByUserIdWithUser(@Param("userId") String userId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("""
            select s
            from Student s
            join fetch s.user
            where s.userId = :userId
            """)
    Optional<Student> findByUserIdWithUserForUpdate(@Param("userId") String userId);
}
