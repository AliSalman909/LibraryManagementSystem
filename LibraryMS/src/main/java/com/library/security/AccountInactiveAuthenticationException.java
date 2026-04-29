package com.library.security;

import com.library.entity.enums.AccountStatus;
import org.springframework.security.core.AuthenticationException;

public class AccountInactiveAuthenticationException extends AuthenticationException {

    private final AccountStatus accountStatus;
    /** When {@link AccountStatus#rejected}, optional staff-entered reason from registration review. */
    private final String rejectionReason;

    public AccountInactiveAuthenticationException(AccountStatus accountStatus, String message) {
        this(accountStatus, message, null);
    }

    public AccountInactiveAuthenticationException(AccountStatus accountStatus, String message, String rejectionReason) {
        super(message);
        this.accountStatus = accountStatus;
        this.rejectionReason = rejectionReason;
    }

    public AccountStatus getAccountStatus() {
        return accountStatus;
    }

    public String getRejectionReason() {
        return rejectionReason;
    }
}
