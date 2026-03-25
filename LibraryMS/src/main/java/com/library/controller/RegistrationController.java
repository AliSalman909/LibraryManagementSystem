package com.library.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.unit.DataSize;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.library.dto.RegistrationForm;
import com.library.entity.enums.UserRole;
import com.library.exception.BusinessRuleException;
import com.library.messages.UserFacingMessages;
import com.library.service.RegistrationService;

import jakarta.persistence.PersistenceException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import java.util.List;
import java.util.Locale;

@Controller
public class RegistrationController {

    private static final Logger log = LoggerFactory.getLogger(RegistrationController.class);

    /** Session-scoped 6-character plain id shown before submit; removed after successful registration. */
    private static final String SESSION_PENDING_USER_ID = "PENDING_REGISTRATION_PLAIN_USER_ID";

    private final RegistrationService registrationService;
    private final DataSize maxProfilePictureSize;

    public RegistrationController(
            RegistrationService registrationService,
            @Value("${spring.servlet.multipart.max-file-size}") DataSize maxProfilePictureSize) {
        this.registrationService = registrationService;
        this.maxProfilePictureSize = maxProfilePictureSize;
    }

    @ModelAttribute("roles")
    public UserRole[] roles() {
        return UserRole.values();
    }

    @ModelAttribute("studentPrograms")
    public List<String> studentPrograms() {
        return RegistrationForm.ALLOWED_STUDENT_PROGRAMS.stream().sorted().toList();
    }

    @ModelAttribute("maxProfilePictureBytes")
    public long maxProfilePictureBytes() {
        return maxProfilePictureSize.toBytes();
    }

    @ModelAttribute("maxProfilePictureLabel")
    public String maxProfilePictureLabel() {
        return maxProfilePictureSize.toMegabytes() + " MB";
    }

    @GetMapping("/register")
    public String showForm(HttpServletRequest request, Model model) {
        if (!model.containsAttribute("registrationForm")) {
            model.addAttribute("registrationForm", new RegistrationForm());
        }
        ensurePreviewUserId(request.getSession(true), model);
        return "auth/register";
    }

    /**
     * Shown after a successful POST /register (flash attributes). Avoids resubmit-on-refresh and displays the assigned
     * plain user ID on the registration flow.
     */
    @GetMapping("/register/complete")
    public String registrationComplete(@ModelAttribute("registeredPlainUserId") String registeredPlainUserId) {
        if (registeredPlainUserId == null || registeredPlainUserId.isBlank()) {
            return "redirect:/register";
        }
        return "auth/registration-complete";
    }

    @SuppressWarnings("null")
    @PostMapping("/register")
    public String register(
            HttpServletRequest request,
            @Valid @ModelAttribute("registrationForm") RegistrationForm form,
            BindingResult bindingResult,
            @RequestParam(value = "profilePictureFile", required = false) MultipartFile profilePictureFile,
            @RequestParam(value = "profilePictureFocalX", defaultValue = "50") double profilePictureFocalX,
            @RequestParam(value = "profilePictureFocalY", defaultValue = "50") double profilePictureFocalY,
            RedirectAttributes redirectAttributes,
            Model model) {
        HttpSession session = request.getSession(true);
        ensurePreviewUserId(session, model);

        if (bindingResult.hasErrors()) {
            return "auth/register";
        }
        if (profilePictureFile != null
                && !profilePictureFile.isEmpty()
                && profilePictureFile.getSize() > maxProfilePictureSize.toBytes()) {
            bindingResult.reject(
                    "registrationGlobal",
                    "That profile photo is too large. The limit is "
                            + maxProfilePictureSize.toMegabytes()
                            + " MB — please choose a smaller image.");
            return "auth/register";
        }

        String pendingPlainUserId = (String) session.getAttribute(SESSION_PENDING_USER_ID);

        final String normalizedPlainUserId;
        try {
            normalizedPlainUserId = registrationService.validateSessionPlainUserIdForSubmit(pendingPlainUserId);
        } catch (BusinessRuleException ex) {
            bindingResult.reject("registrationGlobal", UserFacingMessages.orGeneric(ex.getMessage()));
            session.setAttribute(
                    SESSION_PENDING_USER_ID,
                    registrationService.ensurePendingPlainUserIdForSession(null));
            ensurePreviewUserId(session, model);
            return "auth/register";
        }

        String assignedPlainUserId;
        try {
            assignedPlainUserId =
                    registrationService.register(
                            form,
                            profilePictureFile,
                            profilePictureFocalX,
                            profilePictureFocalY,
                            normalizedPlainUserId);
        } catch (BusinessRuleException ex) {
            bindingResult.reject("registrationGlobal", UserFacingMessages.orGeneric(ex.getMessage()));
            return "auth/register";
        } catch (DataIntegrityViolationException ex) {
            log.error("Registration data integrity violation", ex);
            bindingResult.reject("registrationGlobal", userMessageForDataIntegrity(ex));
            return "auth/register";
        } catch (DataAccessException ex) {
            log.error("Registration database error", ex);
            bindingResult.reject("registrationGlobal", UserFacingMessages.REGISTRATION_SAVE_FAILED_GENERIC);
            return "auth/register";
        } catch (PersistenceException ex) {
            log.error("Registration persistence error", ex);
            bindingResult.reject("registrationGlobal", UserFacingMessages.REGISTRATION_SAVE_FAILED_GENERIC);
            return "auth/register";
        } catch (IllegalArgumentException ex) {
            log.warn("Registration invalid argument (often user id format)", ex);
            bindingResult.reject(
                    "registrationGlobal",
                    "Your registration session was invalid. Refresh the registration page and try again.");
            session.setAttribute(
                    SESSION_PENDING_USER_ID,
                    registrationService.ensurePendingPlainUserIdForSession(null));
            ensurePreviewUserId(session, model);
            return "auth/register";
        } catch (IllegalStateException ex) {
            log.error("Registration illegal state (often encryption or configuration)", ex);
            bindingResult.reject("registrationGlobal", UserFacingMessages.REGISTRATION_SAVE_FAILED_GENERIC);
            return "auth/register";
        } catch (Exception ex) {
            log.error("Registration failed unexpectedly: {} — {}", ex.getClass().getName(), ex.getMessage(), ex);
            bindingResult.reject("registrationGlobal", UserFacingMessages.REGISTRATION_SAVE_FAILED_GENERIC);
            return "auth/register";
        }
        session.removeAttribute(SESSION_PENDING_USER_ID);
        String successMessage =
                form.getRole() == UserRole.ADMIN
                        ? "You're all set. Sign in with your email and password."
                        : "Thanks for registering. An administrator will review your account; you'll be able to sign"
                                + " in once it is activated.";
        redirectAttributes.addFlashAttribute("registrationSuccessMessage", successMessage);
        redirectAttributes.addFlashAttribute("registeredPlainUserId", assignedPlainUserId);
        return "redirect:/register/complete";
    }

    private void ensurePreviewUserId(HttpSession session, Model model) {
        String current = (String) session.getAttribute(SESSION_PENDING_USER_ID);
        String id = registrationService.ensurePendingPlainUserIdForSession(current);
        session.setAttribute(SESSION_PENDING_USER_ID, id);
        model.addAttribute("previewUserId", id);
    }

    private static String userMessageForDataIntegrity(DataIntegrityViolationException ex) {
        Throwable root = ex.getMostSpecificCause();
        String msg = root != null ? root.getMessage() : ex.getMessage();
        if (msg == null) {
            return UserFacingMessages.REGISTRATION_SAVE_FAILED_GENERIC;
        }
        String m = msg.toLowerCase(Locale.ROOT);
        if (m.contains("email")) {
            return UserFacingMessages.REGISTRATION_DUPLICATE_EMAIL;
        }
        if (m.contains("too long")
                || m.contains("data truncated")
                || m.contains("column")
                || m.contains("cannot be null")) {
            return UserFacingMessages.REGISTRATION_SAVE_FAILED_SCHEMA;
        }
        return UserFacingMessages.REGISTRATION_SAVE_FAILED_GENERIC;
    }
}
