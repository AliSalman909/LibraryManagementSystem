package com.library.dto;

import com.library.entity.enums.BorrowRequestStatus;
import java.time.Instant;

public class BorrowRequestResponse {

    private String requestId;
    private String bookId;
    private String studentId;
    private BorrowRequestStatus status;
    private Instant requestedAt;
    private int requestedDurationDays;

    public String getRequestId() {
        return requestId;
    }

    public void setRequestId(String requestId) {
        this.requestId = requestId;
    }

    public String getBookId() {
        return bookId;
    }

    public void setBookId(String bookId) {
        this.bookId = bookId;
    }

    public String getStudentId() {
        return studentId;
    }

    public void setStudentId(String studentId) {
        this.studentId = studentId;
    }

    public BorrowRequestStatus getStatus() {
        return status;
    }

    public void setStatus(BorrowRequestStatus status) {
        this.status = status;
    }

    public Instant getRequestedAt() {
        return requestedAt;
    }

    public void setRequestedAt(Instant requestedAt) {
        this.requestedAt = requestedAt;
    }

    public int getRequestedDurationDays() {
        return requestedDurationDays;
    }

    public void setRequestedDurationDays(int requestedDurationDays) {
        this.requestedDurationDays = requestedDurationDays;
    }
}
