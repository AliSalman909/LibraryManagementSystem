package com.library.entity;

import com.library.entity.enums.ReservationStatus;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import java.time.Instant;

/**
 * Represents a student's place in a FIFO waitlist for an unavailable book.
 * <p>Lifecycle: PENDING → READY (when a copy is returned) → FULFILLED / EXPIRED / CANCELLED.</p>
 */
@Entity
@Table(name = "reservations", uniqueConstraints = {
        @UniqueConstraint(name = "uq_reservation_student_book_active",
                columnNames = {"student_id", "book_id", "status"})
})
public class Reservation {

    @Id
    @Column(name = "reservation_id", nullable = false, length = 64)
    private String reservationId;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "student_id", nullable = false, referencedColumnName = "user_id")
    private Student student;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "book_id", nullable = false, referencedColumnName = "book_id")
    private Book book;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 16)
    private ReservationStatus status = ReservationStatus.PENDING;

    /** Position in the FIFO queue for this book (1-based). */
    @Column(name = "queue_position", nullable = false)
    private int queuePosition;

    /** Requested loan duration in days when this reservation is fulfilled. */
    @Column(name = "requested_duration_days", nullable = false)
    private int requestedDurationDays;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    /** Set when a copy becomes available and the reservation moves to READY. */
    @Column(name = "notified_at")
    private Instant notifiedAt;

    /** Deadline by which the student must pick up the book (READY + pickup window). */
    @Column(name = "expires_at")
    private Instant expiresAt;

    /** Set when the reservation is fulfilled (book issued to student). */
    @Column(name = "fulfilled_at")
    private Instant fulfilledAt;

    // -----------------------------------------------------------------------
    // Getters & setters
    // -----------------------------------------------------------------------

    public String getReservationId() { return reservationId; }
    public void setReservationId(String reservationId) { this.reservationId = reservationId; }

    public Student getStudent() { return student; }
    public void setStudent(Student student) { this.student = student; }

    public Book getBook() { return book; }
    public void setBook(Book book) { this.book = book; }

    public ReservationStatus getStatus() { return status; }
    public void setStatus(ReservationStatus status) { this.status = status; }

    public int getQueuePosition() { return queuePosition; }
    public void setQueuePosition(int queuePosition) { this.queuePosition = queuePosition; }

    public int getRequestedDurationDays() { return requestedDurationDays; }
    public void setRequestedDurationDays(int requestedDurationDays) { this.requestedDurationDays = requestedDurationDays; }

    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }

    public Instant getNotifiedAt() { return notifiedAt; }
    public void setNotifiedAt(Instant notifiedAt) { this.notifiedAt = notifiedAt; }

    public Instant getExpiresAt() { return expiresAt; }
    public void setExpiresAt(Instant expiresAt) { this.expiresAt = expiresAt; }

    public Instant getFulfilledAt() { return fulfilledAt; }
    public void setFulfilledAt(Instant fulfilledAt) { this.fulfilledAt = fulfilledAt; }
}
