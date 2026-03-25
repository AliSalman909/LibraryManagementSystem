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
  account_status           ENUM('pending', 'active', 'rejected', 'suspended', 'deletion_pending', 'deleted') NOT NULL DEFAULT 'pending',
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
-- NOTE: legacy column still required by entity for compatibility.
-- -----------------------------------------------------------------------------
CREATE TABLE students (
  user_id            VARCHAR(512)   NOT NULL,
  student_id         VARCHAR(64)    NOT NULL,
  program            VARCHAR(100)   NOT NULL,
  enrollment_date    DATE           NULL,
  date_of_birth      DATE           NULL,
  max_borrow_limit   INT            NOT NULL DEFAULT 3,
  can_borrow         BOOLEAN        NOT NULL DEFAULT TRUE,

  CONSTRAINT pk_students              PRIMARY KEY (user_id),
  CONSTRAINT fk_students_user         FOREIGN KEY (user_id) REFERENCES users (user_id) ON DELETE CASCADE,
  CONSTRAINT chk_students_borrow_limit CHECK (max_borrow_limit > 0 AND max_borrow_limit <= 10)
);

-- -----------------------------------------------------------------------------
-- librarians
-- NOTE: legacy column still required by entity for compatibility.
-- -----------------------------------------------------------------------------
CREATE TABLE librarians (
  user_id                 VARCHAR(512)   NOT NULL,
  employee_id             VARCHAR(64)    NOT NULL,
  can_approve_borrowing   BOOLEAN        NOT NULL DEFAULT TRUE,

  CONSTRAINT pk_librarians              PRIMARY KEY (user_id),
  CONSTRAINT fk_librarians_user         FOREIGN KEY (user_id) REFERENCES users (user_id) ON DELETE CASCADE
);

-- -----------------------------------------------------------------------------
-- admins (entity class: AdminProfile)
-- NOTE: legacy column still required by entity for compatibility.
-- -----------------------------------------------------------------------------
CREATE TABLE admins (
  user_id              VARCHAR(512)   NOT NULL,
  department           VARCHAR(100)   NULL,
  employee_id          VARCHAR(64)    NOT NULL,
  can_manage_users     BOOLEAN        NOT NULL DEFAULT TRUE,
  can_view_reports     BOOLEAN        NOT NULL DEFAULT TRUE,
  can_manage_catalog   BOOLEAN        NOT NULL DEFAULT TRUE,

  CONSTRAINT pk_admins              PRIMARY KEY (user_id),
  CONSTRAINT fk_admins_user         FOREIGN KEY (user_id) REFERENCES users (user_id) ON DELETE CASCADE
);

-- -----------------------------------------------------------------------------
-- registration_requests
-- (RequestStatus: pending, approved, rejected)
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
-- notifications
-- (NotificationType enum names as stored)
-- -----------------------------------------------------------------------------
CREATE TABLE notifications (
  notification_id  CHAR(36)      NOT NULL,
  recipient_id     VARCHAR(512)  NOT NULL,
  sender_id        VARCHAR(512)  NULL,
  type             ENUM(
    'account_approved',
    'account_rejected',
    'deletion_requested',
    'deletion_approved',
    'deletion_rejected',
    'account_suspended'
  ) NOT NULL,
  title             VARCHAR(150)  NOT NULL,
  message           TEXT          NOT NULL,
  is_read           BOOLEAN       NOT NULL DEFAULT FALSE,
  created_at        DATETIME(6)   NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
  read_at           DATETIME(6)   NULL,

  CONSTRAINT pk_notifications            PRIMARY KEY (notification_id),
  CONSTRAINT fk_notifications_recipient FOREIGN KEY (recipient_id) REFERENCES users (user_id) ON DELETE CASCADE,
  CONSTRAINT fk_notifications_sender    FOREIGN KEY (sender_id) REFERENCES users (user_id) ON DELETE SET NULL
);

-- -----------------------------------------------------------------------------
-- books
-- -----------------------------------------------------------------------------
CREATE TABLE books (
  book_id           VARCHAR(64)   NOT NULL,
  title             VARCHAR(200)  NOT NULL,
  author            VARCHAR(150)  NOT NULL,
  isbn              VARCHAR(64)   NULL,
  category          VARCHAR(100)  NULL,
  total_copies      INT           NOT NULL,
  available_copies  INT           NOT NULL,
  created_at        DATETIME(6)   NOT NULL,
  updated_at        DATETIME(6)   NOT NULL,

  CONSTRAINT pk_books                        PRIMARY KEY (book_id),
  CONSTRAINT uq_books_isbn                   UNIQUE (isbn),
  CONSTRAINT chk_books_total_copies_positive CHECK (total_copies > 0),
  CONSTRAINT chk_books_available_nonnegative CHECK (available_copies >= 0),
  CONSTRAINT chk_books_available_le_total    CHECK (available_copies <= total_copies)
);

-- -----------------------------------------------------------------------------
-- book_copies
-- -----------------------------------------------------------------------------
CREATE TABLE book_copies (
  copy_id       VARCHAR(64)  NOT NULL,
  book_id       VARCHAR(64)  NOT NULL,
  isbn_code     VARCHAR(96)  NOT NULL,
  copy_number   INT          NOT NULL,
  is_available  BOOLEAN      NOT NULL DEFAULT TRUE,

  CONSTRAINT pk_book_copies                  PRIMARY KEY (copy_id),
  CONSTRAINT fk_book_copies_book           FOREIGN KEY (book_id) REFERENCES books (book_id) ON DELETE CASCADE,
  CONSTRAINT uq_book_copies_isbn_code      UNIQUE (isbn_code),
  CONSTRAINT uq_book_copies_book_copy_number UNIQUE (book_id, copy_number),
  CONSTRAINT chk_book_copies_copy_number_positive CHECK (copy_number > 0)
);

-- -----------------------------------------------------------------------------
-- borrow_requests
-- BorrowRequestStatus values: PENDING, APPROVED, REJECTED
-- -----------------------------------------------------------------------------
CREATE TABLE borrow_requests (
  request_id                 VARCHAR(64)  NOT NULL,
  book_id                    VARCHAR(64)  NOT NULL,
  student_id                 VARCHAR(512) NOT NULL,
  processed_by_librarian_id  VARCHAR(512) NULL,
  status                     ENUM('PENDING', 'APPROVED', 'REJECTED') NOT NULL DEFAULT 'PENDING',
  requested_at               DATETIME(6)  NOT NULL,
  reviewed_at                DATETIME(6)  NULL,
  due_date                   DATE         NULL,
  requested_duration_days    INT          NOT NULL DEFAULT 14,

  CONSTRAINT pk_borrow_requests                         PRIMARY KEY (request_id),
  CONSTRAINT fk_borrow_requests_book                    FOREIGN KEY (book_id) REFERENCES books (book_id) ON DELETE CASCADE,
  CONSTRAINT fk_borrow_requests_student                 FOREIGN KEY (student_id) REFERENCES students (user_id) ON DELETE CASCADE,
  CONSTRAINT fk_borrow_requests_processed_by_librarian  FOREIGN KEY (processed_by_librarian_id) REFERENCES librarians (user_id) ON DELETE SET NULL,
  CONSTRAINT chk_borrow_requests_duration               CHECK (requested_duration_days IN (7, 14))
);

CREATE INDEX idx_borrow_requests_student_status ON borrow_requests (student_id, status);
CREATE INDEX idx_borrow_requests_book_status ON borrow_requests (book_id, status);
CREATE INDEX idx_borrow_requests_requested_at ON borrow_requests (requested_at);

-- -----------------------------------------------------------------------------
-- borrow_records
-- -----------------------------------------------------------------------------
CREATE TABLE borrow_records (
  record_id               VARCHAR(64)  NOT NULL,
  request_id              VARCHAR(64)  NOT NULL,
  book_id                 VARCHAR(64)  NOT NULL,
  copy_id                 VARCHAR(64)  NULL,
  student_id              VARCHAR(512) NOT NULL,
  issued_by_librarian_id  VARCHAR(512) NOT NULL,
  issued_at               DATETIME(6)  NOT NULL,
  due_date                DATE         NOT NULL,
  returned_at             DATETIME(6)  NULL,

  CONSTRAINT pk_borrow_records                    PRIMARY KEY (record_id),
  CONSTRAINT uq_borrow_records_request            UNIQUE (request_id),
  CONSTRAINT fk_borrow_records_request            FOREIGN KEY (request_id) REFERENCES borrow_requests (request_id) ON DELETE CASCADE,
  CONSTRAINT fk_borrow_records_book               FOREIGN KEY (book_id) REFERENCES books (book_id) ON DELETE RESTRICT,
  CONSTRAINT fk_borrow_records_copy               FOREIGN KEY (copy_id) REFERENCES book_copies (copy_id) ON DELETE SET NULL,
  CONSTRAINT fk_borrow_records_student            FOREIGN KEY (student_id) REFERENCES students (user_id) ON DELETE RESTRICT,
  CONSTRAINT fk_borrow_records_issued_by         FOREIGN KEY (issued_by_librarian_id) REFERENCES librarians (user_id) ON DELETE RESTRICT
);

CREATE INDEX idx_borrow_records_student_returned ON borrow_records (student_id, returned_at);
CREATE INDEX idx_borrow_records_book_returned ON borrow_records (book_id, returned_at);

