# Library Management System — Authentication Module

Spring Boot 3.2 (Java 17) application with session-based login, BCrypt passwords, role-based dashboards, and a registration flow aligned with the `library_db` schema.

## Prerequisites

- JDK 17+
- Maven 3.9+
- MySQL 8+ with database `library_db` created and tables created using your provided SQL script

## Configuration

Edit `src/main/resources/application.properties`:

- `spring.datasource.username` / `spring.datasource.password` for your MySQL user
- `spring.datasource.url` if MySQL is not on `localhost:3306`

`spring.jpa.hibernate.ddl-auto=validate` expects the database to match the entities (same tables and columns as your schema).

## Run

```bash
cd C:\Users\CodeTech\Desktop\LibraryMS
mvn spring-boot:run
```

Run the app, then open the URL shown in the console (Spring Boot chooses a free port). Debug mode will auto-open the correct URL in your browser.

## Bootstrap admin (optional)

See `docs/seed-admin.sql` for a ready-made **active** administrator using password `password` (BCrypt hash included). Run it in MySQL after creating tables, then adjust the email or hash for production.

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

## Package layout

- `com.library.controller` — MVC controllers  
- `com.library.service` — registration and user services  
- `com.library.repository` — Spring Data JPA  
- `com.library.entity` — JPA mappings for your schema  
- `com.library.dto` — registration form DTO + validation  
- `com.library.security` — custom authentication provider, handlers, `UserDetails`  
- `com.library.config` — `SecurityConfig`  
- `com.library.exception` — global error handling  
