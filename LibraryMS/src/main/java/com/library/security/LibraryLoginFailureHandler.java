package com.library.security;

import com.library.entity.enums.AccountStatus;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.stereotype.Component;

@Component
public class LibraryLoginFailureHandler implements AuthenticationFailureHandler {

    /** Session key: one-time rejection reason shown on GET /login after a rejected account sign-in attempt. */
    public static final String SESSION_LOGIN_REJECTION_REASON = "LOGIN_REJECTION_REASON";

    @Override
    public void onAuthenticationFailure(
            HttpServletRequest request, HttpServletResponse response, AuthenticationException exception)
            throws IOException {
        HttpSession session = request.getSession(true);
        session.removeAttribute(SESSION_LOGIN_REJECTION_REASON);

        String path = request.getContextPath() + "/login";
        if (exception instanceof AccountInactiveAuthenticationException inactive) {
            if (inactive.getAccountStatus() == AccountStatus.rejected && inactive.getRejectionReason() != null) {
                session.setAttribute(SESSION_LOGIN_REJECTION_REASON, inactive.getRejectionReason());
            }
            String code = inactive.getAccountStatus().name();
            path += "?account=" + URLEncoder.encode(code, StandardCharsets.UTF_8);
        } else if (exception instanceof BadCredentialsException) {
            path += "?error";
        } else {
            path += "?error";
        }
        response.sendRedirect(path);
    }
}
