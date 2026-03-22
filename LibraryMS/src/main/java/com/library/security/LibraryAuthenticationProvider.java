package com.library.security;

import com.library.entity.User;
import com.library.entity.enums.AccountStatus;
import com.library.repository.RegistrationRequestRepository;
import com.library.repository.UserRepository;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class LibraryAuthenticationProvider implements AuthenticationProvider {

    private final UserRepository userRepository;
    private final RegistrationRequestRepository registrationRequestRepository;
    private final PasswordEncoder passwordEncoder;
    private final PasswordEncryptionService passwordEncryptionService;
    private final UserIdEncryptionService userIdEncryptionService;

    public LibraryAuthenticationProvider(
            UserRepository userRepository,
            RegistrationRequestRepository registrationRequestRepository,
            PasswordEncoder passwordEncoder,
            PasswordEncryptionService passwordEncryptionService,
            UserIdEncryptionService userIdEncryptionService) {
        this.userRepository = userRepository;
        this.registrationRequestRepository = registrationRequestRepository;
        this.passwordEncoder = passwordEncoder;
        this.passwordEncryptionService = passwordEncryptionService;
        this.userIdEncryptionService = userIdEncryptionService;
    }

    @Override
    public Authentication authenticate(Authentication authentication) throws AuthenticationException {
        String email = authentication.getName();
        Object credentials = authentication.getCredentials();
        if (!(credentials instanceof String rawPassword)) {
            throw new BadCredentialsException("Invalid credentials");
        }

        User user =
                userRepository
                        .findByEmailIgnoreCase(email)
                        .orElseThrow(() -> new BadCredentialsException("Invalid email or password"));

        if (!passwordMatches(rawPassword, user.getPasswordHash())) {
            throw new BadCredentialsException("Invalid email or password");
        }

        if (user.getAccountStatus() != AccountStatus.active) {
            String rejectionReason = null;
            if (user.getAccountStatus() == AccountStatus.rejected) {
                rejectionReason =
                        registrationRequestRepository
                                .findByUser_UserId(user.getUserId())
                                .map(rr -> rr.getRejectionReason())
                                .filter(r -> r != null && !r.isBlank())
                                .orElse(null);
            }
            throw new AccountInactiveAuthenticationException(
                    user.getAccountStatus(), messageForStatus(user.getAccountStatus()), rejectionReason);
        }

        String plainUserId = userIdEncryptionService.decryptUserIdForDisplay(user.getUserId());
        LibraryUserDetails principal = new LibraryUserDetails(user, plainUserId);
        return UsernamePasswordAuthenticationToken.authenticated(principal, null, principal.getAuthorities());
    }

    private boolean passwordMatches(String rawPassword, String stored) {
        if (PasswordEncryptionService.isEncryptedFormat(stored)) {
            try {
                String decrypted = passwordEncryptionService.decrypt(stored);
                return constantTimeEquals(rawPassword, decrypted);
            } catch (Exception e) {
                return false;
            }
        }
        if (PasswordEncryptionService.isBcryptFormat(stored)) {
            return passwordEncoder.matches(rawPassword, stored);
        }
        return false;
    }

    private static boolean constantTimeEquals(String a, String b) {
        if (a == null || b == null) {
            return false;
        }
        byte[] x = a.getBytes(StandardCharsets.UTF_8);
        byte[] y = b.getBytes(StandardCharsets.UTF_8);
        if (x.length != y.length) {
            return false;
        }
        return MessageDigest.isEqual(x, y);
    }

    private static String messageForStatus(AccountStatus status) {
        return switch (status) {
            case pending -> "Account pending approval";
            case rejected -> "Registration was rejected";
            case suspended -> "Account suspended";
            case deletion_pending -> "Account deletion pending";
            case deleted -> "Account removed";
            case active -> "Account active";
        };
    }

    @Override
    public boolean supports(Class<?> authentication) {
        return UsernamePasswordAuthenticationToken.class.isAssignableFrom(authentication);
    }
}
