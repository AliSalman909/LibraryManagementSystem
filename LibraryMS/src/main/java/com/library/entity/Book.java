package com.library.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import java.util.ArrayList;
import java.util.List;
import java.time.Instant;
import java.math.BigDecimal;

@Entity
@Table(
        name = "books",
        uniqueConstraints = {
            @UniqueConstraint(
                    name = "uq_books_title_author_category",
                    columnNames = {"title", "author", "category"})
        })
public class Book {

    /** Plain 5-character ID shown to users (no UUID). */
    public static final int PLAIN_BOOK_ID_LENGTH = 5;

    /** Unambiguous characters for 5-char ids. */
    public static final String PLAIN_BOOK_ID_ALPHABET = "ABCDEFGHJKLMNPQRSTUVWXYZ23456789";

    @Id
    @Column(name = "book_id", nullable = false, length = PLAIN_BOOK_ID_LENGTH)
    private String bookId;

    @Column(nullable = false, length = 200)
    private String title;

    @Column(nullable = false, length = 150)
    private String author;

    /** Auto-generated base ISBN from title + category (always set for new/updated records). */
    @Column(length = 64, unique = true)
    private String isbn;

    @Column(length = 100)
    private String category;

    @Column(name = "total_copies", nullable = false)
    private int totalCopies;

    @Column(name = "available_copies", nullable = false)
    private int availableCopies;

    @Column(name = "fine_per_day_pkr", nullable = false)
    private int finePerDayPkr = 50;

    /**
     * Legacy column kept for existing schemas.
     * Mirrors finePerDayPkr (1 PKR == 1.00 in this column).
     */
    @Column(name = "fine_per_day", nullable = false, precision = 10, scale = 2)
    private BigDecimal finePerDay = BigDecimal.valueOf(50);

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    @OneToMany(mappedBy = "book")
    private List<BorrowRequest> borrowRequests = new ArrayList<>();

    @OneToMany(mappedBy = "book")
    private List<BookCopy> copies = new ArrayList<>();

    public String getBookId() {
        return bookId;
    }

    public void setBookId(String bookId) {
        this.bookId = bookId;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public String getIsbn() {
        return isbn;
    }

    public void setIsbn(String isbn) {
        this.isbn = isbn;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public int getTotalCopies() {
        return totalCopies;
    }

    public void setTotalCopies(int totalCopies) {
        this.totalCopies = totalCopies;
    }

    public int getAvailableCopies() {
        return availableCopies;
    }

    public void setAvailableCopies(int availableCopies) {
        this.availableCopies = availableCopies;
    }

    public int getFinePerDayPkr() {
        return finePerDayPkr;
    }

    public void setFinePerDayPkr(int finePerDayPkr) {
        this.finePerDayPkr = finePerDayPkr;
    }

    public BigDecimal getFinePerDay() {
        return finePerDay;
    }

    public void setFinePerDay(BigDecimal finePerDay) {
        this.finePerDay = finePerDay;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(Instant updatedAt) {
        this.updatedAt = updatedAt;
    }

    public List<BorrowRequest> getBorrowRequests() {
        return borrowRequests;
    }

    public void setBorrowRequests(List<BorrowRequest> borrowRequests) {
        this.borrowRequests = borrowRequests;
    }

    public List<BookCopy> getCopies() {
        return copies;
    }

    public void setCopies(List<BookCopy> copies) {
        this.copies = copies;
    }
}
