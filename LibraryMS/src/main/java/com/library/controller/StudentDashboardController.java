package com.library.controller;

import com.library.security.LibraryUserDetails;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/student")
public class StudentDashboardController {

    @GetMapping("/dashboard")
    public String dashboard(Model model, @AuthenticationPrincipal LibraryUserDetails principal) {
        model.addAttribute("userEmail", principal.getUsername());
        model.addAttribute("profilePictureUrl", principal.getProfilePicture());
        model.addAttribute("profilePictureFocalX", principal.getProfilePictureFocalXEffective());
        model.addAttribute("profilePictureFocalY", principal.getProfilePictureFocalYEffective());
        return "dashboard/student";
    }
}
