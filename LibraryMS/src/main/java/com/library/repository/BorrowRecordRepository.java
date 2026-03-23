package com.library.repository;

import com.library.entity.BorrowRecord;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BorrowRecordRepository extends JpaRepository<BorrowRecord, String> {

    boolean existsByBorrowRequestRequestId(String requestId);

    long countByStudentUserIdAndReturnedAtIsNull(String studentUserId);

    long countDistinctBookBookIdByStudentUserIdAndReturnedAtIsNull(String studentUserId);

    boolean existsByStudentUserIdAndBookBookIdAndReturnedAtIsNull(String studentUserId, String bookId);
}
