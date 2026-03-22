package com.library.service;

import com.library.entity.Notification;
import com.library.entity.RegistrationRequest;
import com.library.entity.User;
import com.library.entity.enums.AccountStatus;
import com.library.entity.enums.NotificationType;
import com.library.entity.enums.RequestStatus;
import com.library.exception.BusinessRuleException;
import com.library.repository.NotificationRepository;
import com.library.repository.RegistrationRequestRepository;
import com.library.repository.UserRepository;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

@Service
public class RegistrationApprovalService {

    private final RegistrationRequestRepository registrationRequestRepository;
    private final UserRepository userRepository;
    private final NotificationRepository notificationRepository;

    public RegistrationApprovalService(
            RegistrationRequestRepository registrationRequestRepository,
            UserRepository userRepository,
            NotificationRepository notificationRepository) {
        this.registrationRequestRepository = registrationRequestRepository;
        this.userRepository = userRepository;
        this.notificationRepository = notificationRepository;
    }

    @Transactional(readOnly = true)
    public List<RegistrationRequest> listPending() {
        return registrationRequestRepository.findAllByStatusWithUser(RequestStatus.pending);
    }

    @Transactional
    public void approve(String requestId, String adminUserId) {
        RegistrationRequest rr =
                registrationRequestRepository
                        .findByIdWithUser(requestId)
                        .orElseThrow(
                                () ->
                                        new BusinessRuleException(
                                                "We could not find that registration. It may have already been"
                                                        + " handled - refresh the page to see the latest list."));
        if (rr.getStatus() != RequestStatus.pending) {
            throw new BusinessRuleException(
                    "This registration has already been approved or rejected. Refresh the page for the current list.");
        }

        User user = rr.getUser();
        user.setAccountStatus(AccountStatus.active);
        userRepository.save(user);

        rr.setStatus(RequestStatus.approved);
        rr.setReviewedAt(Instant.now());
        rr.setRejectionReason(null);
        registrationRequestRepository.save(rr);

        notify(user, adminUserId, NotificationType.account_approved, "Account approved", buildApprovedMessage(user));
    }

    @Transactional
    public void reject(String requestId, String adminUserId, String reason) {
        RegistrationRequest rr =
                registrationRequestRepository
                        .findByIdWithUser(requestId)
                        .orElseThrow(
                                () ->
                                        new BusinessRuleException(
                                                "We could not find that registration. It may have already been"
                                                        + " handled - refresh the page to see the latest list."));
        if (rr.getStatus() != RequestStatus.pending) {
            throw new BusinessRuleException(
                    "This registration has already been approved or rejected. Refresh the page for the current list.");
        }

        User user = rr.getUser();
        user.setAccountStatus(AccountStatus.rejected);
        userRepository.save(user);

        String trimmed = StringUtils.hasText(reason) ? reason.trim() : null;
        if (trimmed != null && trimmed.length() > 255) {
            trimmed = trimmed.substring(0, 255);
        }

        rr.setStatus(RequestStatus.rejected);
        rr.setReviewedAt(Instant.now());
        rr.setRejectionReason(trimmed);
        registrationRequestRepository.save(rr);

        String message =
                trimmed != null
                        ? "Your registration was rejected. Reason: " + trimmed
                        : "Your registration was rejected. Contact the library if you need more information.";
        notify(user, adminUserId, NotificationType.account_rejected, "Registration not approved", message);
    }

    private void notify(User recipient, String adminUserId, NotificationType type, String title, String message) {
        Notification n = new Notification();
        n.setNotificationId(UUID.randomUUID().toString());
        n.setRecipient(recipient);
        n.setSender(userRepository.getReferenceById(adminUserId));
        n.setType(type);
        n.setTitle(title);
        n.setMessage(message);
        n.setReadFlag(false);
        notificationRepository.save(n);
    }

    private static String buildApprovedMessage(User user) {
        return "Your library account ("
                + user.getEmail()
                + ") is now active. You can sign in with your email and password.";
    }
}
