package com.library.controller;

import com.library.entity.enums.FineStatus;
import com.library.exception.BusinessRuleException;
import com.library.messages.UserFacingMessages;
import com.library.security.LibraryUserDetails;
import com.library.service.FineService;
import java.math.BigDecimal;
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
        model.addAttribute("fines", fineService.listFinesByFilter(filter));
        model.addAttribute("unpaidFinesForReceipt", fineService.listFinesByFilter("unpaid"));
        model.addAttribute("liveOverdueLoans", fineService.listLiveOverdueLoans());
        model.addAttribute("filter", normalizeFilter(filter));
        model.addAttribute("FineStatus", FineStatus.class); // expose enum to template
        return "librarian/fines";
    }

    @PostMapping("/librarian/fines/{fineId}/status")
    public String updateStatus(
            @PathVariable String fineId,
            @RequestParam("status") FineStatus status,
            @RequestParam(name = "notes", required = false) String notes,
            @AuthenticationPrincipal LibraryUserDetails principal,
            RedirectAttributes redirectAttributes) {
        try {
            fineService.updateStatus(fineId, status, principal.getUserId(), notes, null);
            redirectAttributes.addFlashAttribute("flashSuccess", "Fine status updated.");
        } catch (BusinessRuleException ex) {
            redirectAttributes.addFlashAttribute("flashError",
                    UserFacingMessages.orGeneric(ex.getMessage()));
        }
        return "redirect:/librarian/fines?filter=" + normalizeFilter(status.name().toLowerCase());
    }

    @PostMapping("/librarian/fines/{fineId}/waived-adjustment")
    public String updateWaivedAdjustment(
            @PathVariable String fineId,
            @RequestParam(name = "waivedAdjustment", required = false) BigDecimal waivedAdjustment,
            RedirectAttributes redirectAttributes) {
        try {
            fineService.updateWaivedAdjustment(fineId, waivedAdjustment);
            redirectAttributes.addFlashAttribute("flashSuccess", "Waived adjustment updated.");
        } catch (BusinessRuleException ex) {
            redirectAttributes.addFlashAttribute("flashError",
                    UserFacingMessages.orGeneric(ex.getMessage()));
        }
        return "redirect:/librarian/fines?filter=unpaid";
    }

    @GetMapping("/librarian/fines/{fineId}/receipt")
    public String viewReceipt(@PathVariable String fineId, Model model) {
        var fine = fineService.getFineByIdWithDetails(fineId);
        model.addAttribute("fine", fine);
        model.addAttribute("receiptFileName", fine.getStudent().getUserId() + "-receipt");
        return "librarian/fine-receipt";
    }

    private String normalizeFilter(String filter) {
        if (filter == null || filter.isBlank()) {
            return "unpaid";
        }
        String lowered = filter.toLowerCase();
        return switch (lowered) {
            case "unpaid", "paid", "waived", "all" -> lowered;
            default -> "unpaid";
        };
    }

}
