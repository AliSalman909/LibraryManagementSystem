CREATE DATABASE IF NOT EXISTS library_db
  CHARACTER SET utf8mb4
  COLLATE utf8mb4_unicode_ci;

USE library_db;

CREATE TABLE users (
  user_id         CHAR(36)        NOT NULL DEFAULT (UUID()),
  full_name       VARCHAR(100)    NOT NULL,
  email           VARCHAR(150)    NOT NULL,
  password_hash   VARCHAR(255)    NOT NULL,
  role            ENUM('ADMIN', 'LIBRARIAN', 'STUDENT') NOT NULL,
  account_status  ENUM(
                    'pending',
                    'active',
                    'rejected',
                    'suspended',
                    'deletion_pending',
                    'deleted'
                  )               NOT NULL DEFAULT 'pending',
  phone_number    VARCHAR(20),
  profile_picture VARCHAR(500),
  created_at      TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at      TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  last_login_at   TIMESTAMP       NULL,

  CONSTRAINT pk_users        PRIMARY KEY (user_id),
  CONSTRAINT uq_users_email  UNIQUE      (email)
);

CREATE TABLE students (
  user_id          CHAR(36)     NOT NULL,
  student_id       VARCHAR(30)  NOT NULL,
  program          VARCHAR(100) NOT NULL,    
  enrollment_date  DATE,                  
  date_of_birth    DATE,
  max_borrow_limit INT          NOT NULL DEFAULT 3,
  can_borrow       BOOLEAN      NOT NULL DEFAULT TRUE,

  CONSTRAINT pk_students            PRIMARY KEY (user_id),
  CONSTRAINT uq_students_sid        UNIQUE      (student_id),
  CONSTRAINT fk_students_user       FOREIGN KEY (user_id) REFERENCES users(user_id) ON DELETE CASCADE,
  CONSTRAINT chk_borrow_limit       CHECK       (max_borrow_limit > 0 AND max_borrow_limit <= 10)
);

CREATE TABLE librarians (
  user_id               CHAR(36)     NOT NULL,
  employee_id           VARCHAR(30)  NOT NULL,
  can_approve_borrowing BOOLEAN      NOT NULL DEFAULT TRUE,

  CONSTRAINT pk_librarians        PRIMARY KEY (user_id),
  CONSTRAINT uq_librarians_eid    UNIQUE      (employee_id),
  CONSTRAINT fk_librarians_user   FOREIGN KEY (user_id) REFERENCES users(user_id) ON DELETE CASCADE
);

CREATE TABLE admins (
  user_id              CHAR(36)     NOT NULL,
  employee_id          VARCHAR(30)  NOT NULL,
  department           VARCHAR(100),
  can_manage_users     BOOLEAN      NOT NULL DEFAULT TRUE,
  can_view_reports     BOOLEAN      NOT NULL DEFAULT TRUE,
  can_manage_catalog   BOOLEAN      NOT NULL DEFAULT TRUE,

  CONSTRAINT pk_admins         PRIMARY KEY (user_id),
  CONSTRAINT uq_admins_eid     UNIQUE      (employee_id),
  CONSTRAINT fk_admins_user    FOREIGN KEY (user_id) REFERENCES users(user_id) ON DELETE CASCADE
);

CREATE TABLE registration_requests (
  request_id        CHAR(36)     NOT NULL DEFAULT (UUID()),
  user_id           CHAR(36)     NOT NULL,
  status            ENUM(
                      'pending',
                      'approved',
                      'rejected'
                    )            NOT NULL DEFAULT 'pending',
  submitted_at      TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
  reviewed_at       TIMESTAMP    NULL,
  rejection_reason  VARCHAR(255),

  CONSTRAINT pk_reg_requests        PRIMARY KEY (request_id),
  CONSTRAINT uq_reg_requests_user   UNIQUE      (user_id),
  CONSTRAINT fk_reg_requests_user   FOREIGN KEY (user_id)      REFERENCES users(user_id) ON DELETE CASCADE
);

CREATE TABLE deletion_requests (
  deletion_id   CHAR(36)     NOT NULL DEFAULT (UUID()),
  user_id       CHAR(36)     NOT NULL,
  reason        TEXT,
  status        ENUM(
                  'pending',
                  'approved',
                  'rejected'
                )            NOT NULL DEFAULT 'pending',
  requested_at  TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
  reviewed_at   TIMESTAMP    NULL,
  admin_notes   TEXT,

  CONSTRAINT pk_deletion_requests        PRIMARY KEY (deletion_id),
  CONSTRAINT fk_deletion_requests_user   FOREIGN KEY (user_id)      REFERENCES users(user_id) ON DELETE CASCADE
);

CREATE TABLE notifications (
  notification_id  CHAR(36)      NOT NULL DEFAULT (UUID()),
  recipient_id     CHAR(36)      NOT NULL,
  sender_id        CHAR(36),
  type             ENUM(
                     'account_approved',
                     'account_rejected',
                     'deletion_requested',
                     'deletion_approved',
                     'deletion_rejected',
                     'account_suspended'
                   )             NOT NULL,
  title            VARCHAR(150)  NOT NULL,
  message          TEXT          NOT NULL,
  is_read          BOOLEAN       NOT NULL DEFAULT FALSE,
  created_at       TIMESTAMP     NOT NULL DEFAULT CURRENT_TIMESTAMP,
  read_at          TIMESTAMP     NULL,

  CONSTRAINT pk_notifications           PRIMARY KEY (notification_id),
  CONSTRAINT fk_notifications_recipient FOREIGN KEY (recipient_id) REFERENCES users(user_id) ON DELETE CASCADE,
  CONSTRAINT fk_notifications_sender    FOREIGN KEY (sender_id)    REFERENCES users(user_id) ON DELETE SET NULL
);

USE library_db;
SHOW TABLES;
