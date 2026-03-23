package com.library.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class BookForm {

    @NotBlank(message = "Title is required.")
    @Size(max = 200, message = "Title must be at most 200 characters.")
    private String title;

    @NotBlank(message = "Author is required.")
    @Size(max = 150, message = "Author must be at most 150 characters.")
    private String author;

    @NotBlank(message = "Category is required.")
    @Size(max = 100, message = "Category must be at most 100 characters.")
    private String category;

    @Min(value = 1, message = "Total copies must be at least 1.")
    private int totalCopies;

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
}
