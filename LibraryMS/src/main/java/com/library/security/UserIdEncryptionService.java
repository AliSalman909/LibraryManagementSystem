package com.library.security;

import java.nio.BufferUnderflowException;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;
import java.util.Locale;
import java.util.regex.Pattern;

import javax.crypto.Cipher;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

/**
 * User IDs are stored as plain text: exactly six alphanumeric characters (normalized to uppercase). No encryption is
 * applied for new rows.
 *
 * <p>Legacy databases may still contain deterministic AES-256-GCM values prefixed with {@value #LEGACY_PREFIX}; those
 * are decrypted only for display. Optional {@code app.security.user-id-secret} affects legacy decryption only.
 */
@Service
public class UserIdEncryptionService {

    public static final int PLAIN_USER_ID_LENGTH = 6;

    private static final String LEGACY_PREFIX = "U1:";
    private static final int GCM_IV_LENGTH = 12;
    private static final int GCM_TAG_BITS = 128;
    private static final String TRANSFORMATION = "AES/GCM/NoPadding";

    /** Six letters or digits (case-insensitive); normalized to uppercase for storage. */
    private static final Pattern PLAIN_ALPHANUMERIC_6 = Pattern.compile("[A-Za-z0-9]{6}");

    private final SecretKeySpec legacyKeySpec;

    public UserIdEncryptionService(
            @Value("${app.security.password-secret}") String passwordSecret,
            @Value("${app.security.user-id-secret:}") String userIdSecret) {
        if (!StringUtils.hasText(passwordSecret) || passwordSecret.length() < 16) {
            throw new IllegalStateException(
                    "app.security.password-secret must be set and at least 16 characters long.");
        }
        String material =
                StringUtils.hasText(userIdSecret) && userIdSecret.length() >= 16 ? userIdSecret : passwordSecret;
        if (StringUtils.hasText(userIdSecret) && userIdSecret.length() < 16) {
            throw new IllegalStateException(
                    "app.security.user-id-secret must be at least 16 characters when set.");
        }
        try {
            byte[] keyBytes =
                    MessageDigest.getInstance("SHA-256").digest(material.getBytes(StandardCharsets.UTF_8));
            this.legacyKeySpec = new SecretKeySpec(keyBytes, "AES");
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("Could not build legacy user-id key", e);
        }
    }

    public boolean isValidPlainUserId(String raw) {
        return raw != null && PLAIN_ALPHANUMERIC_6.matcher(raw.trim()).matches();
    }

    public String normalizePlainUserId(String raw) {
        return raw.trim().toUpperCase(Locale.ROOT);
    }

    /** Returns the value persisted as {@code users.user_id}: normalized plain id (historical name {@code encryptUserId}). */
    public String encryptUserId(String plainUserId) {
        if (!isValidPlainUserId(plainUserId)) {
            throw new IllegalArgumentException("User id must be exactly 6 letters or digits");
        }
        return normalizePlainUserId(plainUserId);
    }

    /**
     * For display: returns stored plain ids as-is; decrypts legacy {@value #LEGACY_PREFIX} rows when possible.
     */
    public String decryptUserIdForDisplay(String stored) {
        if (stored == null) {
            return null;
        }
        if (!isLegacyEncryptedUserId(stored)) {
            return stored;
        }

        try {
            byte[] decoded =
                    Base64.getUrlDecoder().decode(stored.substring(LEGACY_PREFIX.length()));
            ByteBuffer buf = ByteBuffer.wrap(decoded);
            byte[] iv = new byte[GCM_IV_LENGTH];
            buf.get(iv);
            byte[] cipherBytes = new byte[buf.remaining()];
            buf.get(cipherBytes);
            Cipher cipher = Cipher.getInstance(TRANSFORMATION);
            cipher.init(Cipher.DECRYPT_MODE, legacyKeySpec, new GCMParameterSpec(GCM_TAG_BITS, iv));
            byte[] plain = cipher.doFinal(cipherBytes);
            return new String(plain, StandardCharsets.UTF_8);
        } catch (IllegalArgumentException | GeneralSecurityException | BufferUnderflowException e) {
            return "Not available";
        }
    }

    public static boolean isLegacyEncryptedUserId(String stored) {
        return stored != null && stored.startsWith(LEGACY_PREFIX);
    }
}
