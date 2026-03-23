package com.library.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.MapsId;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "librarians")
public class Librarian {

    @Id
    private String userId;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @MapsId
    @JoinColumn(name = "user_id", referencedColumnName = "user_id", columnDefinition = "VARCHAR(512)")
    private User user;

    /**
     * Legacy schema compatibility: older databases require this column as NOT NULL.
     */
    @Column(name = "employee_id", nullable = false, length = 64)
    private String employeeId;

    @Column(name = "can_approve_borrowing", nullable = false)
    private boolean canApproveBorrowing = true;

    public String getUserId() {
        return userId;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getEmployeeId() {
        return employeeId;
    }

    public void setEmployeeId(String employeeId) {
        this.employeeId = employeeId;
    }

    public boolean isCanApproveBorrowing() {
        return canApproveBorrowing;
    }

    public void setCanApproveBorrowing(boolean canApproveBorrowing) {
        this.canApproveBorrowing = canApproveBorrowing;
    }
}
