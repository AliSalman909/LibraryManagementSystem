# Library Management System — Web app (auth, registration, admin)

Spring Boot **3.5.x** (Java **17**) application: session-based login, role-based dashboards (admin, librarian, student), self-service registration with optional profile photo, admin review of pending accounts, and an admin **user accounts** console (status changes, suspend/activate, hard delete, re-approve rejected accounts).

Stack: Spring MVC, Thymeleaf, Spring Security, Spring Data JPA, MySQL.

## Prerequisites

- **JDK 17+**
- **Maven 3.9+** (or use your IDE’s Maven integration)
- **MySQL 8+** — create database `library_db` and apply **`docs/library_db_schema.sql`**

## Configuration

Edit `src/main/resources/application.properties` (or override with a Spring profile / environment variables in deployment):

| Area | Properties |
|------|------------|
| MySQL | `spring.datasource.url`, `spring.datasource.username`, `spring.datasource.password` |
| Hibernate | `spring.jpa.hibernate.ddl-auto` — `update` is convenient in dev; for production prefer `validate` or `none` and manage schema with SQL or migrations |
| Secrets | **`app.security.password-secret`** — at least 16 characters; used for **AES-256-GCM** storage of new passwords and for legacy user-id decryption key material. Change for any real deployment and keep it secret |
| Legacy user IDs | Optional **`app.security.user-id-secret`** — only if old rows used `U1:`-prefixed encrypted user ids |
| HTTP port | **`server.port=0`** picks a random free port locally; set a fixed port (e.g. `8080`) when you need a stable URL |
| Browser | **`app.browser.open-on-start`** — `true` opens the home page when the app is ready; set `false` for production, CI, or headless servers |
| Uploads | **`app.upload.dir`**, `spring.servlet.multipart.max-file-size` — profile pictures are stored on disk and served under `/uploads/profiles/**` |

**Security note:** Do not commit real database passwords or production secrets. Use local-only overrides or environment-specific configuration.

### Password storage (important)

- **New registrations** store passwords with **reversible AES-256-GCM** (`PasswordEncryptionService`), so an administrator can view plaintext in the user-accounts UI (lab-oriented behavior).
- **Legacy rows** may still use **BCrypt** (`$2a$…`); login supports both formats.

## Run

From this directory (`LibraryMS`):

```bash
mvn spring-boot:run
```

On Windows (PowerShell), from the repo root:

```powershell
cd LibraryMS
mvn spring-boot:run
```

Open the URL logged by Spring Boot (with `server.port=0`, the chosen port appears in the console). If `app.browser.open-on-start=true`, a browser may open automatically.

## First administrator

- **Bootstrap:** If the `admins` table is **empty**, a single **Admin** registration is allowed through **`/register`**. That account is created **active** immediately and does not wait for approval. Further self-service **Admin** sign-ups are blocked.
- **Promoting via SQL (alternative):** Register through the app, then in MySQL align `users` and `admins` for the same `user_id` (the **6-character** id shown at registration), set `users.user_role = 'ADMIN'`, `users.account_status = 'active'`, and insert the matching `admins` row.

## Registration and approval

1. Users register at **`/register`** (role, profile fields, optional profile image and focal point).
2. Non-admin registrations start as **`pending`**; login is blocked until approved.
3. An active **Admin** opens **`/admin/registrations/pending`**: **Approve** activates the account and records notifications; **Reject** can store a reason and notifies the user.

**SQL-only activation** (if you skip the admin UI):

```sql
UPDATE users SET account_status = 'active' WHERE email = 'you@example.com';
UPDATE registration_requests rr
JOIN users u ON u.user_id = rr.user_id
SET rr.status = 'approved', rr.reviewed_at = CURRENT_TIMESTAMP(6)
WHERE u.email = 'you@example.com';
```

## Admin user accounts

**`/admin/users`** (Manage user accounts): list all users with decrypted password for AES-stored accounts (lab use), plain **6-character user id**, registration metadata, and actions:

- Set **`account_status`** (e.g. suspended)
- **Suspend** / **activate**
- **Approve** a previously **rejected** account again
- **Hard delete** user and related rows (where business rules allow)

## Roles and main URLs

| Role | After login |
|------|-------------|
| `ADMIN` | `/admin/dashboard` |
| `LIBRARIAN` | `/librarian/dashboard` |
| `STUDENT` | `/student/dashboard` |

**Public:** `/`, `/login`, `/register`, `/register/complete`, static assets under `/css/**`, `/js/**`, `/images/**`, `/error`, `/access-denied`.

**Authenticated:** `/uploads/profiles/**` (profile images).

**Admin-only:** `/admin/**` (dashboard, `/admin/registrations/pending`, `/admin/users`, POST handlers for approve/reject and user actions).

**Librarian-only:** `/librarian/**`  
**Student-only:** `/student/**`

Logout: **`POST /logout`** (form in the UI) → redirects to `/login?logout`.

## Package layout

- `com.library.controller` — MVC controllers  
- `com.library.service` — registration, approval, admin user operations, profile picture storage  
- `com.library.repository` — Spring Data JPA  
- `com.library.entity` — JPA entities aligned with `library_db`  
- `com.library.dto` — form DTOs and admin views  
- `com.library.security` — authentication provider, login handlers, password/user-id helpers, `UserDetails`  
- `com.library.config` — `SecurityConfig`, `WebConfig`, `BrowserLauncher`  
- `com.library.exception` — global error handling  

## Database notes

The schema in **`docs/library_db_schema.sql`** includes tables such as **`deletion_requests`** that are not yet exposed by dedicated controllers in this codebase; related enums and services may be used indirectly (for example when cleaning up on delete). Extend the app against that schema as you add features.
