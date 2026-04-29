-- =========================================================
-- Library Management System - Canonical MySQL Schema
-- Compatible with current project codebase
-- =========================================================
-- How to run whole schema at once
-- MySQL CLI:
-- mysql -u root -p < your_schema.sql
-- or inside mysql shell: SOURCE /full/path/your_schema.sql;
-- MySQL Workbench:
-- Open the script and click Run All (lightning bolt)
-- we can absolutely execute the whole schema in one shot safely.

-- If the script is written in correct dependency order (parent tables first), FK errors won’t happen.
-- For rerunnable scripts, this pattern is standard:

-- SET FOREIGN_KEY_CHECKS = 0;
-- DROP TABLE ... (children/parents any order)
-- SET FOREIGN_KEY_CHECKS = 1;
-- then CREATE TABLE ... in normal order
-- So No GO in MySQL to execute in a batch like SQL Server, but we can absolutely execute the whole schema in one shot safely.
-- Also we have export the schema of library_db (contains the DDL Commands) directly from my sql that is also available in docs folder

CREATE DATABASE IF NOT EXISTS library_db
  CHARACTER SET utf8mb4
  COLLATE utf8mb4_unicode_ci;

USE library_db;

-- ---------------------------------------------------------
-- Optional cleanup (makes script rerunnable safely)
-- ---------------------------------------------------------
SET FOREIGN_KEY_CHECKS = 0;

DROP TABLE IF EXISTS deletion_requests;
DROP TABLE IF EXISTS notifications;
DROP TABLE IF EXISTS registration_requests;
DROP TABLE IF EXISTS fines;
DROP TABLE IF EXISTS reservations;
DROP TABLE IF EXISTS borrow_records;
DROP TABLE IF EXISTS borrow_requests;
DROP TABLE IF EXISTS book_copies;
DROP TABLE IF EXISTS books;
DROP TABLE IF EXISTS students;
DROP TABLE IF EXISTS librarians;
DROP TABLE IF EXISTS admins;
DROP TABLE IF EXISTS users;

SET FOREIGN_KEY_CHECKS = 1;

-- =========================================================
-- Core user tables
-- =========================================================

CREATE TABLE users (
  user_id                  VARCHAR(512) NOT NULL,
  full_name                VARCHAR(100) NOT NULL,
  email                    VARCHAR(150) NOT NULL,
  password_hash            VARCHAR(512) NOT NULL,
  user_role                ENUM('ADMIN','LIBRARIAN','STUDENT') NOT NULL,
  account_status           ENUM('active','deleted','deletion_pending','pending','rejected','suspended') NOT NULL,
  phone_number             VARCHAR(20) NULL,
  profile_picture          VARCHAR(500) NULL,
  profile_picture_focal_x  DOUBLE NULL,
  profile_picture_focal_y  DOUBLE NULL,
  created_at               DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
  updated_at               DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6),
  last_login_at            DATETIME(6) NULL,
  CONSTRAINT pk_users PRIMARY KEY (user_id),
  CONSTRAINT uq_users_email UNIQUE (email)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE admins (
  user_id             VARCHAR(512) NOT NULL,
  department          VARCHAR(100) NULL,
  employee_id         VARCHAR(64) NOT NULL,
  can_manage_users    TINYINT(1) NOT NULL DEFAULT 1,
  can_view_reports    TINYINT(1) NOT NULL DEFAULT 1,
  can_manage_catalog  TINYINT(1) NOT NULL DEFAULT 1,
  CONSTRAINT pk_admins PRIMARY KEY (user_id),
  CONSTRAINT fk_admins_user FOREIGN KEY (user_id)
    REFERENCES users(user_id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE librarians (
  user_id                VARCHAR(512) NOT NULL,
  employee_id            VARCHAR(64) NOT NULL,
  can_approve_borrowing  TINYINT(1) NOT NULL DEFAULT 1,
  CONSTRAINT pk_librarians PRIMARY KEY (user_id),
  CONSTRAINT fk_librarians_user FOREIGN KEY (user_id)
    REFERENCES users(user_id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE students (
  user_id            VARCHAR(512) NOT NULL,
  student_id         VARCHAR(64) NOT NULL,
  program            VARCHAR(100) NOT NULL,
  enrollment_date    DATE NULL,
  date_of_birth      DATE NULL,
  max_borrow_limit   INT NOT NULL DEFAULT 3,
  can_borrow         TINYINT(1) NOT NULL DEFAULT 1,
  CONSTRAINT pk_students PRIMARY KEY (user_id),
  CONSTRAINT fk_students_user FOREIGN KEY (user_id)
    REFERENCES users(user_id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- =========================================================
-- Catalog and copies
-- =========================================================

CREATE TABLE books (
  book_id            VARCHAR(5) NOT NULL,
  title              VARCHAR(200) NOT NULL,
  author             VARCHAR(150) NOT NULL,
  isbn               VARCHAR(64) NULL,
  category           VARCHAR(100) NULL,
  total_copies       INT NOT NULL,
  available_copies   INT NOT NULL,
  created_at         DATETIME(6) NOT NULL,
  updated_at         DATETIME(6) NOT NULL,
  fine_per_day       DECIMAL(10,2) NOT NULL,
  fine_per_day_pkr   INT NOT NULL,
  max_borrow_days    INT NOT NULL DEFAULT 28,
  CONSTRAINT pk_books PRIMARY KEY (book_id),
  CONSTRAINT uq_books_isbn UNIQUE (isbn),
  CONSTRAINT uq_books_title_author_category UNIQUE (title, author, category)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE book_copies (
  copy_id        VARCHAR(64) NOT NULL,
  book_id        VARCHAR(5) NOT NULL,
  isbn_code      VARCHAR(96) NOT NULL,
  copy_number    INT NOT NULL,
  is_available   TINYINT(1) NOT NULL DEFAULT 1,
  CONSTRAINT pk_book_copies PRIMARY KEY (copy_id),
  CONSTRAINT uq_book_copies_isbn_code UNIQUE (isbn_code),
  CONSTRAINT uq_book_copies_book_copy_number UNIQUE (book_id, copy_number),
  CONSTRAINT fk_book_copies_book FOREIGN KEY (book_id)
    REFERENCES books(book_id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- =========================================================
-- Borrow request / issue / return flow
-- =========================================================

CREATE TABLE borrow_requests (
  request_id                  VARCHAR(64) NOT NULL,
  book_id                     VARCHAR(5) NOT NULL,
  student_id                  VARCHAR(512) NOT NULL,
  processed_by_librarian_id   VARCHAR(512) NULL,
  status                      ENUM('APPROVED','CANCELLED','PENDING','REJECTED') NOT NULL,
  requested_at                DATETIME(6) NOT NULL,
  reviewed_at                 DATETIME(6) NULL,
  due_date                    DATE NULL,
  requested_duration_days     INT NOT NULL DEFAULT 14,
  CONSTRAINT pk_borrow_requests PRIMARY KEY (request_id),
  CONSTRAINT fk_borrow_requests_book FOREIGN KEY (book_id)
    REFERENCES books(book_id) ON DELETE CASCADE,
  CONSTRAINT fk_borrow_requests_student FOREIGN KEY (student_id)
    REFERENCES students(user_id) ON DELETE CASCADE,
  CONSTRAINT fk_borrow_requests_processed_by_librarian FOREIGN KEY (processed_by_librarian_id)
    REFERENCES librarians(user_id) ON DELETE SET NULL,
  CONSTRAINT chk_borrow_requests_duration
    CHECK (requested_duration_days IN (7, 14, 21, 28))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE borrow_records (
  record_id                  VARCHAR(64) NOT NULL,
  request_id                 VARCHAR(64) NOT NULL,
  book_id                    VARCHAR(5) NOT NULL,
  copy_id                    VARCHAR(64) NULL,
  student_id                 VARCHAR(512) NOT NULL,
  issued_by_librarian_id     VARCHAR(512) NOT NULL,
  issued_at                  DATETIME(6) NOT NULL,
  due_date                   DATE NOT NULL,
  returned_at                DATETIME(6) NULL,
  last_renewed_at            DATETIME(6) NULL,
  original_due_date          DATE NULL,
  renew_count                INT NOT NULL,
  renew_request_pending      BIT(1) NOT NULL,
  renew_requested_at         DATETIME(6) NULL,
  renew_requested_days       INT NULL,
  CONSTRAINT pk_borrow_records PRIMARY KEY (record_id),
  CONSTRAINT uq_borrow_records_request UNIQUE (request_id),
  CONSTRAINT fk_borrow_records_request FOREIGN KEY (request_id)
    REFERENCES borrow_requests(request_id) ON DELETE CASCADE,
  CONSTRAINT fk_borrow_records_book FOREIGN KEY (book_id)
    REFERENCES books(book_id) ON DELETE RESTRICT,
  CONSTRAINT fk_borrow_records_copy FOREIGN KEY (copy_id)
    REFERENCES book_copies(copy_id) ON DELETE SET NULL,
  CONSTRAINT fk_borrow_records_student FOREIGN KEY (student_id)
    REFERENCES students(user_id) ON DELETE RESTRICT,
  CONSTRAINT fk_borrow_records_issued_by FOREIGN KEY (issued_by_librarian_id)
    REFERENCES librarians(user_id) ON DELETE RESTRICT
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- =========================================================
-- Reservations (waitlist)
-- =========================================================

CREATE TABLE reservations (
  reservation_id            VARCHAR(64) NOT NULL,
  created_at                DATETIME(6) NOT NULL,
  expires_at                DATETIME(6) NULL,
  fulfilled_at              DATETIME(6) NULL,
  notified_at               DATETIME(6) NULL,
  queue_position            INT NOT NULL,
  status                    ENUM('CANCELLED','EXPIRED','FULFILLED','PENDING','READY') NOT NULL,
  book_id                   VARCHAR(5) NOT NULL,
  student_id                VARCHAR(512) NOT NULL,
  requested_duration_days   INT NOT NULL,
  CONSTRAINT pk_reservations PRIMARY KEY (reservation_id),
  CONSTRAINT fk_reservations_book FOREIGN KEY (book_id)
    REFERENCES books(book_id),
  CONSTRAINT fk_reservations_student FOREIGN KEY (student_id)
    REFERENCES students(user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Indexes expected/used by app migration and queries
CREATE INDEX idx_reservation_student_id
  ON reservations (student_id);

CREATE INDEX idx_reservation_book_id
  ON reservations (book_id);

CREATE INDEX idx_reservation_student_book_status
  ON reservations (student_id, book_id, status);

-- =========================================================
-- Fines
-- =========================================================

CREATE TABLE fines (
  fine_id                    VARCHAR(64) NOT NULL,
  amount                     DECIMAL(10,2) NOT NULL,
  days_late                  INT NOT NULL,
  issued_at                  DATETIME(6) NOT NULL,
  notes                      VARCHAR(500) NULL,
  resolved_at                DATETIME(6) NULL,
  status                     ENUM('PAID','UNPAID','WAIVED') NOT NULL,
  record_id                  VARCHAR(64) NOT NULL,
  resolved_by_librarian_id   VARCHAR(512) NULL,
  student_id                 VARCHAR(512) NOT NULL,
  waived_amount              DECIMAL(10,2) NOT NULL,
  CONSTRAINT pk_fines PRIMARY KEY (fine_id),
  CONSTRAINT uq_fines_record UNIQUE (record_id),
  CONSTRAINT fk_fines_record FOREIGN KEY (record_id)
    REFERENCES borrow_records(record_id),
  CONSTRAINT fk_fines_resolved_by FOREIGN KEY (resolved_by_librarian_id)
    REFERENCES librarians(user_id),
  CONSTRAINT fk_fines_student FOREIGN KEY (student_id)
    REFERENCES students(user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- =========================================================
-- Registration, notifications, deletion workflow
-- =========================================================

CREATE TABLE registration_requests (
  request_id         VARCHAR(36) NOT NULL,
  user_id            VARCHAR(512) NOT NULL,
  status             ENUM('approved','pending','rejected') NOT NULL,
  submitted_at       DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
  reviewed_at        DATETIME(6) NULL,
  rejection_reason   VARCHAR(255) NULL,
  CONSTRAINT pk_registration_requests PRIMARY KEY (request_id),
  CONSTRAINT uq_reg_requests_user UNIQUE (user_id),
  CONSTRAINT fk_reg_requests_user FOREIGN KEY (user_id)
    REFERENCES users(user_id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE notifications (
  notification_id   VARCHAR(36) NOT NULL,
  recipient_id      VARCHAR(512) NOT NULL,
  sender_id         VARCHAR(512) NULL,
  type              ENUM('account_approved','account_rejected','account_suspended','deletion_approved','deletion_rejected','deletion_requested') NOT NULL,
  title             VARCHAR(150) NOT NULL,
  message           TEXT NOT NULL,
  is_read           TINYINT(1) NOT NULL DEFAULT 0,
  created_at        DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
  read_at           DATETIME(6) NULL,
  CONSTRAINT pk_notifications PRIMARY KEY (notification_id),
  CONSTRAINT fk_notifications_recipient FOREIGN KEY (recipient_id)
    REFERENCES users(user_id) ON DELETE CASCADE,
  CONSTRAINT fk_notifications_sender FOREIGN KEY (sender_id)
    REFERENCES users(user_id) ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE deletion_requests (
  deletion_id     VARCHAR(36) NOT NULL,
  user_id         VARCHAR(512) NOT NULL,
  reason          TEXT NULL,
  status          ENUM('approved','pending','rejected') NOT NULL,
  requested_at    DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
  reviewed_at     DATETIME(6) NULL,
  admin_notes     TEXT NULL,
  CONSTRAINT pk_deletion_requests PRIMARY KEY (deletion_id),
  CONSTRAINT fk_deletion_requests_user FOREIGN KEY (user_id)
    REFERENCES users(user_id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
