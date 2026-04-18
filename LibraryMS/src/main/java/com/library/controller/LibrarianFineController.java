package com.library.controller;

import com.library.entity.enums.FineStatus;
import com.library.exception.BusinessRuleException;
import com.library.messages.UserFacingMessages;
import com.library.security.LibraryUserDetails;
import com.library.service.FineService;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class LibrarianFineController {

    private final FineService fineService;

    public LibrarianFineController(FineService fineService) {
        this.fineService = fineService;
    }

    /**
     * Lists fines. Supports a {@code filter} query parameter:
     *   - {@code unpaid} → only UNPAID fines (default)
     *   - {@code all}    → all fines (full history)
     */
    @GetMapping("/librarian/fines")
    public String listFines(
            @RequestParam(name = "filter", defaultValue = "unpaid") String filter,
            Model model) {
        boolean showAll = "all".equalsIgnoreCase(filter);
        model.addAttribute("fines", showAll ? fineService.listAllFines() : fineService.listUnpaidFines());
        model.addAttribute("filter", showAll ? "all" : "unpaid");
        model.addAttribute("FineStatus", FineStatus.class); // expose enum to template
        return "librarian/fines";
    }

    /** Mark a fine as PAID. */
    @PostMapping("/librarian/fines/{fineId}/mark-paid")
    public String markPaid(
            @PathVariable String fineId,
            @RequestParam(name = "notes", required = false) String notes,
            @AuthenticationPrincipal LibraryUserDetails principal,
            RedirectAttributes redirectAttributes) {
        try {
            fineService.markPaid(fineId, principal.getUserId(), notes);
            redirectAttributes.addFlashAttribute("flashSuccess", "Fine marked as paid.");
        } catch (BusinessRuleException ex) {
            redirectAttributes.addFlashAttribute("flashError",
                    UserFacingMessages.orGeneric(ex.getMessage()));
        }
        return "redirect:/librarian/fines";
    }

    /** Waive a fine. */
    @PostMapping("/librarian/fines/{fineId}/waive")
    public String waive(
            @PathVariable String fineId,
            @RequestParam(name = "notes", required = false) String notes,
            @AuthenticationPrincipal LibraryUserDetails principal,
            RedirectAttributes redirectAttributes) {
        try {
            fineService.waive(fineId, principal.getUserId(), notes);
            redirectAttributes.addFlashAttribute("flashSuccess", "Fine waived.");
        } catch (BusinessRuleException ex) {
            redirectAttributes.addFlashAttribute("flashError",
                    UserFacingMessages.orGeneric(ex.getMessage()));
        }
        return "redirect:/librarian/fines";
    }
}
