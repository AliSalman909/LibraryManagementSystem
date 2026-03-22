package com.library.dto;

import com.library.entity.enums.UserRole;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import java.time.LocalDate;

public class RegistrationForm {

    @NotBlank(message = "Please enter your full name.")
    @Size(max = 100, message = "Full name is too long (100 characters maximum).")
    private String fullName;

    @NotBlank(message = "Please enter your email address.")
    @Email(message = "That does not look like a valid email address.")
    @Size(max = 150, message = "Email address is too long.")
    private String email;

    @NotBlank(message = "Please choose a password.")
    @Size(min = 8, max = 128, message = "Use between 8 and 128 characters for your password.")
    @Pattern(
            regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d).+$",
            message = "Include at least one uppercase letter, one lowercase letter, and one number.")
    private String password;

    @NotBlank(message = "Please confirm your password.")
    private String confirmPassword;

    @Size(max = 20, message = "Phone number is too long.")
    private String phoneNumber;

    @NotNull(message = "Please select whether you are a student, librarian, or administrator.")
    private UserRole role;

    @Size(max = 100, message = "Program name is too long.")
    private String program;

    private LocalDate enrollmentDate;
    private LocalDate dateOfBirth;

    @Size(max = 100, message = "Department name is too long.")
    private String department;

    @AssertTrue(message = "The two passwords do not match. Please re-enter them.")
    public boolean isPasswordMatching() {
        if (password == null || confirmPassword == null) {
            return false;
        }
        return password.equals(confirmPassword);
    }

    @AssertTrue(message = "Students need to enter a study program.")
    public boolean isStudentRoleValid() {
        if (role != UserRole.STUDENT) {
            return true;
        }
        return program != null && !program.isBlank();
    }

    @AssertTrue(message = "Please check your librarian registration details and try again.")
    public boolean isLibrarianRoleValid() {
        return true;
    }

    @AssertTrue(message = "Please check your administrator registration details and try again.")
    public boolean isAdminRoleValid() {
        return true;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getConfirmPassword() {
        return confirmPassword;
    }

    public void setConfirmPassword(String confirmPassword) {
        this.confirmPassword = confirmPassword;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public UserRole getRole() {
        return role;
    }

    public void setRole(UserRole role) {
        this.role = role;
    }

    public String getProgram() {
        return program;
    }

    public void setProgram(String program) {
        this.program = program;
    }

    public LocalDate getEnrollmentDate() {
        return enrollmentDate;
    }

    public void setEnrollmentDate(LocalDate enrollmentDate) {
        this.enrollmentDate = enrollmentDate;
    }

    public LocalDate getDateOfBirth() {
        return dateOfBirth;
    }

    public void setDateOfBirth(LocalDate dateOfBirth) {
        this.dateOfBirth = dateOfBirth;
    }

    public String getDepartment() {
        return department;
    }

    public void setDepartment(String department) {
        this.department = department;
    }
}
