package com.library.controller;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class HomeController {

    @GetMapping("/")
    public String home(Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            return "redirect:/login";
        }
        for (GrantedAuthority ga : authentication.getAuthorities()) {
            switch (ga.getAuthority()) {
                case "ROLE_ADMIN" -> {
                    return "redirect:/admin/dashboard";
                }
                case "ROLE_LIBRARIAN" -> {
                    return "redirect:/librarian/dashboard";
                }
                default -> {
                }
            }
        }
        return "redirect:/student/dashboard";
    }
}
