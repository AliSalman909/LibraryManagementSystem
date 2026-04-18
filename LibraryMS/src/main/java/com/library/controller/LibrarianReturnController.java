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
        model.addAttribute("activeLoans", returnService.listActiveLoans());
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
}
