# Library DB Schema (Readable)

This file is an easy-to-read summary generated from `docs/library_db_schema.sql`.

## Table List

- `admins`
- `book_copies`
- `books`
- `borrow_records`
- `borrow_requests`
- `deletion_requests`
- `fines`
- `librarians`
- `notifications`
- `registration_requests`
- `reservations`
- `students`
- `users`

## Relationships (Foreign Keys)

- `admins.user_id` -> `users.user_id` via `fk_admins_user` (ON DELETE CASCADE)
- `book_copies.book_id` -> `books.book_id` via `fk_book_copies_book` (ON DELETE CASCADE)
- `borrow_records.book_id` -> `books.book_id` via `fk_borrow_records_book` (ON DELETE RESTRICT)
- `borrow_records.copy_id` -> `book_copies.copy_id` via `fk_borrow_records_copy` (ON DELETE SET NULL)
- `borrow_records.issued_by_librarian_id` -> `librarians.user_id` via `fk_borrow_records_issued_by` (ON DELETE RESTRICT)
- `borrow_records.request_id` -> `borrow_requests.request_id` via `fk_borrow_records_request` (ON DELETE CASCADE)
- `borrow_records.student_id` -> `students.user_id` via `fk_borrow_records_student` (ON DELETE RESTRICT)
- `borrow_requests.book_id` -> `books.book_id` via `fk_borrow_requests_book` (ON DELETE CASCADE)
- `borrow_requests.processed_by_librarian_id` -> `librarians.user_id` via `fk_borrow_requests_processed_by_librarian` (ON DELETE SET NULL)
- `borrow_requests.student_id` -> `students.user_id` via `fk_borrow_requests_student` (ON DELETE CASCADE)
- `deletion_requests.user_id` -> `users.user_id` via `fk_deletion_requests_user` (ON DELETE CASCADE)
- `fines.resolved_by_librarian_id` -> `librarians.user_id` via `FKmfi78yy2nxmi226cjbxl4wh5e`
- `fines.record_id` -> `borrow_records.record_id` via `FKqb1ke4ygikmvjr5w73x9y6ngl`
- `fines.student_id` -> `students.user_id` via `FKsgxc21prt442x0tynbtaieyqk`
- `librarians.user_id` -> `users.user_id` via `fk_librarians_user` (ON DELETE CASCADE)
- `notifications.recipient_id` -> `users.user_id` via `fk_notifications_recipient` (ON DELETE CASCADE)
- `notifications.sender_id` -> `users.user_id` via `fk_notifications_sender` (ON DELETE SET NULL)
- `registration_requests.user_id` -> `users.user_id` via `fk_reg_requests_user` (ON DELETE CASCADE)
- `reservations.student_id` -> `students.user_id` via `FKnlgg22885nfyspmen9jj0jcpp`
- `reservations.book_id` -> `books.book_id` via `FKrsdd3ib3landfpmgoolccjakt`
- `students.user_id` -> `users.user_id` via `fk_students_user` (ON DELETE CASCADE)

## `admins`

### Columns

| Column | Type | Nullable | Default |
|---|---|---|---|
| `user_id` | `varchar(512)` | `NO` | `` |
| `department` | `varchar(100)` | `YES` | `NULL` |
| `employee_id` | `varchar(64)` | `NO` | `` |
| `can_manage_users` | `tinyint(1)` | `NO` | `'1'` |
| `can_view_reports` | `tinyint(1)` | `NO` | `'1'` |
| `can_manage_catalog` | `tinyint(1)` | `NO` | `'1'` |

### Keys

- Primary Key: `user_id`
- Foreign Keys:
  - `fk_admins_user`: `user_id` -> `users.user_id` (ON DELETE CASCADE)

## `book_copies`

### Columns

| Column | Type | Nullable | Default |
|---|---|---|---|
| `copy_id` | `varchar(64)` | `NO` | `` |
| `book_id` | `varchar(5)` | `NO` | `` |
| `isbn_code` | `varchar(96)` | `NO` | `` |
| `copy_number` | `int` | `NO` | `` |
| `is_available` | `tinyint(1)` | `NO` | `'1'` |

### Keys

- Primary Key: `copy_id`
- Unique Keys:
  - `uq_book_copies_isbn_code`: isbn_code
  - `uq_book_copies_book_copy_number`: book_id, copy_number
- Foreign Keys:
  - `fk_book_copies_book`: `book_id` -> `books.book_id` (ON DELETE CASCADE)

## `books`

### Columns

| Column | Type | Nullable | Default |
|---|---|---|---|
| `book_id` | `varchar(5)` | `NO` | `` |
| `title` | `varchar(200)` | `NO` | `` |
| `author` | `varchar(150)` | `NO` | `` |
| `isbn` | `varchar(64)` | `YES` | `NULL` |
| `category` | `varchar(100)` | `YES` | `NULL` |
| `total_copies` | `int` | `NO` | `` |
| `available_copies` | `int` | `NO` | `` |
| `created_at` | `datetime(6)` | `NO` | `` |
| `updated_at` | `datetime(6)` | `NO` | `` |
| `fine_per_day` | `decimal(10,2)` | `NO` | `` |
| `fine_per_day_pkr` | `int` | `NO` | `` |
| `max_borrow_days` | `int` | `NO` | `'28'` |

### Keys

- Primary Key: `book_id`
- Unique Keys:
  - `uq_books_isbn`: isbn
  - `uq_books_title_author_category`: title, author, category

## `borrow_records`

### Columns

| Column | Type | Nullable | Default |
|---|---|---|---|
| `record_id` | `varchar(64)` | `NO` | `` |
| `request_id` | `varchar(64)` | `NO` | `` |
| `book_id` | `varchar(5)` | `NO` | `` |
| `copy_id` | `varchar(64)` | `YES` | `NULL` |
| `student_id` | `varchar(512)` | `NO` | `` |
| `issued_by_librarian_id` | `varchar(512)` | `NO` | `` |
| `issued_at` | `datetime(6)` | `NO` | `` |
| `due_date` | `date` | `NO` | `` |
| `returned_at` | `datetime(6)` | `YES` | `NULL` |
| `last_renewed_at` | `datetime(6)` | `YES` | `NULL` |
| `original_due_date` | `date` | `YES` | `NULL` |
| `renew_count` | `int` | `NO` | `` |
| `renew_request_pending` | `bit(1)` | `NO` | `` |
| `renew_requested_at` | `datetime(6)` | `YES` | `NULL` |
| `renew_requested_days` | `int` | `YES` | `NULL` |

### Keys

- Primary Key: `record_id`
- Unique Keys:
  - `uq_borrow_records_request`: request_id
- Indexes:
  - `fk_borrow_records_copy`: copy_id
  - `fk_borrow_records_issued_by`: issued_by_librarian_id
  - `idx_borrow_records_student_returned`: student_id, returned_at
  - `idx_borrow_records_book_returned`: book_id, returned_at
- Foreign Keys:
  - `fk_borrow_records_book`: `book_id` -> `books.book_id` (ON DELETE RESTRICT)
  - `fk_borrow_records_copy`: `copy_id` -> `book_copies.copy_id` (ON DELETE SET NULL)
  - `fk_borrow_records_issued_by`: `issued_by_librarian_id` -> `librarians.user_id` (ON DELETE RESTRICT)
  - `fk_borrow_records_request`: `request_id` -> `borrow_requests.request_id` (ON DELETE CASCADE)
  - `fk_borrow_records_student`: `student_id` -> `students.user_id` (ON DELETE RESTRICT)

## `borrow_requests`

### Columns

| Column | Type | Nullable | Default |
|---|---|---|---|
| `request_id` | `varchar(64)` | `NO` | `` |
| `book_id` | `varchar(5)` | `NO` | `` |
| `student_id` | `varchar(512)` | `NO` | `` |
| `processed_by_librarian_id` | `varchar(512)` | `YES` | `NULL` |
| `status` | `enum('APPROVED','CANCELLED','PENDING','REJECTED')` | `NO` | `` |
| `requested_at` | `datetime(6)` | `NO` | `` |
| `reviewed_at` | `datetime(6)` | `YES` | `NULL` |
| `due_date` | `date` | `YES` | `NULL` |
| `requested_duration_days` | `int` | `NO` | `'14'` |

### Keys

- Primary Key: `request_id`
- Indexes:
  - `fk_borrow_requests_processed_by_librarian`: processed_by_librarian_id
  - `idx_borrow_requests_student_status`: student_id, status
  - `idx_borrow_requests_book_status`: book_id, status
  - `idx_borrow_requests_requested_at`: requested_at
- Foreign Keys:
  - `fk_borrow_requests_book`: `book_id` -> `books.book_id` (ON DELETE CASCADE)
  - `fk_borrow_requests_processed_by_librarian`: `processed_by_librarian_id` -> `librarians.user_id` (ON DELETE SET NULL)
  - `fk_borrow_requests_student`: `student_id` -> `students.user_id` (ON DELETE CASCADE)

## `deletion_requests`

### Columns

| Column | Type | Nullable | Default |
|---|---|---|---|
| `deletion_id` | `varchar(36)` | `NO` | `` |
| `user_id` | `varchar(512)` | `NO` | `` |
| `reason` | `text` | `YES` | `` |
| `status` | `enum('approved','pending','rejected')` | `NO` | `` |
| `requested_at` | `datetime(6)` | `NO` | `CURRENT_TIMESTAMP(6)` |
| `reviewed_at` | `datetime(6)` | `YES` | `NULL` |
| `admin_notes` | `text` | `YES` | `` |

### Keys

- Primary Key: `deletion_id`
- Indexes:
  - `fk_deletion_requests_user`: user_id
- Foreign Keys:
  - `fk_deletion_requests_user`: `user_id` -> `users.user_id` (ON DELETE CASCADE)

## `fines`

### Columns

| Column | Type | Nullable | Default |
|---|---|---|---|
| `fine_id` | `varchar(64)` | `NO` | `` |
| `amount` | `decimal(10,2)` | `NO` | `` |
| `days_late` | `int` | `NO` | `` |
| `issued_at` | `datetime(6)` | `NO` | `` |
| `notes` | `varchar(500)` | `YES` | `NULL` |
| `resolved_at` | `datetime(6)` | `YES` | `NULL` |
| `status` | `enum('PAID','UNPAID','WAIVED')` | `NO` | `` |
| `record_id` | `varchar(64)` | `NO` | `` |
| `resolved_by_librarian_id` | `varchar(512)` | `YES` | `NULL` |
| `student_id` | `varchar(512)` | `NO` | `` |
| `waived_amount` | `decimal(10,2)` | `NO` | `` |

### Keys

- Primary Key: `fine_id`
- Unique Keys:
  - `UKpb7celoohrcfm1aflh3pbsynk`: record_id
- Indexes:
  - `FKmfi78yy2nxmi226cjbxl4wh5e`: resolved_by_librarian_id
  - `FKsgxc21prt442x0tynbtaieyqk`: student_id
- Foreign Keys:
  - `FKmfi78yy2nxmi226cjbxl4wh5e`: `resolved_by_librarian_id` -> `librarians.user_id`
  - `FKqb1ke4ygikmvjr5w73x9y6ngl`: `record_id` -> `borrow_records.record_id`
  - `FKsgxc21prt442x0tynbtaieyqk`: `student_id` -> `students.user_id`

## `librarians`

### Columns

| Column | Type | Nullable | Default |
|---|---|---|---|
| `user_id` | `varchar(512)` | `NO` | `` |
| `employee_id` | `varchar(64)` | `NO` | `` |
| `can_approve_borrowing` | `tinyint(1)` | `NO` | `'1'` |

### Keys

- Primary Key: `user_id`
- Foreign Keys:
  - `fk_librarians_user`: `user_id` -> `users.user_id` (ON DELETE CASCADE)

## `notifications`

### Columns

| Column | Type | Nullable | Default |
|---|---|---|---|
| `notification_id` | `varchar(36)` | `NO` | `` |
| `recipient_id` | `varchar(512)` | `NO` | `` |
| `sender_id` | `varchar(512)` | `YES` | `NULL` |
| `type` | `enum('account_approved','account_rejected','account_suspended','deletion_approved','deletion_rejected','deletion_requested')` | `NO` | `` |
| `title` | `varchar(150)` | `NO` | `` |
| `message` | `text` | `NO` | `` |
| `is_read` | `tinyint(1)` | `NO` | `'0'` |
| `created_at` | `datetime(6)` | `NO` | `CURRENT_TIMESTAMP(6)` |
| `read_at` | `datetime(6)` | `YES` | `NULL` |

### Keys

- Primary Key: `notification_id`
- Indexes:
  - `fk_notifications_recipient`: recipient_id
  - `fk_notifications_sender`: sender_id
- Foreign Keys:
  - `fk_notifications_recipient`: `recipient_id` -> `users.user_id` (ON DELETE CASCADE)
  - `fk_notifications_sender`: `sender_id` -> `users.user_id` (ON DELETE SET NULL)

## `registration_requests`

### Columns

| Column | Type | Nullable | Default |
|---|---|---|---|
| `request_id` | `varchar(36)` | `NO` | `` |
| `user_id` | `varchar(512)` | `NO` | `` |
| `status` | `enum('approved','pending','rejected')` | `NO` | `` |
| `submitted_at` | `datetime(6)` | `NO` | `CURRENT_TIMESTAMP(6)` |
| `reviewed_at` | `datetime(6)` | `YES` | `NULL` |
| `rejection_reason` | `varchar(255)` | `YES` | `NULL` |

### Keys

- Primary Key: `request_id`
- Unique Keys:
  - `uq_reg_requests_user`: user_id
- Foreign Keys:
  - `fk_reg_requests_user`: `user_id` -> `users.user_id` (ON DELETE CASCADE)

## `reservations`

### Columns

| Column | Type | Nullable | Default |
|---|---|---|---|
| `reservation_id` | `varchar(64)` | `NO` | `` |
| `created_at` | `datetime(6)` | `NO` | `` |
| `expires_at` | `datetime(6)` | `YES` | `NULL` |
| `fulfilled_at` | `datetime(6)` | `YES` | `NULL` |
| `notified_at` | `datetime(6)` | `YES` | `NULL` |
| `queue_position` | `int` | `NO` | `` |
| `status` | `enum('CANCELLED','EXPIRED','FULFILLED','PENDING','READY')` | `NO` | `` |
| `book_id` | `varchar(5)` | `NO` | `` |
| `student_id` | `varchar(512)` | `NO` | `` |
| `requested_duration_days` | `int` | `NO` | `` |

### Keys

- Primary Key: `reservation_id`
- Indexes:
  - `FKrsdd3ib3landfpmgoolccjakt`: book_id
  - `idx_reservation_student_id`: student_id
  - `idx_reservation_book_id`: book_id
  - `idx_reservation_student_book_status`: student_id, book_id, status
- Foreign Keys:
  - `FKnlgg22885nfyspmen9jj0jcpp`: `student_id` -> `students.user_id`
  - `FKrsdd3ib3landfpmgoolccjakt`: `book_id` -> `books.book_id`

## `students`

### Columns

| Column | Type | Nullable | Default |
|---|---|---|---|
| `user_id` | `varchar(512)` | `NO` | `` |
| `student_id` | `varchar(64)` | `NO` | `` |
| `program` | `varchar(100)` | `NO` | `` |
| `enrollment_date` | `date` | `YES` | `NULL` |
| `date_of_birth` | `date` | `YES` | `NULL` |
| `max_borrow_limit` | `int` | `NO` | `'3'` |
| `can_borrow` | `tinyint(1)` | `NO` | `'1'` |

### Keys

- Primary Key: `user_id`
- Foreign Keys:
  - `fk_students_user`: `user_id` -> `users.user_id` (ON DELETE CASCADE)

## `users`

### Columns

| Column | Type | Nullable | Default |
|---|---|---|---|
| `user_id` | `varchar(512)` | `NO` | `` |
| `full_name` | `varchar(100)` | `NO` | `` |
| `email` | `varchar(150)` | `NO` | `` |
| `password_hash` | `varchar(512)` | `NO` | `` |
| `user_role` | `enum('ADMIN','LIBRARIAN','STUDENT')` | `NO` | `` |
| `account_status` | `enum('active','deleted','deletion_pending','pending','rejected','suspended')` | `NO` | `` |
| `phone_number` | `varchar(20)` | `YES` | `NULL` |
| `profile_picture` | `varchar(500)` | `YES` | `NULL` |
| `profile_picture_focal_x` | `double` | `YES` | `NULL` |
| `profile_picture_focal_y` | `double` | `YES` | `NULL` |
| `created_at` | `datetime(6)` | `NO` | `CURRENT_TIMESTAMP(6)` |
| `updated_at` | `datetime(6)` | `NO` | `CURRENT_TIMESTAMP(6)` |
| `last_login_at` | `datetime(6)` | `YES` | `NULL` |

### Keys

- Primary Key: `user_id`
- Unique Keys:
  - `uq_users_email`: email
