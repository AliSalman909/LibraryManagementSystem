package com.library.controller;

import com.library.exception.BusinessRuleException;
import com.library.messages.UserFacingMessages;
import com.library.security.LibraryUserDetails;
import com.library.service.RegistrationApprovalService;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/admin/registrations")
public class AdminRegistrationController {

    private final RegistrationApprovalService registrationApprovalService;

    public AdminRegistrationController(RegistrationApprovalService registrationApprovalService) {
        this.registrationApprovalService = registrationApprovalService;
    }

    @GetMapping("/pending")
    public String pending(Model model, @AuthenticationPrincipal LibraryUserDetails principal) {
        model.addAttribute("userEmail", principal.getUsername());
        model.addAttribute("requests", registrationApprovalService.listPending());
        return "admin/registrations-pending";
    }

    @PostMapping("/{requestId}/approve")
    public String approve(
            @PathVariable String requestId,
            @AuthenticationPrincipal LibraryUserDetails principal,
            RedirectAttributes redirectAttributes) {
        try {
            registrationApprovalService.approve(requestId, principal.getUserId());
            redirectAttributes.addFlashAttribute(
                    "flashSuccess", "You approved this registration and the member has been notified.");
        } catch (BusinessRuleException ex) {
            redirectAttributes.addFlashAttribute("flashError", UserFacingMessages.orGeneric(ex.getMessage()));
        }
        return "redirect:/admin/registrations/pending";
    }

    @PostMapping("/{requestId}/reject")
    public String reject(
            @PathVariable String requestId,
            @RequestParam(value = "reason", required = false) String reason,
            @AuthenticationPrincipal LibraryUserDetails principal,
            RedirectAttributes redirectAttributes) {
        try {
            registrationApprovalService.reject(requestId, principal.getUserId(), reason);
            redirectAttributes.addFlashAttribute(
                    "flashSuccess", "You rejected this registration and the member has been notified.");
        } catch (BusinessRuleException ex) {
            redirectAttributes.addFlashAttribute("flashError", UserFacingMessages.orGeneric(ex.getMessage()));
        }
        return "redirect:/admin/registrations/pending";
    }
}
