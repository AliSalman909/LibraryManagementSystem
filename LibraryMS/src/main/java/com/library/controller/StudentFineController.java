package com.library.controller;

import com.library.security.LibraryUserDetails;
import com.library.service.FineService;
import java.math.BigDecimal;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class StudentFineController {

    private final FineService fineService;

    public StudentFineController(FineService fineService) {
        this.fineService = fineService;
    }

    /** Shows the logged-in student's own fines (outstanding + history). */
    @GetMapping("/student/fines")
    public String studentFines(
            @AuthenticationPrincipal LibraryUserDetails principal,
            Model model) {
        var fines = fineService.listFinesForStudent(principal.getUserId());
        BigDecimal outstandingTotal = fines.stream()
                .filter(f -> f.getStatus().name().equals("UNPAID"))
                .map(f -> f.getAmount())
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        model.addAttribute("fines", fines);
        model.addAttribute("outstandingTotal", outstandingTotal);
        return "student/fines";
    }
}
