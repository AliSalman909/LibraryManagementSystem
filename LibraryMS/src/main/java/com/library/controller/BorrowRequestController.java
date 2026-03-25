package com.library.controller;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.library.entity.BorrowRecord;
import com.library.exception.BusinessRuleException;
import com.library.messages.UserFacingMessages;
import com.library.security.LibraryUserDetails;
import com.library.service.BorrowRequestService;

@Controller
public class BorrowRequestController {

    private final BorrowRequestService borrowRequestService;

    public BorrowRequestController(BorrowRequestService borrowRequestService) {
        this.borrowRequestService = borrowRequestService;
    }

    @GetMapping("/librarian/borrow-requests")
    public String pendingForLibrarian(Model model) {
        model.addAttribute("pendingRequests", borrowRequestService.listPendingForLibrarian());
        return "librarian/borrow-requests";
    }

    @PostMapping("/librarian/borrow-requests/{requestId}/approve")
    public String approve(
            @PathVariable String requestId,
            @AuthenticationPrincipal LibraryUserDetails principal,
            RedirectAttributes redirectAttributes) {
        try {
            BorrowRecord record = borrowRequestService.approve(requestId, principal.getUserId(), null);
            redirectAttributes.addFlashAttribute(
                    "flashSuccess",
                    "Borrow request approved. Due date set to " + record.getDueDate() + ".");
        } catch (BusinessRuleException ex) {
            redirectAttributes.addFlashAttribute("flashError", UserFacingMessages.orGeneric(ex.getMessage()));
        }
        return "redirect:/librarian/borrow-requests";
    }

    @PostMapping("/librarian/borrow-requests/{requestId}/reject")
    public String reject(
            @PathVariable String requestId,
            @AuthenticationPrincipal LibraryUserDetails principal,
            RedirectAttributes redirectAttributes) {
        try {
            borrowRequestService.reject(requestId, principal.getUserId());
            redirectAttributes.addFlashAttribute("flashSuccess", "Borrow request rejected.");
        } catch (BusinessRuleException ex) {
            redirectAttributes.addFlashAttribute("flashError", UserFacingMessages.orGeneric(ex.getMessage()));
        }
        return "redirect:/librarian/borrow-requests";
    }

    @GetMapping("/student/borrow-requests")
    public String studentRequests(@AuthenticationPrincipal LibraryUserDetails principal, Model model) {
        model.addAttribute("requests", borrowRequestService.listForStudent(principal.getUserId()));
        return "student/borrow-requests";
    }
}
