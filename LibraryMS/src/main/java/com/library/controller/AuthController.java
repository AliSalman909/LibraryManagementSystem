package com.library.controller;

import com.library.security.LibraryLoginFailureHandler;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class AuthController {

    @GetMapping("/login")
    public String login(HttpServletRequest request, Model model) {
        HttpSession session = request.getSession(false);
        if (session != null) {
            Object reason = session.getAttribute(LibraryLoginFailureHandler.SESSION_LOGIN_REJECTION_REASON);
            if (reason instanceof String s && !s.isBlank()) {
                model.addAttribute("loginRejectionReason", s);
            }
            session.removeAttribute(LibraryLoginFailureHandler.SESSION_LOGIN_REJECTION_REASON);
        }
        return "auth/login";
    }

    @GetMapping("/access-denied")
    public String accessDenied() {
        return "error/access-denied";
    }
}
