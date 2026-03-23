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
@Table(name = "admins")
public class AdminProfile {

    /**
     * Filled automatically from {@link #user} via {@link MapsId}. Do not set this manually — only call
     * {@link #setUser(User)} with a persisted {@link User}.
     */
    @Id
    private String userId;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @MapsId
    @JoinColumn(name = "user_id", referencedColumnName = "user_id", columnDefinition = "VARCHAR(512)")
    private User user;

    @Column(length = 100)
    private String department;

    /**
     * Legacy schema compatibility: older databases require this column as NOT NULL.
     */
    @Column(name = "employee_id", nullable = false, length = 64)
    private String employeeId;

    @Column(name = "can_manage_users", nullable = false)
    private boolean canManageUsers = true;

    @Column(name = "can_view_reports", nullable = false)
    private boolean canViewReports = true;

    @Column(name = "can_manage_catalog", nullable = false)
    private boolean canManageCatalog = true;

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

    public String getDepartment() {
        return department;
    }

    public void setDepartment(String department) {
        this.department = department;
    }

    public String getEmployeeId() {
        return employeeId;
    }

    public void setEmployeeId(String employeeId) {
        this.employeeId = employeeId;
    }

    public boolean isCanManageUsers() {
        return canManageUsers;
    }

    public void setCanManageUsers(boolean canManageUsers) {
        this.canManageUsers = canManageUsers;
    }

    public boolean isCanViewReports() {
        return canViewReports;
    }

    public void setCanViewReports(boolean canViewReports) {
        this.canViewReports = canViewReports;
    }

    public boolean isCanManageCatalog() {
        return canManageCatalog;
    }

    public void setCanManageCatalog(boolean canManageCatalog) {
        this.canManageCatalog = canManageCatalog;
    }
}
