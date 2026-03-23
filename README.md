# Library Management System

A Java Spring Boot + MySQL web application for managing library users, catalog operations, and borrowing workflows.

## Tech Stack

- Backend: Java, Spring Boot, Spring Security, Spring Data JPA
- Frontend: Thymeleaf (HTML/CSS), server-rendered MVC pages
- Database: MySQL
- Build: Maven

## Implemented Scope

### Sprint 1 (Completed)

- User registration for `ADMIN`, `LIBRARIAN`, and `STUDENT`
- Session-based login with role-based routing and authorization
- Account status flow (`pending`, `active`, `rejected`, etc.)
- Admin approval/rejection of registration requests
- Admin user-account actions (status management and deletion workflows)
- Account deletion request support
- Profile photo upload + focal point support

### Sprint 2 (Implemented)

#### 1) Book Management (Librarian)

- Add, update, delete, and list books
- Category is mandatory
- Book base ISBN is auto-generated from title + category
- Per-copy unique identifier support through `book_copies`

#### 2) Book Search (Student / Librarian)

- Search by title and/or author
- Student catalog browsing UI
- REST search endpoint support

#### 3) Borrow Request System (Student)

- Student can request a book with duration:
  - `14 days` (default)
  - `7 days`
- Request stored with `PENDING` status
- Duplicate request prevention for same book
- Duplicate active loan prevention for same book

#### 4) Librarian Approval / Rejection

- Librarian can view pending requests
- Librarian can approve or reject requests
- On approval:
  - book quantity decrements safely
  - specific available copy is assigned
  - borrow record is created
  - due date is computed from approval date + requested duration

#### 5) Borrow Rules and Due Dates

- Due date logic supports 7/14 day durations
- Overdue indicators in student-facing request views
- Student may issue at most **3 unique active books** at a time

## Core Database Model

Existing base tables include:
- `users`
- `students`
- `librarians`
- `admins`
- `registration_requests`
- `notifications`

Sprint 2 additions:
- `books`
- `book_copies`
- `borrow_requests`
- `borrow_records`

## Project Structure

- `com.library.controller` - MVC + REST controllers
- `com.library.service` - business rules and transaction orchestration
- `com.library.repository` - JPA repository layer
- `com.library.entity` - JPA entities and relationships
- `com.library.dto` - request/response and form DTOs
- `com.library.security` - auth provider, user details, login handlers
- `com.library.config` - security and web configuration
- `com.library.exception` - centralized exception handling
- `src/main/resources/templates` - Thymeleaf UI pages
- `src/main/resources/static` - CSS/JS assets

## Setup

### Prerequisites

- JDK 17+
- Maven 3.9+
- MySQL 8+

### Configure Database

Update `src/main/resources/application.properties`:

- `spring.datasource.url`
- `spring.datasource.username`
- `spring.datasource.password`

Recommended local DB name: `library_db`.

### Run

```bash
mvn spring-boot:run
```

App starts on configured port (default in project: `8081`).

## Main Role URLs

- Public:
  - `/`
  - `/login`
  - `/register`
- Admin:
  - `/admin/dashboard`
  - `/admin/registrations/pending`
  - `/admin/users`
- Librarian:
  - `/librarian/dashboard`
  - `/librarian/books`
  - `/librarian/borrow-requests`
- Student:
  - `/student/dashboard`
  - `/student/books`
  - `/student/borrow-requests`

## REST Endpoints (Current)

### Books

- `POST /books` (LIBRARIAN)
- `GET /books` (LIBRARIAN)
- `PUT /books/{id}` (LIBRARIAN)
- `DELETE /books/{id}` (LIBRARIAN)
- `GET /books/search?title=...&author=...` (LIBRARIAN, STUDENT)

### Borrowing

- `POST /borrow/request` (STUDENT)
- `GET /borrow/pending` (LIBRARIAN)
- `POST /borrow/approve/{id}` (LIBRARIAN)
- `POST /borrow/reject/{id}` (LIBRARIAN)

## Notes

- This project includes compatibility handling for legacy schema columns such as `employee_id` and `student_id` where required by existing database constraints.
- If schema drift exists from earlier versions, let Spring Boot update mode and/or apply migration SQL as needed.

## Next Recommended Enhancements

- Return-book flow (`returned_at`, stock increment)
- Pagination for books/requests
- Flyway/Liquibase migrations for repeatable schema management
- Test coverage for service-layer borrowing rules
