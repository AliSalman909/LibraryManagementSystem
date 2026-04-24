package com.library.controller;

import com.library.entity.Fine;
import com.library.exception.BusinessRuleException;
import com.library.messages.UserFacingMessages;
import com.library.security.LibraryUserDetails;
import com.library.service.ReturnService;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class LibrarianReturnController {

    private final ReturnService returnService;

    public LibrarianReturnController(ReturnService returnService) {
        this.returnService = returnService;
    }

    /** Lists all active (unreturned) loans. */
    @GetMapping("/librarian/active-loans")
    public String activeLoans(Model model) {
        var recentlyReturned = returnService.listReturnsForUndoSection();
        var undoAllowed = new java.util.HashMap<String, Boolean>();
        var fineStatusByRecord = new java.util.HashMap<String, String>();
        var fineAmountByRecord = new java.util.HashMap<String, java.math.BigDecimal>();
        var undoMessageByRecord = new java.util.HashMap<String, String>();
        for (var loan : recentlyReturned) {
            String lockReason = returnService.explainUndoLockReason(loan);
            boolean undoAllowedNow = lockReason == null;
            undoAllowed.put(loan.getRecordId(), undoAllowedNow);
            fineStatusByRecord.put(loan.getRecordId(), returnService.fineStatusLabel(loan));
            fineAmountByRecord.put(loan.getRecordId(), returnService.fineAmountForLoan(loan));
            if (undoAllowedNow) {
                boolean overdueIfUndone = loan.getDueDate().isBefore(java.time.LocalDate.now());
                String hint = overdueIfUndone
                        ? "If undone: moves to Live Overdue Books."
                        : "If undone: moves to Active Loans.";
                undoMessageByRecord.put(loan.getRecordId(), hint);
            } else {
                undoMessageByRecord.put(loan.getRecordId(), lockReason);
            }
        }
        model.addAttribute("activeLoans", returnService.listActiveLoans());
        model.addAttribute("recentlyReturnedLoans", recentlyReturned);
        model.addAttribute("undoAllowedByRecord", undoAllowed);
        model.addAttribute("fineStatusByRecord", fineStatusByRecord);
        model.addAttribute("fineAmountByRecord", fineAmountByRecord);
        model.addAttribute("undoMessageByRecord", undoMessageByRecord);
        return "librarian/active-loans";
    }

    /** Processes a book return, generating a fine if overdue. */
    @PostMapping("/librarian/active-loans/{recordId}/return")
    public String returnBook(
            @PathVariable String recordId,
            @AuthenticationPrincipal LibraryUserDetails principal,
            RedirectAttributes redirectAttributes) {
        try {
            Fine fine = returnService.returnBook(recordId, principal.getUserId());
            if (fine == null) {
                redirectAttributes.addFlashAttribute("flashSuccess",
                        "Book returned successfully. No overdue fine.");
            } else {
                redirectAttributes.addFlashAttribute("flashSuccess",
                        String.format("Book returned. Overdue by %d day(s). Fine of PKR %.2f issued.",
                                fine.getDaysLate(), fine.getAmount()));
            }
        } catch (BusinessRuleException ex) {
            redirectAttributes.addFlashAttribute("flashError",
                    UserFacingMessages.orGeneric(ex.getMessage()));
        }
        return "redirect:/librarian/active-loans";
    }

    @PostMapping("/librarian/active-loans/{recordId}/undo-return")
    public String undoReturn(
            @PathVariable String recordId,
            @AuthenticationPrincipal LibraryUserDetails principal,
            RedirectAttributes redirectAttributes) {
        try {
            returnService.undoReturn(recordId, principal.getUserId());
            redirectAttributes.addFlashAttribute("flashSuccess", "Return has been undone successfully.");
        } catch (BusinessRuleException ex) {
            redirectAttributes.addFlashAttribute("flashError",
                    UserFacingMessages.orGeneric(ex.getMessage()));
        }
        return "redirect:/librarian/active-loans";
    }
}
