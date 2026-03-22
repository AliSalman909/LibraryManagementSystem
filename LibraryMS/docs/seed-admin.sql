-- Optional bootstrap: one ACTIVE admin (password: password) using legacy bcrypt.
-- Login still supports bcrypt for this row; new registrations use AES encryption (see application.properties).
-- Run after CREATE TABLE statements. Change email as needed.

SET @admin_uid = UUID();

INSERT INTO users (
  user_id, full_name, email, password_hash, user_role, account_status,
  phone_number, profile_picture
) VALUES (
  @admin_uid,
  'System Administrator',
  'admin@library.local',
  '$2a$10$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Ro9llC/.og/at2.uheWG/igi',
  'ADMIN',
  'active',
  NULL,
  NULL
);

INSERT INTO admins (
  user_id, employee_id, department, can_manage_users, can_view_reports, can_manage_catalog
) VALUES (
  @admin_uid,
  'ADM-BOOT-1',
  'IT',
  TRUE,
  TRUE,
  TRUE
);

INSERT INTO registration_requests (
  request_id, user_id, status, reviewed_at
) VALUES (
  UUID(),
  @admin_uid,
  'approved',
  CURRENT_TIMESTAMP
);
