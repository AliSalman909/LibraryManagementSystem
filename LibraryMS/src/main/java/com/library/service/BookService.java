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
import java.util.Objects;
import java.math.BigDecimal;
import jakarta.annotation.PostConstruct;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

@Service
public class BookService {

    private final BookRepository bookRepository;
    private final BookCopyRepository bookCopyRepository;

    /** Five unambiguous characters shown to librarians. */
    private static final String PLAIN_BOOK_ID_ALPHABET = Book.PLAIN_BOOK_ID_ALPHABET;
    private static final int PLAIN_BOOK_ID_LENGTH = Book.PLAIN_BOOK_ID_LENGTH;
    private static final int DEFAULT_FINE_PER_DAY_PKR = 50;

    public BookService(BookRepository bookRepository, BookCopyRepository bookCopyRepository) {
        this.bookRepository = bookRepository;
        this.bookCopyRepository = bookCopyRepository;
    }

    @PostConstruct
    @Transactional
    public void backfillDefaultFinePerDay() {
        bookRepository.backfillMissingFinePerDay(DEFAULT_FINE_PER_DAY_PKR);
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

    @SuppressWarnings("null")
    @Transactional(readOnly = true)
    public Book findByIdOrThrow(String bookId) {
        return bookRepository.findById(bookId).orElseThrow(() -> new BusinessRuleException("Book not found."));
    }

    @Transactional
    public Book create(BookForm form) {
        String title = form.getTitle().trim();
        String author = form.getAuthor().trim();
        String category = form.getCategory().trim();
        ensureNoDuplicateBook(null, title, author, category);

        Book book = new Book();
        book.setBookId(generateUniquePlainBookId());
        book.setTitle(title);
        book.setAuthor(author);
        book.setCategory(category);
        String baseIsbn = generateUniqueBaseIsbn(book.getTitle(), book.getCategory());
        book.setIsbn(baseIsbn);
        book.setTotalCopies(form.getTotalCopies());
        book.setAvailableCopies(form.getTotalCopies());
        int finePerDayPkr = requireFinePerDay(bookFormFine(form));
        book.setFinePerDayPkr(finePerDayPkr);
        book.setFinePerDay(BigDecimal.valueOf(finePerDayPkr));
        book.setCreatedAt(Instant.now());
        book.setUpdatedAt(Instant.now());
        Book saved = bookRepository.save(book);
        createCopies(saved, form.getTotalCopies(), 1);
        return saved;
    }

    private String generateUniquePlainBookId() {
        // 36^5 is large enough for typical library catalogs; we still check for safety.
        for (int attempt = 0; attempt < 500; attempt++) {
            StringBuilder sb = new StringBuilder(PLAIN_BOOK_ID_LENGTH);
            for (int i = 0; i < PLAIN_BOOK_ID_LENGTH; i++) {
                sb.append(PLAIN_BOOK_ID_ALPHABET.charAt((int) (Math.random() * PLAIN_BOOK_ID_ALPHABET.length())));
            }
            String candidate = sb.toString();
            if (!bookRepository.existsById(Objects.requireNonNull(candidate))) {
                return candidate;
            }
        }
        throw new BusinessRuleException("Could not allocate a unique book id. Please try again.");
    }

    @Transactional
    public Book update(String bookId, BookForm form) {
        Book book =
                bookRepository
                        .findByIdForUpdate(bookId)
                        .orElseThrow(() -> new BusinessRuleException("Book not found."));
        int activeBorrowedCopies = Math.max(0, book.getTotalCopies() - book.getAvailableCopies());
        if (form.getTotalCopies() < activeBorrowedCopies) {
            throw new BusinessRuleException("Total copies cannot be less than currently borrowed copies.");
        }
        String newTitle = form.getTitle().trim();
        String newAuthor = form.getAuthor().trim();
        String newCategory = form.getCategory().trim();
        ensureNoDuplicateBook(bookId, newTitle, newAuthor, newCategory);

        int previousTotalCopies = book.getTotalCopies();

        // Changing category/title changes the base ISBN, so we must regenerate it and refresh all copy ISBN codes.
        String newBaseIsbn = generateUniqueBaseIsbn(newTitle, newCategory);

        book.setTitle(newTitle);
        book.setAuthor(newAuthor);
        book.setCategory(newCategory);
        book.setIsbn(newBaseIsbn);
        book.setTotalCopies(form.getTotalCopies());
        int finePerDayPkr = requireFinePerDay(bookFormFine(form));
        book.setFinePerDayPkr(finePerDayPkr);
        book.setFinePerDay(BigDecimal.valueOf(finePerDayPkr));
        int newAvailableCopies = form.getTotalCopies() - activeBorrowedCopies;
        book.setAvailableCopies(newAvailableCopies);
        book.setUpdatedAt(Instant.now());
        Book saved = bookRepository.save(book);
        syncCopiesForTotal(saved, previousTotalCopies, form.getTotalCopies());
        refreshCopiesIsbnCodes(saved);
        return saved;
    }

    private void refreshCopiesIsbnCodes(Book book) {
        // Refresh ISBN codes for every physical copy to keep them consistent with the updated base ISBN.
        List<BookCopy> copies =
                new ArrayList<>(
                        bookCopyRepository.findAllCopiesForUpdateOrderByCopyNumberAsc(book.getBookId()));
        for (BookCopy copy : copies) {
            copy.setIsbnCode(generateUniqueCopyIsbn(book.getIsbn(), copy.getCopyNumber()));
        }
        bookCopyRepository.saveAll(copies);
    }

    /**
     * Prevent duplicate books: same Title + Author + Category (case-insensitive).
     *
     * @param currentBookId the book being updated, or null for create.
     */
    private void ensureNoDuplicateBook(String currentBookId, String title, String author, String category) {
        bookRepository
                .findByTitleIgnoreCaseAndAuthorIgnoreCaseAndCategoryIgnoreCase(title, author, category)
                .ifPresent(
                        existing -> {
                            if (currentBookId == null || !existing.getBookId().equals(currentBookId)) {
                                throw new BusinessRuleException(
                                        "A book with the same title, author, and category already exists. Please edit the existing book instead of creating a duplicate.");
                            }
                        });
    }

    @Transactional
    public void delete(String bookId) {
        Book book =
                bookRepository
                        .findByIdForUpdate(bookId)
                        .orElseThrow(() -> new BusinessRuleException("Book not found."));
        if (book.getAvailableCopies() < book.getTotalCopies()) {
            throw new BusinessRuleException("Cannot delete a book that is currently borrowed.");
        }
        List<BookCopy> copies = bookCopyRepository.findAllCopiesForUpdateOrderByCopyNumberAsc(bookId);
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
        List<BookCopy> copies =
                new ArrayList<>(bookCopyRepository.findAllCopiesForUpdateOrderByCopyNumberAsc(book.getBookId()));
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

    private int bookFormFine(BookForm form) {
        if (form.getFinePerDayPkr() == null) {
            throw new BusinessRuleException("Fine per day is required.");
        }
        return form.getFinePerDayPkr();
    }

    private int requireFinePerDay(int finePerDayPkr) {
        if (finePerDayPkr < 1) {
            throw new BusinessRuleException("Fine per day must be at least 1 PKR.");
        }
        return finePerDayPkr;
    }
}
