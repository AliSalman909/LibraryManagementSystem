package com.library.controller;

import com.library.dto.BookForm;
import com.library.dto.BookResponse;
import com.library.entity.Book;
import com.library.exception.BusinessRuleException;
import com.library.service.BookService;
import jakarta.validation.Valid;
import java.util.List;
import java.util.Map;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/books")
public class BookController {

    private final BookService bookService;

    public BookController(BookService bookService) {
        this.bookService = bookService;
    }

    @PostMapping
    @PreAuthorize("hasRole('LIBRARIAN')")
    public ResponseEntity<BookResponse> addBook(@Valid @RequestBody BookForm request) {
        Book created = bookService.create(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(toResponse(created));
    }

    @GetMapping
    @PreAuthorize("hasRole('LIBRARIAN')")
    public List<BookResponse> getAllBooks() {
        return bookService.listAll().stream().map(BookController::toResponse).toList();
    }

    @GetMapping("/search")
    @PreAuthorize("hasAnyRole('LIBRARIAN','STUDENT')")
    public List<BookResponse> searchBooks(
            @RequestParam(value = "title", required = false) String title,
            @RequestParam(value = "author", required = false) String author) {
        return bookService.searchByTitleAuthor(title, author).stream().map(BookController::toResponse).toList();
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('LIBRARIAN')")
    public BookResponse updateBook(@PathVariable("id") String id, @Valid @RequestBody BookForm request) {
        Book updated = bookService.update(id, request);
        return toResponse(updated);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('LIBRARIAN')")
    public ResponseEntity<Void> deleteBook(@PathVariable("id") String id) {
        bookService.delete(id);
        return ResponseEntity.noContent().build();
    }

    @ExceptionHandler(BusinessRuleException.class)
    public ResponseEntity<Map<String, String>> handleBusinessRule(BusinessRuleException ex) {
        return ResponseEntity.badRequest().body(Map.of("error", ex.getMessage()));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, String>> handleValidation(MethodArgumentNotValidException ex) {
        String message = ex.getBindingResult().getFieldErrors().stream()
                .findFirst()
                .map(error -> error.getField() + ": " + error.getDefaultMessage())
                .orElse("Invalid request body.");
        return ResponseEntity.badRequest().body(Map.of("error", message));
    }

    private static BookResponse toResponse(Book book) {
        BookResponse response = new BookResponse();
        response.setId(book.getBookId());
        response.setTitle(book.getTitle());
        response.setAuthor(book.getAuthor());
        response.setIsbn(book.getIsbn());
        response.setCategory(book.getCategory());
        response.setTotalCopies(book.getTotalCopies());
        response.setAvailableCopies(book.getAvailableCopies());
        response.setFinePerDayPkr(book.getFinePerDayPkr());
        response.setMaxBorrowDays(book.getMaxBorrowDays());
        return response;
    }
}
