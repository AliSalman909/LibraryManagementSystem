package com.library.controller;

import com.library.exception.BusinessRuleException;
import com.library.messages.UserFacingMessages;
import com.library.security.LibraryUserDetails;
import com.library.service.BookService;
import com.library.service.BorrowRequestService;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/student/books")
public class StudentBookController {

    private final BookService bookService;
    private final BorrowRequestService borrowRequestService;

    public StudentBookController(BookService bookService, BorrowRequestService borrowRequestService) {
        this.bookService = bookService;
        this.borrowRequestService = borrowRequestService;
    }

    @GetMapping
    public String search(@RequestParam(value = "q", required = false) String query, Model model) {
        model.addAttribute("query", query);
        model.addAttribute("books", bookService.search(query));
        return "student/books";
    }

    @PostMapping("/request")
    public String requestBorrow(
            @RequestParam("bookId") String bookId,
            @RequestParam(value = "durationDays", defaultValue = "14") Integer durationDays,
            @AuthenticationPrincipal LibraryUserDetails principal,
            RedirectAttributes redirectAttributes) {
        try {
            borrowRequestService.createPendingRequest(principal.getUserId(), bookId, durationDays);
            redirectAttributes.addFlashAttribute("flashSuccess", "Borrow request submitted.");
        } catch (BusinessRuleException ex) {
            redirectAttributes.addFlashAttribute("flashError", UserFacingMessages.orGeneric(ex.getMessage()));
        }
        return "redirect:/student/books";
    }
}
