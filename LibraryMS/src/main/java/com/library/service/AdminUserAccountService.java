package com.library.service;

import com.library.dto.AdminUserAccountView;
import com.library.entity.Notification;
import com.library.entity.RegistrationRequest;
import com.library.entity.User;
import com.library.entity.enums.AccountStatus;
import com.library.entity.enums.NotificationType;
import com.library.entity.enums.RequestStatus;
import com.library.entity.enums.UserRole;
import com.library.exception.BusinessRuleException;
import com.library.repository.AdminProfileRepository;
import com.library.repository.DeletionRequestRepository;
import com.library.repository.LibrarianRepository;
import com.library.repository.NotificationRepository;
import com.library.repository.RegistrationRequestRepository;
import com.library.repository.StudentRepository;
import com.library.repository.UserRepository;
import com.library.security.PasswordEncryptionService;
import com.library.security.UserIdEncryptionService;
import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AdminUserAccountService {

    private final UserRepository userRepository;
    private final PasswordEncryptionService passwordEncryptionService;
    private final UserIdEncryptionService userIdEncryptionService;
    private final NotificationRepository notificationRepository;
    private final RegistrationRequestRepository registrationRequestRepository;
    private final DeletionRequestRepository deletionRequestRepository;
    private final StudentRepository studentRepository;
    private final LibrarianRepository librarianRepository;
    private final AdminProfileRepository adminProfileRepository;

    public AdminUserAccountService(
            UserRepository userRepository,
            PasswordEncryptionService passwordEncryptionService,
            UserIdEncryptionService userIdEncryptionService,
            NotificationRepository notificationRepository,
            RegistrationRequestRepository registrationRequestRepository,
            DeletionRequestRepository deletionRequestRepository,
            StudentRepository studentRepository,
            LibrarianRepository librarianRepository,
            AdminProfileRepository adminProfileRepository) {
        this.userRepository = userRepository;
        this.passwordEncryptionService = passwordEncryptionService;
        this.userIdEncryptionService = userIdEncryptionService;
        this.notificationRepository = notificationRepository;
        this.registrationRequestRepository = registrationRequestRepository;
        this.deletionRequestRepository = deletionRequestRepository;
        this.studentRepository = studentRepository;
        this.librarianRepository = librarianRepository;
        this.adminProfileRepository = adminProfileRepository;
    }

    @Transactional(readOnly = true)
    public List<AdminUserAccountView> listAllForAdminDisplay() {
        List<User> users = userRepository.findAll(Sort.by(Sort.Direction.DESC, "createdAt"));
        if (users.isEmpty()) {
            return List.of();
        }
        List<String> userIds = users.stream().map(User::getUserId).toList();
        Map<String, RegistrationRequest> regByUserId = new HashMap<>();
        for (RegistrationRequest rr : registrationRequestRepository.findAllByUser_UserIdInWithUser(userIds)) {
            regByUserId.put(rr.getUser().getUserId(), rr);
        }
        return users.stream().map(u -> toAdminView(u, regByUserId.get(u.getUserId()))).toList();
    }

    private AdminUserAccountView toAdminView(User user, RegistrationRequest registrationRequest) {
        String stored = user.getPasswordHash();
        String plainPassword;
        if (PasswordEncryptionService.isBcryptFormat(stored)) {
            plainPassword = "Not shown (older sign-in format)";
        } else if (PasswordEncryptionService.isEncryptedFormat(stored)) {
            try {
                plainPassword = passwordEncryptionService.decrypt(stored);
            } catch (Exception e) {
                plainPassword = "Not shown";
            }
        } else {
            plainPassword = "Not shown";
        }
        String plainUserId = userIdEncryptionService.decryptUserIdForDisplay(user.getUserId());
        RequestStatus regStatus = registrationRequest != null ? registrationRequest.getStatus() : null;
        Instant reviewedAt = registrationRequest != null ? registrationRequest.getReviewedAt() : null;
        String rejectionReason = registrationRequest != null ? registrationRequest.getRejectionReason() : null;
        return new AdminUserAccountView(user, plainPassword, plainUserId, regStatus, reviewedAt, rejectionReason);
    }

    @Transactional
    public void setAccountStatus(String storedUserId, AccountStatus newStatus) {
        User user = loadManagedUser(storedUserId);
        assertNotAdmin(user);
        user.setAccountStatus(newStatus);
        userRepository.save(user);
    }

    @Transactional
    public void suspendUser(String storedUserId) {
        User user = loadManagedUser(storedUserId);
        assertNotAdmin(user);
        if (user.getAccountStatus() == AccountStatus.suspended) {
            throw new BusinessRuleException("This account is already suspended.");
        }
        user.setAccountStatus(AccountStatus.suspended);
        userRepository.save(user);
    }

    @Transactional
    public void activateUser(String userId) {
        User user = loadManagedUser(userId);
        assertNotAdmin(user);
        if (user.getAccountStatus() != AccountStatus.suspended) {
            throw new BusinessRuleException(
                    "Only suspended accounts can be set back to active this way. Use the status menu for other"
                            + " changes.");
        }
        user.setAccountStatus(AccountStatus.active);
        userRepository.save(user);
    }

    /**
     * Reactivates a previously rejected account from the user-accounts screen: sets user to active and marks the
     * linked registration request as approved.
     */
    @Transactional
    public void approveRejectedByUserId(String userId, String adminUserId) {
        User user = loadManagedUser(userId);
        assertNotAdmin(user);
        if (user.getAccountStatus() != AccountStatus.rejected) {
            throw new BusinessRuleException(
                    "Only accounts that were rejected can be re-approved here. For other cases, use the status options"
                            + " on the user list.");
        }
        RegistrationRequest rr =
                registrationRequestRepository
                        .findByUser_UserId(userId)
                        .orElseThrow(
                                () ->
                                        new BusinessRuleException(
                                                "We could not find a registration on file for this person."));
        if (rr.getStatus() != RequestStatus.rejected) {
            throw new BusinessRuleException(
                    "This registration is not in a state where it can be re-approved from this screen.");
        }

        user.setAccountStatus(AccountStatus.active);
        userRepository.save(user);

        rr.setStatus(RequestStatus.approved);
        rr.setReviewedAt(Instant.now());
        rr.setRejectionReason(null);
        registrationRequestRepository.save(rr);

        notify(user, adminUserId, NotificationType.account_approved, "Account approved", buildApprovedMessage(user));
    }

    /**
     * Permanently removes the user row and dependent records. Not allowed for {@link UserRole#ADMIN}.
     */
    @Transactional
    public void hardDeleteUser(String storedUserId) {
        User user = loadManagedUser(storedUserId);
        assertNotAdmin(user);
        String uid = user.getUserId();
        notificationRepository.deleteAllForUser(uid);
        registrationRequestRepository.deleteAllForUser(uid);
        deletionRequestRepository.deleteAllForUser(uid);
        studentRepository.deleteById(uid);
        librarianRepository.deleteById(uid);
        adminProfileRepository.deleteById(uid);
        userRepository.deleteById(uid);
    }

    private User loadManagedUser(String userId) {
        return userRepository
                .findById(userId)
                .orElseThrow(
                        () ->
                                new BusinessRuleException(
                                        "We could not find that account. Refresh the page and try again."));
    }

    private static void assertNotAdmin(User user) {
        if (user.getUserRole() == UserRole.ADMIN) {
            throw new BusinessRuleException(
                    "Administrator accounts are protected and cannot be changed or removed from this screen.");
        }
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
