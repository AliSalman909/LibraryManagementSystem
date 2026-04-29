package com.library.security;

import com.library.service.UserService;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

@Component
public class LibraryLoginSuccessHandler implements AuthenticationSuccessHandler {

    private final UserService userService;

    public LibraryLoginSuccessHandler(UserService userService) {
        this.userService = userService;
    }

    @Override
    public void onAuthenticationSuccess(
            HttpServletRequest request, HttpServletResponse response, Authentication authentication)
            throws IOException, ServletException {
        LibraryUserDetails principal = (LibraryUserDetails) authentication.getPrincipal();
        userService.recordSuccessfulLogin(principal.getUserId());

        String target = "/student/dashboard";
        for (GrantedAuthority ga : authentication.getAuthorities()) {
            String a = ga.getAuthority();
            if ("ROLE_ADMIN".equals(a)) {
                target = "/admin/dashboard";
                break;
            }
            if ("ROLE_LIBRARIAN".equals(a)) {
                target = "/librarian/dashboard";
            }
        }
        response.sendRedirect(request.getContextPath() + target);
    }
}
