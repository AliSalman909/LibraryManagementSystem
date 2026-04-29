package com.library.dto;

import jakarta.validation.constraints.NotBlank;

public class BorrowRequestCreateRequest {

    @NotBlank(message = "bookId is required.")
    private String bookId;
    private Integer durationDays;

    public String getBookId() {
        return bookId;
    }

    public void setBookId(String bookId) {
        this.bookId = bookId;
    }

    public Integer getDurationDays() {
        return durationDays;
    }

    public void setDurationDays(Integer durationDays) {
        this.durationDays = durationDays;
    }
}
