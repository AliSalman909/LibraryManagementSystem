package com.library.controller;

import com.library.dto.StudentUnpaidReceiptRow;
import com.library.entity.Fine;
import com.library.entity.enums.FineStatus;
import com.library.exception.BusinessRuleException;
import com.library.messages.UserFacingMessages;
import com.library.security.LibraryUserDetails;
import com.library.service.FineService;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
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
        List<Fine> unpaid = fineService.listFinesByFilter("unpaid");
        Map<String, List<Fine>> byStudent =
                unpaid.stream().collect(Collectors.groupingBy(f -> f.getStudent().getUserId()));
        List<StudentUnpaidReceiptRow> studentReceiptRows = new ArrayList<>();
        for (Map.Entry<String, List<Fine>> e : byStudent.entrySet()) {
            List<Fine> lines = new ArrayList<>(e.getValue());
            lines.sort(Comparator.comparing(Fine::getIssuedAt));
            Fine first = lines.get(0);
            BigDecimal totalAmount = BigDecimal.ZERO;
            BigDecimal totalWaived = BigDecimal.ZERO;
            for (Fine f : lines) {
                totalAmount = totalAmount.add(f.getAmount());
                totalWaived = totalWaived.add(f.getWaivedAmount() != null ? f.getWaivedAmount() : BigDecimal.ZERO);
            }
            BigDecimal totalNet =
                    lines.stream().map(Fine::getNetAmount).reduce(BigDecimal.ZERO, BigDecimal::add);
            long rid = StudentUnpaidReceiptRow.combinedReceiptId(first.getStudent().getUserId());
            studentReceiptRows.add(
                    new StudentUnpaidReceiptRow(
                            first.getStudent().getUserId(),
                            first.getStudent().getUser().getFullName(),
                            first.getStudent().getUser().getEmail(),
                            lines.size(),
                            totalAmount,
                            totalWaived,
                            totalNet,
                            rid,
                            lines));
        }
        studentReceiptRows.sort(Comparator.comparing(StudentUnpaidReceiptRow::fullName, String.CASE_INSENSITIVE_ORDER));
        model.addAttribute("studentUnpaidReceiptRows", studentReceiptRows);
        model.addAttribute("liveOverdueLoans", fineService.listLiveOverdueLoans());
        model.addAttribute("today", LocalDate.now());
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

    /** Legacy per-fine URL → combined student receipt. */
    @GetMapping("/librarian/fines/{fineId}/receipt")
    public String viewReceipt(@PathVariable String fineId) {
        var fine = fineService.getFineByIdWithDetails(fineId);
        return "redirect:/librarian/fines/receipt/student/" + fine.getStudent().getUserId();
    }

    @GetMapping("/librarian/fines/receipt/student/{studentUserId}")
    public String viewStudentReceipt(
            @PathVariable String studentUserId, Model model, RedirectAttributes redirectAttributes) {
        List<Fine> lines = fineService.listUnpaidWithDetailsForStudent(studentUserId);
        if (lines.isEmpty()) {
            redirectAttributes.addFlashAttribute("flashError", "No unpaid fines for this student.");
            return "redirect:/librarian/fines?filter=unpaid";
        }
        Fine first = lines.get(0);
        BigDecimal totalAmount = BigDecimal.ZERO;
        BigDecimal totalWaived = BigDecimal.ZERO;
        for (Fine f : lines) {
            totalAmount = totalAmount.add(f.getAmount());
            totalWaived = totalWaived.add(f.getWaivedAmount() != null ? f.getWaivedAmount() : BigDecimal.ZERO);
        }
        BigDecimal totalNet = lines.stream().map(Fine::getNetAmount).reduce(BigDecimal.ZERO, BigDecimal::add);
        long rid = StudentUnpaidReceiptRow.combinedReceiptId(studentUserId);
        model.addAttribute(
                "receiptRow",
                new StudentUnpaidReceiptRow(
                        first.getStudent().getUserId(),
                        first.getStudent().getUser().getFullName(),
                        first.getStudent().getUser().getEmail(),
                        lines.size(),
                        totalAmount,
                        totalWaived,
                        totalNet,
                        rid,
                        lines));
        model.addAttribute("receiptFileName", studentUserId + "-receipt");
        return "librarian/fine-receipt-student";
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
