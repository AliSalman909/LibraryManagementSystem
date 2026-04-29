package com.library.dto;

import java.time.Instant;

import com.library.entity.User;
import com.library.entity.enums.RequestStatus;

/** Admin-only row: user entity plus decrypted password text when stored with app encryption; user id as stored/plain. */
public class AdminUserAccountView {

    private final User user;
    private final String passwordPlainText;
    private final String plainUserId;
    /** From linked {@code registration_requests} row, if any. */
    private final RequestStatus registrationRequestStatus;
    private final Instant registrationReviewedAt;
    private final String registrationRejectionReason;

    public AdminUserAccountView(
            User user,
            String passwordPlainText,
            String plainUserId,
            RequestStatus registrationRequestStatus,
            Instant registrationReviewedAt,
            String registrationRejectionReason) {
        this.user = user;
        this.passwordPlainText = passwordPlainText;
        this.plainUserId = plainUserId;
        this.registrationRequestStatus = registrationRequestStatus;
        this.registrationReviewedAt = registrationReviewedAt;
        this.registrationRejectionReason = registrationRejectionReason;
    }

    public User getUser() {
        return user;
    }

    public String getPasswordPlainText() {
        return passwordPlainText;
    }

    public String getPlainUserId() {
        return plainUserId;
    }

    public RequestStatus getRegistrationRequestStatus() {
        return registrationRequestStatus;
    }

    public Instant getRegistrationReviewedAt() {
        return registrationReviewedAt;
    }

    public String getRegistrationRejectionReason() {
        return registrationRejectionReason;
    }
}
