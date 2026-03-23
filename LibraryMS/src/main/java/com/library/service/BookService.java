package com.library.service;

import com.library.dto.BookForm;
import com.library.entity.Book;
import com.library.entity.BookCopy;
import com.library.exception.BusinessRuleException;
import com.library.repository.BookCopyRepository;
import com.library.repository.BookRepository;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

@Service
public class BookService {

    private final BookRepository bookRepository;
    private final BookCopyRepository bookCopyRepository;

    public BookService(BookRepository bookRepository, BookCopyRepository bookCopyRepository) {
        this.bookRepository = bookRepository;
        this.bookCopyRepository = bookCopyRepository;
    }

    @Transactional(readOnly = true)
    public List<Book> listAll() {
        return bookRepository.findAll().stream().sorted((a, b) -> a.getTitle().compareToIgnoreCase(b.getTitle())).toList();
    }

    @Transactional(readOnly = true)
    public List<Book> search(String query) {
        if (!StringUtils.hasText(query)) {
            return listAll();
        }
        return bookRepository.search(query.trim());
    }

    @Transactional(readOnly = true)
    public List<Book> searchByTitleAuthor(String title, String author) {
        String normalizedTitle = normalizeOptional(title);
        String normalizedAuthor = normalizeOptional(author);
        if (normalizedTitle == null && normalizedAuthor == null) {
            return listAll();
        }
        return bookRepository.searchByTitleAndAuthor(normalizedTitle, normalizedAuthor);
    }

    @Transactional(readOnly = true)
    public Book findByIdOrThrow(String bookId) {
        return bookRepository.findById(bookId).orElseThrow(() -> new BusinessRuleException("Book not found."));
    }

    @Transactional
    public Book create(BookForm form) {
        Book book = new Book();
        book.setBookId(UUID.randomUUID().toString());
        book.setTitle(form.getTitle().trim());
        book.setAuthor(form.getAuthor().trim());
        book.setCategory(form.getCategory().trim());
        String baseIsbn = generateUniqueBaseIsbn(book.getTitle(), book.getCategory());
        book.setIsbn(baseIsbn);
        book.setTotalCopies(form.getTotalCopies());
        book.setAvailableCopies(form.getTotalCopies());
        book.setCreatedAt(Instant.now());
        book.setUpdatedAt(Instant.now());
        Book saved = bookRepository.save(book);
        createCopies(saved, form.getTotalCopies(), 1);
        return saved;
    }

    @Transactional
    public Book update(String bookId, BookForm form) {
        Book book = findByIdOrThrow(bookId);
        int activeBorrowedCopies = Math.max(0, book.getTotalCopies() - book.getAvailableCopies());
        if (form.getTotalCopies() < activeBorrowedCopies) {
            throw new BusinessRuleException("Total copies cannot be less than currently borrowed copies.");
        }
        String newCategory = form.getCategory().trim();
        if (!book.getCategory().equalsIgnoreCase(newCategory)) {
            throw new BusinessRuleException(
                    "Category is part of generated ISBN codes and cannot be changed after creation.");
        }

        int previousTotalCopies = book.getTotalCopies();
        book.setTitle(form.getTitle().trim());
        book.setAuthor(form.getAuthor().trim());
        book.setCategory(newCategory);
        book.setTotalCopies(form.getTotalCopies());
        int newAvailableCopies = form.getTotalCopies() - activeBorrowedCopies;
        book.setAvailableCopies(newAvailableCopies);
        book.setUpdatedAt(Instant.now());
        Book saved = bookRepository.save(book);
        syncCopiesForTotal(saved, previousTotalCopies, form.getTotalCopies());
        return saved;
    }

    @Transactional
    public void delete(String bookId) {
        Book book = findByIdOrThrow(bookId);
        if (book.getAvailableCopies() < book.getTotalCopies()) {
            throw new BusinessRuleException("Cannot delete a book that is currently borrowed.");
        }
        List<BookCopy> copies = bookCopyRepository.findByBookBookIdOrderByCopyNumberAsc(bookId);
        if (!copies.isEmpty()) {
            bookCopyRepository.deleteAll(copies);
        }
        bookRepository.delete(book);
    }

    private void syncCopiesForTotal(Book book, int oldTotal, int newTotal) {
        if (newTotal == oldTotal) {
            return;
        }
        if (newTotal > oldTotal) {
            createCopies(book, newTotal - oldTotal, oldTotal + 1);
            return;
        }
        int toRemove = oldTotal - newTotal;
        List<BookCopy> copies = new ArrayList<>(bookCopyRepository.findByBookBookIdOrderByCopyNumberAsc(book.getBookId()));
        copies.sort(Comparator.comparingInt(BookCopy::getCopyNumber).reversed());
        List<BookCopy> removable = copies.stream().filter(BookCopy::isAvailable).limit(toRemove).toList();
        if (removable.size() < toRemove) {
            throw new BusinessRuleException("Cannot reduce copies because some copies are currently borrowed.");
        }
        bookCopyRepository.deleteAll(removable);
    }

    private void createCopies(Book book, int count, int startCopyNumber) {
        List<BookCopy> copies = new ArrayList<>(count);
        for (int i = 0; i < count; i++) {
            int copyNumber = startCopyNumber + i;
            BookCopy copy = new BookCopy();
            copy.setCopyId(UUID.randomUUID().toString());
            copy.setBook(book);
            copy.setCopyNumber(copyNumber);
            copy.setAvailable(true);
            copy.setIsbnCode(generateUniqueCopyIsbn(book.getIsbn(), copyNumber));
            copies.add(copy);
        }
        bookCopyRepository.saveAll(copies);
    }

    private String generateUniqueBaseIsbn(String title, String category) {
        String titlePart = normalizeToken(title, 6);
        String categoryPart = normalizeToken(category, 4);
        for (int attempt = 0; attempt < 25; attempt++) {
            String suffix = UUID.randomUUID().toString().substring(0, 6).toUpperCase();
            String candidate = titlePart + "-" + categoryPart + "-" + suffix;
            if (bookRepository.findByIsbn(candidate).isEmpty()) {
                return candidate;
            }
        }
        throw new BusinessRuleException("Could not generate a unique ISBN. Please try again.");
    }

    private String generateUniqueCopyIsbn(String baseIsbn, int copyNumber) {
        String candidate = baseIsbn + "-C" + String.format("%03d", copyNumber);
        if (!bookCopyRepository.existsByIsbnCode(candidate)) {
            return candidate;
        }
        String fallback = candidate + "-" + UUID.randomUUID().toString().substring(0, 4).toUpperCase();
        if (!bookCopyRepository.existsByIsbnCode(fallback)) {
            return fallback;
        }
        throw new BusinessRuleException("Could not generate unique copy ISBN code.");
    }

    private static String normalizeToken(String value, int maxLen) {
        String cleaned = value == null ? "" : value.replaceAll("[^A-Za-z0-9]", "").toUpperCase();
        if (cleaned.isBlank()) {
            cleaned = "X";
        }
        return cleaned.substring(0, Math.min(maxLen, cleaned.length()));
    }

    private static String normalizeOptional(String value) {
        return StringUtils.hasText(value) ? value.trim() : null;
    }
}
