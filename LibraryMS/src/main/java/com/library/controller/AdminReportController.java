package com.library.controller;

import com.library.service.ReportService;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/admin/reports")
public class AdminReportController {

    private final ReportService reportService;

    public AdminReportController(ReportService reportService) {
        this.reportService = reportService;
    }

    /** Overdue loans listing. */
    @GetMapping("/overdue")
    public String overdue(Model model, HttpServletResponse response) {
        applyNoStore(response);
        model.addAttribute("overdueLoans", reportService.getOverdueLoans());
        return "admin/reports-overdue";
    }

    /** Currently issued (active) books. */
    @GetMapping("/issued")
    public String issued(Model model, HttpServletResponse response) {
        applyNoStore(response);
        model.addAttribute("activeLoans", reportService.getActiveLoans());
        return "admin/reports-issued";
    }

    /** Fine collection summary. */
    @GetMapping("/fines")
    public String fines(Model model, HttpServletResponse response) {
        applyNoStore(response);
        model.addAttribute("fineReport", reportService.getFineReport());
        return "admin/reports-fines";
    }

    /** User activity summary. */
    @GetMapping("/activity")
    public String activity(Model model, HttpServletResponse response) {
        applyNoStore(response);
        model.addAttribute("activityReport", reportService.getActivityReport());
        return "admin/reports-activity";
    }

    private static void applyNoStore(HttpServletResponse response) {
        response.setHeader("Cache-Control", "no-store, no-cache, must-revalidate, max-age=0");
        response.setHeader("Pragma", "no-cache");
        response.setDateHeader("Expires", 0);
    }
}
