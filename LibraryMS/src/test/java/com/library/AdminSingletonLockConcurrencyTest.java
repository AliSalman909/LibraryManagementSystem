package com.library;

import com.library.dto.RegistrationForm;
import com.library.entity.enums.UserRole;
import com.library.exception.BusinessRuleException;
import com.library.repository.AdminProfileRepository;
import com.library.service.RegistrationService;
import com.library.security.UserIdEncryptionService;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfSystemProperty;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.mock.web.MockMultipartFile;

@SpringBootTest
@EnabledIfSystemProperty(named = "RUN_ADMIN_SINGLETON_LOCK_TEST", matches = "true")
public class AdminSingletonLockConcurrencyTest {

    @Autowired private RegistrationService registrationService;

    @Autowired private AdminProfileRepository adminProfileRepository;

    @Autowired private JdbcTemplate jdbcTemplate;

    @Autowired private UserIdEncryptionService userIdEncryptionService;

    @Test
    void adminRegistrationIsSerializedByAdvisoryLock() throws Exception {
        boolean adminAlreadyExists = adminProfileRepository.count() > 0;

        // Use distinct emails + distinct user ids so uniqueness constraints won't be the reason for failure.
        String generatedUserId1 = randomPlainUserId6();
        String generatedUserId2 = randomPlainUserId6();
        while (generatedUserId2.equals(generatedUserId1)) {
            generatedUserId2 = randomPlainUserId6();
        }
        final String plainUserId1 = generatedUserId1;
        final String plainUserId2 = generatedUserId2;

        String userId1 = userIdEncryptionService.encryptUserId(plainUserId1);
        String userId2 = userIdEncryptionService.encryptUserId(plainUserId2);

        String email1 = "admin1." + System.nanoTime() + "@example.com";
        String email2 = "admin2." + System.nanoTime() + "@example.com";

        RegistrationForm form1 = buildAdminForm("Admin One", email1, "Password123A", "Dept");
        RegistrationForm form2 = buildAdminForm("Admin Two", email2, "Password123A", "Dept");

        // Empty file so ProfilePictureStorageService.store(...) returns null.
        MockMultipartFile emptyFile = new MockMultipartFile("profilePicture", "empty.png", "image/png", new byte[0]);

        ExecutorService exec = Executors.newFixedThreadPool(2);
        List<Callable<String>> tasks = new ArrayList<>();
        tasks.add(() -> registrationService.register(form1, emptyFile, 50.0, 50.0, plainUserId1));
        tasks.add(() -> registrationService.register(form2, emptyFile, 50.0, 50.0, plainUserId2));

        try {
            Future<String> f1 = exec.submit(tasks.get(0));
            Future<String> f2 = exec.submit(tasks.get(1));

            // If the advisory lock isn't released correctly on exceptions, one of these will time out / hang.
            int successes = 0;
            List<Throwable> failures = new ArrayList<>();

            try {
                f1.get(20, TimeUnit.SECONDS);
                successes++;
            } catch (ExecutionException ee) {
                failures.add(ee.getCause());
            }

            try {
                f2.get(20, TimeUnit.SECONDS);
                successes++;
            } catch (ExecutionException ee) {
                failures.add(ee.getCause());
            }

            // Verify failure type(s)
            for (Throwable t : failures) {
                Assertions.assertTrue(
                        t instanceof BusinessRuleException || t instanceof DataIntegrityViolationException,
                        "Expected BusinessRuleException or DataIntegrityViolationException but got: "
                                + (t == null ? "null" : t.getClass().getName()));
            }

            if (adminAlreadyExists) {
                Assertions.assertEquals(0, successes, "No admin should be created when one already exists.");
            } else {
                Assertions.assertEquals(
                        1,
                        successes,
                        "Exactly one registration should succeed when there was no admin before the test.");
            }
        } finally {
            exec.shutdownNow();

            // Best-effort cleanup to avoid leaving test rows behind.
            // (The foreign keys use ON DELETE CASCADE / SET NULL, so deleting users should remove dependent rows.)
            jdbcTemplate.update("DELETE FROM users WHERE user_id IN (?, ?)", userId1, userId2);
        }
    }

    private static RegistrationForm buildAdminForm(
            String fullName, String email, String password, String department) {
        RegistrationForm form = new RegistrationForm();
        form.setFullName(fullName);
        form.setEmail(email);
        form.setPassword(password);
        form.setConfirmPassword(password);
        form.setRole(UserRole.ADMIN);
        form.setDepartment(department);
        return form;
    }

    // Must be 6 alphanumeric characters (matches UserIdEncryptionService validation).
    private static String randomPlainUserId6() {
        String alphabet = "ABCDEFGHJKLMNPQRSTUVWXYZ23456789";
        StringBuilder sb = new StringBuilder(6);
        for (int i = 0; i < 6; i++) {
            int idx = (int) (Math.random() * alphabet.length());
            sb.append(alphabet.charAt(idx));
        }
        return sb.toString();
    }
}

