CREATE DATABASE IF NOT EXISTS library_db
 CHARACTER SET utf8mb4
 COLLATE utf8mb4_unicode_ci;

USE library_db;

-- -----------------------------------------------------------------------------
-- users
-- -----------------------------------------------------------------------------
CREATE TABLE users (
  user_id                  VARCHAR(512)   NOT NULL,
  full_name                VARCHAR(100)   NOT NULL,
  email                    VARCHAR(150)   NOT NULL,
  password_hash            VARCHAR(512)   NOT NULL,
  user_role                ENUM('ADMIN', 'LIBRARIAN', 'STUDENT') NOT NULL,
  account_status           ENUM('pending', 'active', 'rejected', 'suspended', 'deletion_pending', 'deleted' )    NOT NULL DEFAULT 'pending',
  phone_number             VARCHAR(20)    NULL,
  profile_picture          VARCHAR(500)   NULL,
  profile_picture_focal_x  DOUBLE         NULL,
  profile_picture_focal_y  DOUBLE         NULL,
  created_at               DATETIME(6)    NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
  updated_at               DATETIME(6)    NOT NULL DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6),
  last_login_at            DATETIME(6)    NULL,

  CONSTRAINT pk_users        PRIMARY KEY (user_id),
  CONSTRAINT uq_users_email  UNIQUE (email)
);

-- -----------------------------------------------------------------------------
-- students
-- -----------------------------------------------------------------------------
CREATE TABLE students (
  user_id            VARCHAR(512)   NOT NULL,
  program            VARCHAR(100)   NOT NULL,
  enrollment_date    DATE           NULL,
  date_of_birth      DATE           NULL,
  max_borrow_limit   INT            NOT NULL DEFAULT 3,
  can_borrow         BOOLEAN        NOT NULL DEFAULT TRUE,

  CONSTRAINT pk_students        PRIMARY KEY (user_id),
  CONSTRAINT fk_students_user   FOREIGN KEY (user_id) REFERENCES users (user_id) ON DELETE CASCADE,
  CONSTRAINT chk_students_borrow_limit CHECK (max_borrow_limit > 0 AND max_borrow_limit <= 10)
);

-- -----------------------------------------------------------------------------
-- librarians
-- -----------------------------------------------------------------------------
CREATE TABLE librarians (
  user_id                 VARCHAR(512)   NOT NULL,
  can_approve_borrowing   BOOLEAN        NOT NULL DEFAULT TRUE,

  CONSTRAINT pk_librarians       PRIMARY KEY (user_id),
  CONSTRAINT fk_librarians_user  FOREIGN KEY (user_id) REFERENCES users (user_id) ON DELETE CASCADE
);

-- -----------------------------------------------------------------------------
-- admins  (entity class: AdminProfile)
-- -----------------------------------------------------------------------------
CREATE TABLE admins (
  user_id              VARCHAR(512)   NOT NULL,
  department           VARCHAR(100)   NULL,
  can_manage_users     BOOLEAN        NOT NULL DEFAULT TRUE,
  can_view_reports     BOOLEAN        NOT NULL DEFAULT TRUE,
  can_manage_catalog   BOOLEAN        NOT NULL DEFAULT TRUE,

  CONSTRAINT pk_admins       PRIMARY KEY (user_id),
  CONSTRAINT fk_admins_user  FOREIGN KEY (user_id) REFERENCES users (user_id) ON DELETE CASCADE
);

-- -----------------------------------------------------------------------------
-- registration_requests  (RequestStatus: pending, approved, rejected)
-- request_id set in application code (UUID string).
-- -----------------------------------------------------------------------------
CREATE TABLE registration_requests (
  request_id        CHAR(36)      NOT NULL,
  user_id           VARCHAR(512)  NOT NULL,
  status            ENUM('pending', 'approved', 'rejected') NOT NULL DEFAULT 'pending',
  submitted_at      DATETIME(6)   NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
  reviewed_at       DATETIME(6)   NULL,
  rejection_reason  VARCHAR(255)  NULL,

  CONSTRAINT pk_reg_requests        PRIMARY KEY (request_id),
  CONSTRAINT uq_reg_requests_user   UNIQUE (user_id),
  CONSTRAINT fk_reg_requests_user   FOREIGN KEY (user_id) REFERENCES users (user_id) ON DELETE CASCADE
);

-- -----------------------------------------------------------------------------
-- deletion_requests
-- deletion_id assigned when rows are created in code.
-- -----------------------------------------------------------------------------
CREATE TABLE deletion_requests (
  deletion_id    CHAR(36)      NOT NULL,
  user_id        VARCHAR(512)  NOT NULL,
  reason         TEXT          NULL,
  status         ENUM('pending', 'approved', 'rejected') NOT NULL DEFAULT 'pending',
  requested_at   DATETIME(6)   NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
  reviewed_at    DATETIME(6)   NULL,
  admin_notes    TEXT          NULL,

  CONSTRAINT pk_deletion_requests       PRIMARY KEY (deletion_id),
  CONSTRAINT fk_deletion_requests_user  FOREIGN KEY (user_id) REFERENCES users (user_id) ON DELETE CASCADE
);

-- -----------------------------------------------------------------------------
-- notifications  (NotificationType enum names as stored values)
-- notification_id set in application code.
-- -----------------------------------------------------------------------------
CREATE TABLE notifications (
  notification_id  CHAR(36)      NOT NULL,
  recipient_id     VARCHAR(512)  NOT NULL,
  sender_id        VARCHAR(512)  NULL,
  type             ENUM( 'account_approved', 'account_rejected', 'deletion_requested', 'deletion_approved', 'deletion_rejected', 'account_suspended' )    NOT NULL,
  title             VARCHAR(150)  NOT NULL,
  message           TEXT          NOT NULL,
  is_read           BOOLEAN       NOT NULL DEFAULT FALSE,
  created_at        DATETIME(6)   NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
  read_at           DATETIME(6)   NULL,

  CONSTRAINT pk_notifications            PRIMARY KEY (notification_id),
  CONSTRAINT fk_notifications_recipient FOREIGN KEY (recipient_id) REFERENCES users (user_id) ON DELETE CASCADE,
  CONSTRAINT fk_notifications_sender    FOREIGN KEY (sender_id)    REFERENCES users (user_id) ON DELETE SET NULL
);
