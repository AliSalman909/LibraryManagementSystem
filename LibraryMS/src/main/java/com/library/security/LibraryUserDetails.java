package com.library.security;

import com.library.entity.User;
import java.util.Collection;
import java.util.List;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

public class LibraryUserDetails implements UserDetails {

    private final String userId;
    /** Same as stored {@code user_id} when plain; legacy DB rows may differ until migrated. Shown in the UI header. */
    private final String plainUserId;
    private final String fullName;
    private final String email;
    private final String passwordHash;
    private final String profilePicture;
    private final Double profilePictureFocalX;
    private final Double profilePictureFocalY;
    private final Collection<? extends GrantedAuthority> authorities;

    public LibraryUserDetails(User user, String plainUserId) {
        this.userId = user.getUserId();
        this.plainUserId = plainUserId != null ? plainUserId : "";
        this.fullName = user.getFullName() != null ? user.getFullName() : "";
        this.email = user.getEmail();
        this.passwordHash = user.getPasswordHash();
        this.profilePicture = resolveProfilePictureUrl(user.getProfilePicture());
        this.profilePictureFocalX = user.getProfilePictureFocalX();
        this.profilePictureFocalY = user.getProfilePictureFocalY();
        this.authorities = List.of(new SimpleGrantedAuthority("ROLE_" + user.getUserRole().name()));
    }

    public String getUserId() {
        return userId;
    }

    public String getPlainUserId() {
        return plainUserId;
    }

    public String getFullName() {
        return fullName;
    }

    public String getProfilePicture() {
        return profilePicture;
    }

    public Double getProfilePictureFocalX() {
        return profilePictureFocalX;
    }

    public Double getProfilePictureFocalY() {
        return profilePictureFocalY;
    }

    public Double getProfilePictureFocalXEffective() {
        return profilePictureFocalX != null ? profilePictureFocalX : 50.0;
    }

    public Double getProfilePictureFocalYEffective() {
        return profilePictureFocalY != null ? profilePictureFocalY : 50.0;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return authorities;
    }

    @Override
    public String getPassword() {
        return passwordHash;
    }

    @Override
    public String getUsername() {
        return email;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }

    private static String resolveProfilePictureUrl(String storedValue) {
        if (storedValue == null || storedValue.isBlank()) {
            return storedValue;
        }
        // Backward compatible: old rows may store '/uploads/profiles/file.jpg'
        if (storedValue.startsWith("/uploads/profiles/")) {
            return storedValue;
        }
        return "/uploads/profiles/" + storedValue;
    }
}
