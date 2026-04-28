package com.library.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import java.time.Instant;
import java.time.LocalDate;

@Entity
@Table(name = "borrow_records")
public class BorrowRecord {

    @Id
    @Column(name = "record_id", nullable = false, length = 64)
    private String recordId;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "request_id", nullable = false, referencedColumnName = "request_id", unique = true)
    private BorrowRequest borrowRequest;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "book_id", nullable = false, referencedColumnName = "book_id")
    private Book book;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "copy_id", referencedColumnName = "copy_id")
    private BookCopy copy;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "student_id", nullable = false, referencedColumnName = "user_id")
    private Student student;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "issued_by_librarian_id", nullable = false, referencedColumnName = "user_id")
    private Librarian issuedBy;

    @Column(name = "issued_at", nullable = false, updatable = false)
    private Instant issuedAt;

    @Column(name = "due_date", nullable = false)
    private LocalDate dueDate;

    @Column(name = "returned_at")
    private Instant returnedAt;

    /** Number of times this loan has been renewed. Defaults to 0. */
    @Column(name = "renew_count", nullable = false)
    private int renewCount = 0;

    /** Preserved original due date before any renewal extension. Set on first renew. */
    @Column(name = "original_due_date")
    private LocalDate originalDueDate;

    /** Timestamp of the most recent renewal. */
    @Column(name = "last_renewed_at")
    private Instant lastRenewedAt;

    /** Pending renewal-request flag required by the current DB schema. */
    @Column(name = "renew_request_pending", nullable = false)
    private boolean renewRequestPending = false;

    @Column(name = "renew_requested_at")
    private Instant renewRequestedAt;

    @Column(name = "renew_requested_days")
    private Integer renewRequestedDays;

    public String getRecordId() {
        return recordId;
    }

    public void setRecordId(String recordId) {
        this.recordId = recordId;
    }

    public BorrowRequest getBorrowRequest() {
        return borrowRequest;
    }

    public void setBorrowRequest(BorrowRequest borrowRequest) {
        this.borrowRequest = borrowRequest;
    }

    public Book getBook() {
        return book;
    }

    public void setBook(Book book) {
        this.book = book;
    }

    public BookCopy getCopy() {
        return copy;
    }

    public void setCopy(BookCopy copy) {
        this.copy = copy;
    }

    public Student getStudent() {
        return student;
    }

    public void setStudent(Student student) {
        this.student = student;
    }

    public Librarian getIssuedBy() {
        return issuedBy;
    }

    public void setIssuedBy(Librarian issuedBy) {
        this.issuedBy = issuedBy;
    }

    public Instant getIssuedAt() {
        return issuedAt;
    }

    public void setIssuedAt(Instant issuedAt) {
        this.issuedAt = issuedAt;
    }

    public LocalDate getDueDate() {
        return dueDate;
    }

    public void setDueDate(LocalDate dueDate) {
        this.dueDate = dueDate;
    }

    public Instant getReturnedAt() {
        return returnedAt;
    }

    public void setReturnedAt(Instant returnedAt) {
        this.returnedAt = returnedAt;
    }

    public int getRenewCount() {
        return renewCount;
    }

    public void setRenewCount(int renewCount) {
        this.renewCount = renewCount;
    }

    public LocalDate getOriginalDueDate() {
        return originalDueDate;
    }

    public void setOriginalDueDate(LocalDate originalDueDate) {
        this.originalDueDate = originalDueDate;
    }

    public Instant getLastRenewedAt() {
        return lastRenewedAt;
    }

    public void setLastRenewedAt(Instant lastRenewedAt) {
        this.lastRenewedAt = lastRenewedAt;
    }

    public boolean isRenewRequestPending() {
        return renewRequestPending;
    }

    public void setRenewRequestPending(boolean renewRequestPending) {
        this.renewRequestPending = renewRequestPending;
    }

    public Instant getRenewRequestedAt() {
        return renewRequestedAt;
    }

    public void setRenewRequestedAt(Instant renewRequestedAt) {
        this.renewRequestedAt = renewRequestedAt;
    }

    public Integer getRenewRequestedDays() {
        return renewRequestedDays;
    }

    public void setRenewRequestedDays(Integer renewRequestedDays) {
        this.renewRequestedDays = renewRequestedDays;
    }
}
