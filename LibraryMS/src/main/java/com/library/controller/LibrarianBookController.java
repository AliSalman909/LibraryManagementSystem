package com.library.controller;

import com.library.dto.BookForm;
import com.library.exception.BusinessRuleException;
import com.library.messages.UserFacingMessages;
import com.library.service.BookService;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/librarian/books")
public class LibrarianBookController {

    private static final Logger log = LoggerFactory.getLogger(LibrarianBookController.class);
    private final BookService bookService;

    public LibrarianBookController(BookService bookService) {
        this.bookService = bookService;
    }

    @GetMapping
    public String list(Model model) {
        model.addAttribute("books", bookService.listAll());
        if (!model.containsAttribute("bookForm")) {
            BookForm form = new BookForm();
            form.setTotalCopies(1);
            form.setFinePerDayPkr(50);
            form.setMaxBorrowDays(28);
            model.addAttribute("bookForm", form);
        }
        return "librarian/books";
    }

    @PostMapping
    public String create(
            @Valid @ModelAttribute("bookForm") BookForm bookForm,
            BindingResult bindingResult,
            Model model,
            RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            model.addAttribute("books", bookService.listAll());
            return "librarian/books";
        }
        try {
            bookService.create(bookForm);
            redirectAttributes.addFlashAttribute("flashSuccess", "Book created successfully.");
            return "redirect:/librarian/books";
        } catch (BusinessRuleException ex) {
            model.addAttribute("books", bookService.listAll());
            model.addAttribute("flashError", UserFacingMessages.orGeneric(ex.getMessage()));
            return "librarian/books";
        } catch (Exception ex) {
            log.error("Unexpected error while creating book", ex);
            model.addAttribute("books", bookService.listAll());
            model.addAttribute("flashError", UserFacingMessages.GENERIC_TRY_AGAIN);
            return "librarian/books";
        }
    }

    @GetMapping("/{bookId}/edit")
    public String editForm(@PathVariable String bookId, Model model) {
        var book = bookService.findByIdOrThrow(bookId);
        BookForm form = new BookForm();
        form.setTitle(book.getTitle());
        form.setAuthor(book.getAuthor());
        form.setCategory(book.getCategory());
        form.setTotalCopies(book.getTotalCopies());
        form.setFinePerDayPkr(book.getFinePerDayPkr());
        form.setMaxBorrowDays(book.getMaxBorrowDays());
        model.addAttribute("bookId", book.getBookId());
        model.addAttribute("bookForm", form);
        return "librarian/book-edit";
    }

    @PostMapping("/{bookId}/edit")
    public String update(
            @PathVariable String bookId,
            @Valid @ModelAttribute("bookForm") BookForm bookForm,
            BindingResult bindingResult,
            Model model,
            RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            model.addAttribute("bookId", bookId);
            return "librarian/book-edit";
        }
        try {
            bookService.update(bookId, bookForm);
            redirectAttributes.addFlashAttribute("flashSuccess", "Book updated successfully.");
            return "redirect:/librarian/books";
        } catch (BusinessRuleException ex) {
            model.addAttribute("bookId", bookId);
            model.addAttribute("flashError", UserFacingMessages.orGeneric(ex.getMessage()));
            return "librarian/book-edit";
        } catch (Exception ex) {
            log.error("Unexpected error while updating book {}", bookId, ex);
            model.addAttribute("bookId", bookId);
            model.addAttribute("flashError", UserFacingMessages.GENERIC_TRY_AGAIN);
            return "librarian/book-edit";
        }
    }

    @PostMapping("/{bookId}/delete")
    public String delete(@PathVariable String bookId, RedirectAttributes redirectAttributes) {
        try {
            bookService.delete(bookId);
            redirectAttributes.addFlashAttribute(
                    "flashSuccess",
                    "Book deleted successfully. Pending requests and reservations for this book were also removed.");
        } catch (BusinessRuleException ex) {
            redirectAttributes.addFlashAttribute("flashError", UserFacingMessages.orGeneric(ex.getMessage()));
        }
        return "redirect:/librarian/books";
    }
}
