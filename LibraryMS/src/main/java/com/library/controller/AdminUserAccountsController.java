package com.library.controller;

import com.library.exception.BusinessRuleException;
import com.library.messages.UserFacingMessages;
import com.library.security.LibraryUserDetails;
import com.library.service.AdminUserAccountService;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import com.library.entity.enums.AccountStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/admin/users")
public class AdminUserAccountsController {

    private final AdminUserAccountService adminUserAccountService;

    public AdminUserAccountsController(AdminUserAccountService adminUserAccountService) {
        this.adminUserAccountService = adminUserAccountService;
    }

    @ModelAttribute("accountStatuses")
    public AccountStatus[] accountStatuses() {
        return AccountStatus.values();
    }

    @GetMapping
    public String list(Model model, @AuthenticationPrincipal LibraryUserDetails principal) {
        model.addAttribute("userEmail", principal.getUsername());
        model.addAttribute("userAccountRows", adminUserAccountService.listAllForAdminDisplay());
        return "admin/user-accounts";
    }

    @PostMapping("/{userId}/approve")
    public String approveRejected(
            @PathVariable String userId,
            @AuthenticationPrincipal LibraryUserDetails principal,
            RedirectAttributes redirectAttributes) {
        try {
            adminUserAccountService.approveRejectedByUserId(userId, principal.getUserId());
            redirectAttributes.addFlashAttribute(
                    "flashSuccess", "The account is active again and the member can sign in.");
        } catch (BusinessRuleException ex) {
            redirectAttributes.addFlashAttribute("flashError", UserFacingMessages.orGeneric(ex.getMessage()));
        }
        return "redirect:/admin/users";
    }

    @PostMapping("/{userId}/suspend")
    public String suspend(
            @PathVariable String userId,
            RedirectAttributes redirectAttributes) {
        return handle(() -> adminUserAccountService.suspendUser(userId), redirectAttributes);
    }

    @PostMapping("/{userId}/activate")
    public String activate(
            @PathVariable String userId,
            RedirectAttributes redirectAttributes) {
        return handle(() -> adminUserAccountService.activateUser(userId), redirectAttributes);
    }

    @PostMapping("/{userId}/status")
    public String setStatus(
            @PathVariable String userId,
            @RequestParam("status") AccountStatus status,
            RedirectAttributes redirectAttributes) {
        return handle(() -> adminUserAccountService.setAccountStatus(userId, status), redirectAttributes);
    }

    @PostMapping("/{userId}/delete")
    public String delete(
            @PathVariable String userId,
            RedirectAttributes redirectAttributes) {
        try {
            adminUserAccountService.hardDeleteUser(userId);
            redirectAttributes.addFlashAttribute(
                    "flashSuccess", "That member account and related information have been removed.");
        } catch (BusinessRuleException ex) {
            redirectAttributes.addFlashAttribute("flashError", UserFacingMessages.orGeneric(ex.getMessage()));
        }
        return "redirect:/admin/users";
    }

    private static String handle(Runnable action, RedirectAttributes redirectAttributes) {
        try {
            action.run();
            redirectAttributes.addFlashAttribute("flashSuccess", "Your changes were saved.");
        } catch (BusinessRuleException ex) {
            redirectAttributes.addFlashAttribute("flashError", UserFacingMessages.orGeneric(ex.getMessage()));
        }
        return "redirect:/admin/users";
    }
}
