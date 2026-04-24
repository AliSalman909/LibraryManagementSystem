package com.library.entity;

import com.library.entity.enums.FineStatus;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;
import java.math.BigDecimal;
import java.time.Instant;

/**
 * Represents an overdue fine linked to a specific BorrowRecord.
 * A fine row is only created when a book is returned late (daysLate > 0).
 * One fine per borrow record (enforced by the UNIQUE constraint on record_id).
 */
@Entity
@Table(name = "fines")
public class Fine {

    @Id
    @Column(name = "fine_id", nullable = false, length = 64)
    private String fineId;

    /** The borrow record that triggered this fine (one-to-one: one fine per return). */
    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "record_id", nullable = false, referencedColumnName = "record_id", unique = true)
    private BorrowRecord borrowRecord;

    /** Denormalised student FK for fast student-facing queries without joining borrow_records. */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "student_id", nullable = false, referencedColumnName = "user_id")
    private Student student;

    /** Calculated as daysLate × perDayRate at the moment of return. */
    @Column(name = "amount", nullable = false, precision = 10, scale = 2)
    private BigDecimal amount;

    /** Number of calendar days past the due date. Always ≥ 1 for a persisted fine. */
    @Column(name = "days_late", nullable = false)
    private int daysLate;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 16)
    private FineStatus status = FineStatus.UNPAID;

    /** Timestamp when the fine was created (at return time). */
    @Column(name = "issued_at", nullable = false)
    private Instant issuedAt;

    /** Timestamp when the fine was marked PAID or WAIVED. Null while UNPAID. */
    @Column(name = "resolved_at")
    private Instant resolvedAt;

    /** Librarian who marked the fine as PAID or WAIVED. Null while UNPAID. */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "resolved_by_librarian_id", referencedColumnName = "user_id")
    private Librarian resolvedBy;

    /** Optional librarian note added on resolution (e.g. reason for waiver). */
    @Column(name = "notes", length = 500)
    private String notes;

    /** Amount adjusted/waived from the original fine (PKR). */
    @Column(name = "waived_amount", nullable = false, precision = 10, scale = 2)
    private BigDecimal waivedAmount = BigDecimal.ZERO;

    // -----------------------------------------------------------------------
    // Getters & setters
    // -----------------------------------------------------------------------

    public String getFineId() {
        return fineId;
    }

    public void setFineId(String fineId) {
        this.fineId = fineId;
    }

    public BorrowRecord getBorrowRecord() {
        return borrowRecord;
    }

    public void setBorrowRecord(BorrowRecord borrowRecord) {
        this.borrowRecord = borrowRecord;
    }

    public Student getStudent() {
        return student;
    }

    public void setStudent(Student student) {
        this.student = student;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public int getDaysLate() {
        return daysLate;
    }

    public void setDaysLate(int daysLate) {
        this.daysLate = daysLate;
    }

    public FineStatus getStatus() {
        return status;
    }

    public void setStatus(FineStatus status) {
        this.status = status;
    }

    public Instant getIssuedAt() {
        return issuedAt;
    }

    public void setIssuedAt(Instant issuedAt) {
        this.issuedAt = issuedAt;
    }

    public Instant getResolvedAt() {
        return resolvedAt;
    }

    public void setResolvedAt(Instant resolvedAt) {
        this.resolvedAt = resolvedAt;
    }

    public Librarian getResolvedBy() {
        return resolvedBy;
    }

    public void setResolvedBy(Librarian resolvedBy) {
        this.resolvedBy = resolvedBy;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    public BigDecimal getWaivedAmount() {
        return waivedAmount;
    }

    public void setWaivedAmount(BigDecimal waivedAmount) {
        this.waivedAmount = waivedAmount;
    }

    public BigDecimal getNetAmount() {
        BigDecimal waived = waivedAmount == null ? BigDecimal.ZERO : waivedAmount;
        BigDecimal net = amount.subtract(waived);
        return net.max(BigDecimal.ZERO);
    }

    @Transient
    public long getReceiptIdNumeric() {
        if (fineId == null) {
            return 0L;
        }
        long hash = Integer.toUnsignedLong(fineId.hashCode());
        return 100000000L + (hash % 900000000L);
    }
}
