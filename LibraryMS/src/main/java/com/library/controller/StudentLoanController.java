package com.library.controller;

import com.library.exception.BusinessRuleException;
import com.library.messages.UserFacingMessages;
import com.library.security.LibraryUserDetails;
import com.library.service.RenewalService;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class StudentLoanController {

    private final RenewalService renewalService;

    public StudentLoanController(RenewalService renewalService) {
        this.renewalService = renewalService;
    }

    /** Shows the student's active (unreturned) loans with renewal options. */
    @GetMapping("/student/my-loans")
    public String myLoans(
            @AuthenticationPrincipal LibraryUserDetails principal,
            Model model) {
        var activeLoans = renewalService.listActiveLoansForStudent(principal.getUserId());
        // Pre-compute renew-ability per record so the template can show/hide the button
        var canRenewMap = new java.util.HashMap<String, Boolean>();
        var renewOptionsMap = new java.util.HashMap<String, java.util.List<Integer>>();
        for (var loan : activeLoans) {
            canRenewMap.put(loan.getRecordId(), renewalService.canRenew(loan, principal.getUserId()));
            renewOptionsMap.put(loan.getRecordId(), renewalService.allowedRenewDurations(loan));
        }
        model.addAttribute("activeLoans", activeLoans);
        model.addAttribute("canRenewMap", canRenewMap);
        model.addAttribute("renewOptionsMap", renewOptionsMap);
        model.addAttribute("maxRenewals", renewalService.getMaxRenewals());
        return "student/my-loans";
    }

    /** Renews a loan by one of the allowed day options for that book. */
    @PostMapping("/student/my-loans/{recordId}/renew")
    public String renewLoan(
            @PathVariable String recordId,
            @RequestParam(name = "durationDays", defaultValue = "14") Integer durationDays,
            @AuthenticationPrincipal LibraryUserDetails principal,
            RedirectAttributes redirectAttributes) {
        try {
            var record = renewalService.renewLoan(recordId, principal.getUserId(), durationDays);
            redirectAttributes.addFlashAttribute("flashSuccess",
                    "Loan renewed! New due date: " + record.getDueDate() + ".");
        } catch (BusinessRuleException ex) {
            redirectAttributes.addFlashAttribute("flashError",
                    UserFacingMessages.orGeneric(ex.getMessage()));
        }
        return "redirect:/student/my-loans";
    }
}
