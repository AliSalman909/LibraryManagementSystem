package com.library.service;

import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import com.library.dto.RegistrationForm;
import com.library.entity.AdminProfile;
import com.library.entity.Librarian;
import com.library.entity.RegistrationRequest;
import com.library.entity.Student;
import com.library.entity.User;
import com.library.entity.enums.AccountStatus;
import com.library.entity.enums.RequestStatus;
import com.library.entity.enums.UserRole;
import com.library.exception.BusinessRuleException;
import com.library.repository.AdminProfileRepository;
import com.library.repository.LibrarianRepository;
import com.library.repository.RegistrationRequestRepository;
import com.library.repository.StudentRepository;
import com.library.repository.UserRepository;
import com.library.security.PasswordEncryptionService;
import com.library.security.UserIdEncryptionService;

@Service
public class RegistrationService {

    /** Unambiguous characters for 6-char ids (no I, O, 0, 1). */
    private static final String PLAIN_ID_ALPHABET = "ABCDEFGHJKLMNPQRSTUVWXYZ23456789";

    private final UserRepository userRepository;
    private final StudentRepository studentRepository;
    private final LibrarianRepository librarianRepository;
    private final AdminProfileRepository adminProfileRepository;
    private final RegistrationRequestRepository registrationRequestRepository;
    private final PasswordEncryptionService passwordEncryptionService;
    private final UserIdEncryptionService userIdEncryptionService;
    private final ProfilePictureStorageService profilePictureStorageService;
    private final JdbcTemplate jdbcTemplate;

    // Prevents race conditions where two concurrent ADMIN registrations both pass the "no admin exists" check.
    private static final String ADMIN_SINGLETON_LOCK = "library_admin_singleton";

    public RegistrationService(
            UserRepository userRepository,
            StudentRepository studentRepository,
            LibrarianRepository librarianRepository,
            AdminProfileRepository adminProfileRepository,
            RegistrationRequestRepository registrationRequestRepository,
            PasswordEncryptionService passwordEncryptionService,
            UserIdEncryptionService userIdEncryptionService,
            ProfilePictureStorageService profilePictureStorageService,
            JdbcTemplate jdbcTemplate) {
        this.userRepository = userRepository;
        this.studentRepository = studentRepository;
        this.librarianRepository = librarianRepository;
        this.adminProfileRepository = adminProfileRepository;
        this.registrationRequestRepository = registrationRequestRepository;
        this.passwordEncryptionService = passwordEncryptionService;
        this.userIdEncryptionService = userIdEncryptionService;
        this.profilePictureStorageService = profilePictureStorageService;
        this.jdbcTemplate = jdbcTemplate;
    }

    /**
     * Ensures the session holds a 6-character plain id that is not already taken ({@code users.user_id} is the same
     * value).
     *
     * @param currentFromSession may be null or invalid — a new id is allocated
     * @return normalized plain id to store in session and show on the form
     */
    @SuppressWarnings("null")
    @Transactional(readOnly = true)
    public String ensurePendingPlainUserIdForSession(String currentFromSession) {
        if (userIdEncryptionService.isValidPlainUserId(currentFromSession)) {
            String normalized = userIdEncryptionService.normalizePlainUserId(currentFromSession);
            if (!userRepository.existsById(userIdEncryptionService.encryptUserId(normalized))) {
                return normalized;
            }
        }
        return generateUniquePlainUserId();
    }

    /**
     * Validates the id from the session immediately before save (handles rare races).
     *
     * @return normalized plain user id for {@link #register}
     */
    @SuppressWarnings("null")
    @Transactional(readOnly = true)
    public String validateSessionPlainUserIdForSubmit(String fromSession) {
        if (!userIdEncryptionService.isValidPlainUserId(fromSession)) {
            throw new BusinessRuleException(
                    "Your registration session is missing a valid user ID. Refresh the registration page and try again.");
        }
        String normalized = userIdEncryptionService.normalizePlainUserId(fromSession);
        if (userRepository.existsById(userIdEncryptionService.encryptUserId(normalized))) {
            throw new BusinessRuleException(
                    "That user ID is no longer available. Refresh the registration page to get a new one.");
        }
        return normalized;
    }

    @SuppressWarnings("null")
    private String generateUniquePlainUserId() {
        ThreadLocalRandom rng = ThreadLocalRandom.current();
        for (int attempt = 0; attempt < 200; attempt++) {
            StringBuilder sb = new StringBuilder(UserIdEncryptionService.PLAIN_USER_ID_LENGTH);
            for (int i = 0; i < UserIdEncryptionService.PLAIN_USER_ID_LENGTH; i++) {
                sb.append(PLAIN_ID_ALPHABET.charAt(rng.nextInt(PLAIN_ID_ALPHABET.length())));
            }
            String candidate = sb.toString();
            if (!userRepository.existsById(userIdEncryptionService.encryptUserId(candidate))) {
                return candidate;
            }
        }
        throw new IllegalStateException("Could not allocate a unique user id");
    }

    /**
     * @param plainUserId six-character id from the session (normalized uppercase)
     * @return the same plain id for confirmation pages
     */
    @Transactional
    public String register(
            RegistrationForm form,
            MultipartFile profilePictureFile,
            double profilePictureFocalX,
            double profilePictureFocalY,
            String plainUserId) {
        if (!userIdEncryptionService.isValidPlainUserId(plainUserId)) {
            throw new BusinessRuleException(
                    "Your registration session is missing a valid user ID. Refresh the registration page and try again.");
        }
        plainUserId = userIdEncryptionService.normalizePlainUserId(plainUserId);

        String email = form.getEmail().trim();
        if (userRepository.existsByEmailIgnoreCase(email)) {
            throw new BusinessRuleException(
                    "An account with this email already exists. Try signing in, or use a different email address.");
        }

        UserRole role = form.getRole();

        boolean creatingFirstAdmin = false;
        boolean adminLockHeld = false;
        String profilePicturePath = null;

        try {
            if (role == UserRole.ADMIN) {
                // Advisory DB lock so only one request can run the "first admin" check+save at a time.
                Integer lockAcquired =
                        jdbcTemplate.queryForObject(
                                "SELECT GET_LOCK(?, 10)", Integer.class, ADMIN_SINGLETON_LOCK);
                adminLockHeld = lockAcquired != null && lockAcquired == 1;
                if (!adminLockHeld) {
                    throw new BusinessRuleException(
                            "Admin registration is in progress. Please try again in a moment.");
                }

                creatingFirstAdmin = adminProfileRepository.count() == 0;
                // Re-run validation under the lock so concurrent requests cannot both pass.
                validateAdmin();
            } else {
                switch (role) {
                    case STUDENT -> validateStudent(form);
                    case LIBRARIAN -> validateLibrarian();
                    default -> throw new BusinessRuleException("Unsupported role.");
                }
            }

            String storedUserId =
                    userIdEncryptionService.encryptUserId(plainUserId); // stored = plain normalized id
            profilePicturePath =
                    profilePictureStorageService.store(profilePictureFile, storedUserId);

            User user = new User();
            user.setUserId(storedUserId);
            user.setFullName(form.getFullName().trim());
            user.setEmail(email);
            user.setPasswordHash(passwordEncryptionService.encrypt(form.getPassword()));
            user.setUserRole(role);
            user.setAccountStatus(
                    creatingFirstAdmin ? AccountStatus.active : AccountStatus.pending);
            user.setPhoneNumber(
                    StringUtils.hasText(form.getPhoneNumber()) ? form.getPhoneNumber().trim() : null);
            user.setProfilePicture(profilePicturePath);
            if (StringUtils.hasText(profilePicturePath)) {
                user.setProfilePictureFocalX(clampFocalPercent(profilePictureFocalX));
                user.setProfilePictureFocalY(clampFocalPercent(profilePictureFocalY));
            }

            user = userRepository.save(user);

            switch (role) {
                case STUDENT -> persistStudent(user, form);
                case LIBRARIAN -> persistLibrarian(user);
                case ADMIN -> persistAdmin(user, form);
                default -> {
                }
            }

            RegistrationRequest reg = new RegistrationRequest();
            reg.setRequestId(UUID.randomUUID().toString());
            reg.setUser(user);
            reg.setStatus(
                    creatingFirstAdmin ? RequestStatus.approved : RequestStatus.pending);
            reg.setReviewedAt(creatingFirstAdmin ? java.time.Instant.now() : null);
            registrationRequestRepository.save(reg);
            return plainUserId;
        } catch (BusinessRuleException e) {
            // Caller-friendly error; no need to wrap.
            throw e;
        } catch (DataIntegrityViolationException e) {
            // Transaction will roll back; remove uploaded file to avoid orphan images.
            profilePictureStorageService.deleteByWebPath(profilePicturePath);
            throw new BusinessRuleException(
                    "Registration failed due to a uniqueness constraint. Try signing in or use another email.");
        } catch (RuntimeException e) {
            // Transaction will roll back; remove uploaded file to avoid orphan images.
            profilePictureStorageService.deleteByWebPath(profilePicturePath);
            throw e;
        } finally {
            if (adminLockHeld) {
                jdbcTemplate.update("SELECT RELEASE_LOCK(?)", ADMIN_SINGLETON_LOCK);
            }
        }
    }

    private static double clampFocalPercent(double value) {
        if (Double.isNaN(value) || Double.isInfinite(value)) {
            return 50.0;
        }
        return Math.min(100.0, Math.max(0.0, value));
    }

    private void validateStudent(RegistrationForm form) {
        if (!StringUtils.hasText(form.getProgram())
                || !RegistrationForm.ALLOWED_STUDENT_PROGRAMS.contains(form.getProgram())) {
            throw new BusinessRuleException("Please select a valid study program.");
        }
    }

    private void validateLibrarian() {
        // No extra uniqueness checks needed after removing employee_id from schema.
    }

    private void validateAdmin() {
        if (adminProfileRepository.count() > 0) {
            throw new BusinessRuleException(
                    "This library already has an administrator account. New administrators cannot be registered through"
                            + " this form.");
        }
    }

    private void persistStudent(User user, RegistrationForm form) {
        Student s = new Student();
        s.setUser(user);
        // Required by legacy schemas that still enforce a NOT NULL students.student_id column.
        s.setStudentId("S-" + UUID.randomUUID().toString().substring(0, 12).toUpperCase());
        s.setProgram(form.getProgram().trim());
        s.setEnrollmentDate(form.getEnrollmentDate());
        s.setDateOfBirth(form.getDateOfBirth());
        s.setMaxBorrowLimit(3);
        s.setCanBorrow(true);
        studentRepository.save(s);
    }

    private void persistLibrarian(User user) {
        Librarian l = new Librarian();
        l.setUser(user);
        l.setEmployeeId("L-" + UUID.randomUUID().toString().substring(0, 12).toUpperCase());
        l.setCanApproveBorrowing(true);
        librarianRepository.save(l);
    }

    private void persistAdmin(User user, RegistrationForm form) {
        AdminProfile a = new AdminProfile();
        a.setUser(user);
        a.setDepartment(StringUtils.hasText(form.getDepartment()) ? form.getDepartment().trim() : null);
        a.setEmployeeId("A-" + UUID.randomUUID().toString().substring(0, 12).toUpperCase());
        a.setCanManageUsers(true);
        a.setCanViewReports(true);
        a.setCanManageCatalog(true);
        adminProfileRepository.save(a);
    }
}
