package com.library.entity;

import com.library.entity.enums.BorrowRequestStatus;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;

@Entity
@Table(name = "borrow_requests")
public class BorrowRequest {

    @Id
    @Column(name = "request_id", nullable = false, length = 64)
    private String requestId;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "book_id", nullable = false, referencedColumnName = "book_id")
    private Book book;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "student_id", nullable = false, referencedColumnName = "user_id")
    private Student student;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "processed_by_librarian_id", referencedColumnName = "user_id")
    private Librarian processedBy;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 16)
    private BorrowRequestStatus status = BorrowRequestStatus.PENDING;

    @Column(name = "requested_at", nullable = false, updatable = false)
    private Instant requestedAt;

    @Column(name = "reviewed_at")
    private Instant reviewedAt;

    @Column(name = "due_date")
    private LocalDate dueDate;

    @Column(name = "requested_duration_days", nullable = false)
    private int requestedDurationDays = 14;

    public String getRequestId() {
        return requestId;
    }

    public void setRequestId(String requestId) {
        this.requestId = requestId;
    }

    public Book getBook() {
        return book;
    }

    public void setBook(Book book) {
        this.book = book;
    }

    public Student getStudent() {
        return student;
    }

    public void setStudent(Student student) {
        this.student = student;
    }

    public Librarian getProcessedBy() {
        return processedBy;
    }

    public void setProcessedBy(Librarian processedBy) {
        this.processedBy = processedBy;
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

    public Instant getReviewedAt() {
        return reviewedAt;
    }

    public void setReviewedAt(Instant reviewedAt) {
        this.reviewedAt = reviewedAt;
    }

    public LocalDate getDueDate() {
        return dueDate;
    }

    public void setDueDate(LocalDate dueDate) {
        this.dueDate = dueDate;
    }

    public int getRequestedDurationDays() {
        return requestedDurationDays;
    }

    public void setRequestedDurationDays(int requestedDurationDays) {
        this.requestedDurationDays = requestedDurationDays;
    }

    @Transient
    public LocalDate getRequestExpiresOn() {
        if (requestedAt == null) {
            return null;
        }
        return requestedAt.atZone(ZoneId.systemDefault()).toLocalDate().plusDays(requestedDurationDays);
    }

    @Transient
    public boolean isPendingExpired() {
        return status == BorrowRequestStatus.PENDING
                && getRequestExpiresOn() != null
                && LocalDate.now().isAfter(getRequestExpiresOn());
    }
}
