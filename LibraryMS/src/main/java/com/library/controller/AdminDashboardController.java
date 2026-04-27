package com.library.controller;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.library.exception.BusinessRuleException;
import com.library.messages.UserFacingMessages;
import com.library.security.LibraryUserDetails;
import com.library.service.AdminMaintenanceService;

@Controller
@RequestMapping("/admin")
public class AdminDashboardController {

    private final AdminMaintenanceService adminMaintenanceService;

    public AdminDashboardController(AdminMaintenanceService adminMaintenanceService) {
        this.adminMaintenanceService = adminMaintenanceService;
    }

    @GetMapping("/dashboard")
    public String dashboard(Model model, @AuthenticationPrincipal LibraryUserDetails principal) {
        model.addAttribute("userEmail", principal.getUsername());
        model.addAttribute("profilePictureUrl", principal.getProfilePicture());
        model.addAttribute("profilePictureFocalX", principal.getProfilePictureFocalXEffective());
        model.addAttribute("profilePictureFocalY", principal.getProfilePictureFocalYEffective());
        model.addAttribute("lastAutoMaintenanceAt", adminMaintenanceService.getLastAutoMaintenanceAt());
        return "dashboard/admin";
    }

    @PostMapping("/maintenance/backup")
    public String runBackupAndCleanup(RedirectAttributes redirectAttributes) {
        try {
            AdminMaintenanceService.MaintenanceResult result = adminMaintenanceService.backupAndCleanup();
            redirectAttributes.addFlashAttribute(
                    "flashSuccess",
                    "Maintenance Complete. Backup Done !!! Removed temporary files and reclaimed Storage");
        } catch (BusinessRuleException ex) {
            redirectAttributes.addFlashAttribute("flashError", UserFacingMessages.orGeneric(ex.getMessage()));
        }
        return "redirect:/admin/dashboard";
    }

    @PostMapping("/maintenance/restore")
    public String restoreFromBackup(RedirectAttributes redirectAttributes) {
        try {
            AdminMaintenanceService.RestoreResult result = adminMaintenanceService.restoreFromLatestBackup();
            redirectAttributes.addFlashAttribute(
                    "flashSuccess",
                    "Restore complete from backup file Sucessfully!!!.");
        } catch (BusinessRuleException ex) {
            redirectAttributes.addFlashAttribute("flashError", UserFacingMessages.orGeneric(ex.getMessage()));
        }
        return "redirect:/admin/dashboard";
    }
}
