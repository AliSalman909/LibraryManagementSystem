# Library Management System

Spring Boot 3.5.x (Java 17) application with session-based login, encrypted passwords, role-based dashboards, and a registration flow aligned with the `library_db` schema.

## Prerequisites

- JDK 17+
- Maven 3.9+
- MySQL 8+ with database `library_db` created and tables created using `docs/library_db_schema.sql`

## Configuration

Edit `src/main/resources/application.properties`:

- `spring.datasource.username` / `spring.datasource.password` for your MySQL user
- `spring.datasource.url` if MySQL is not on `localhost:3306`

- `spring.jpa.hibernate.ddl-auto=update` updates the schema if it needs to.
- `server.port=8081` is preferred, but if it is already in use the app will automatically switch to a free port at startup.

## Run

```powershell
cd C:\Users\CodeTech\Downloads\LibraryManagementSystem\LibraryMS
mvn spring-boot:run
```

When the server starts, it will automatically open the correct URL in your browser (it uses the actual runtime port, including any auto-switch to a free port).

If you run multiple debug sessions at the same time, the server won’t fail with “port already in use”.

## Bootstrap admin (optional)

To bootstrap the system, create the **first ADMIN**.

You can do this either by:
- Using the registration page (`/register`) and selecting role `ADMIN` when there are no admins yet.
- Or by inserting a row into `users` plus the corresponding `admins` row directly in MySQL.

## Approve registrations (admin UI)

Sign in as an **active** admin, open **Admin dashboard** → **Review pending registrations** (`/admin/registrations/pending`). **Approve** sets `users.account_status` to `active`, marks the request `approved`, and inserts an `account_approved` notification. **Reject** sets `account_status` to `rejected`, stores an optional reason, and notifies with `account_rejected`.

## Activate a registered user (SQL alternative)

After self-registration, `account_status` is `pending`, so login is blocked until activation:

```sql
UPDATE users SET account_status = 'active' WHERE email = 'you@example.com';
-- optional consistency:
UPDATE registration_requests rr
JOIN users u ON u.user_id = rr.user_id
SET rr.status = 'approved', rr.reviewed_at = CURRENT_TIMESTAMP
WHERE u.email = 'you@example.com';
```

## Roles and URLs

| Role      | After login        |
|-----------|--------------------|
| `ADMIN`   | `/admin/dashboard` |
| `LIBRARIAN` | `/librarian/dashboard` |
| `STUDENT` | `/student/dashboard` |

Public: `/`, `/login`, `/register`, static assets, `/error`, `/access-denied`.  
Admin-only: `/admin/registrations/pending` (and POST approve/reject under `/admin/registrations/...`).

## Concurrency / Data consistency

The app uses database transactions and row-level pessimistic locks on the critical update flows:
- Borrow approvals/rejections lock the `borrow_requests` row and pessimistically lock an available `book_copy` before issuing a copy.
- Registration approve/reject locks the `registration_requests` row and locks the target `users` row before updating `account_status`.
- Book edit/delete locks the `books` row and all `book_copies` for the affected book while recomputing ISBN/copy data.
- “First ADMIN can be created only once” is guarded with a MySQL advisory lock (`GET_LOCK`).

For profile-picture uploads: if the DB transaction fails and rolls back, the uploaded image is deleted to prevent orphan files.

## Package layout

- `com.library.controller` — MVC controllers  
- `com.library.service` — registration and user services  
- `com.library.repository` — Spring Data JPA  
- `com.library.entity` — JPA mappings for your schema  
- `com.library.dto` — registration form DTO + validation  
- `com.library.security` — custom authentication provider, handlers, `UserDetails`  
- `com.library.config` — `SecurityConfig`  
- `com.library.exception` — global error handling  
