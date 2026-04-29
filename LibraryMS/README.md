# Library Management System (LibraryMS)

Full-stack web application for managing a university-style library with role-based portals for **Admin**, **Librarian**, and **Student**.

This project is built with:
- Java 17
- Spring Boot 3.5.x
- Spring MVC + Thymeleaf
- Spring Security (session-based auth)
- Spring Data JPA / Hibernate
- MySQL 8
- Vanilla JavaScript + shared CSS (`app.css`)

---

## Quick Overview

### Project Description

LibraryMS is a multi-role web application that handles the full library lifecycle: registration and approval, catalog/inventory control, borrow/return workflows, reservation queueing, renewals, fines, receipts, and admin reporting with live status updates.

### How to Run

1. Configure DB in `src/main/resources/application.properties`:
   - `spring.datasource.url`
   - `spring.datasource.username`
   - `spring.datasource.password`
2. Ensure database `library_db` exists.
3. Start the app:

```powershell
cd C:\Users\CodeTech\Downloads\LibraryManagementSystem\LibraryMS
mvn spring-boot:run
```

### Key Features

- Role-based portals: Admin, Librarian, Student
- Book catalog + inventory management
- Borrow requests, issue/return, and undo return workflow
- Reservation queue with automatic expiry and queue resequencing
- Loan renewal with rule-based eligibility messages
- Fine management (unpaid/paid/waived, adjustments, receipts)
- Admin reports (fines, activity, issued, overdue)
- Live updates with interaction-aware refresh pause
- Responsive UI for mobile/tablet/desktop

---

## 1) Project Scope

This application covers the complete borrowing lifecycle:
- User registration and approval
- Book catalog and inventory management
- Borrow request workflow and issue tracking
- Reservation queue and fulfillment
- Returns, undo return flow, and renewal rules
- Fine issuance, waiver/adjustment, payment status, and receipts
- Admin reports for fines, activity, issued books, and overdue books
- Live updates across portals for near real-time status changes

---

## 2) Core Portals and Functionalities

### Admin Portal
- Dashboard navigation and maintenance controls
- Registration approval/rejection workflow
- User account management
- Reports:
  - Fines report (unpaid/paid/waived)
  - Activity report (active, completed, overdue, total loans)
  - Issued books report
  - Overdue report
- Live report updates

### Librarian Portal
- Book add/update/delete with inventory consistency checks
- Borrow request review and approval/rejection
- Active loans management and return processing
- Undo return with business rule checks
- Reservation queue management:
  - View ready/all/expired reservations
  - Fulfill and cancel actions
  - Automatic expiration of pickup windows
- Fine status management and receipt printing

### Student Portal
- Search books and submit borrow requests
- Reserve unavailable books with queue tracking
- View/cancel own reservations
- View active and historical loans
- Renew eligible loans with clear block reasons
- View personal fines
- Live updates for books, loans, fines, requests, and reservations

---

## 3) Important Business Rules Implemented

- Book deletion allowed only when not actively borrowed.
- Deleting an eligible book also removes related requests/reservations/history entries tied to that book path.
- Reservation queue positions are kept unique per book for active reservations.
- Reservation status lifecycle includes `PENDING`, `READY`, `FULFILLED`, `CANCELLED`, `EXPIRED`.
- READY reservations automatically expire if pickup window passes.
- Pickup window capped at **4 days (96 hours maximum)**.
- Live refresh pauses while user is interacting (focused controls/text selection) to avoid UX disruption.
- Alerts/messages remain visible for at least 5 seconds.
- Renewals:
  - Allowed when loan is active and renewals remain
  - Blocked for overdue items
  - Unpaid fines alone do not block renewal (per current rules)
- Undo return is locked when it would violate max active-loan constraints.

---

## 4) Fine and Reporting Logic Highlights

- Fine management supports unpaid, paid, and waived states.
- Waived adjustments and net amounts are reflected in UI and reports.
- Paid/waived reporting uses net-aware calculations.
- Legacy fine data compatibility handled in report service logic.
- Student/librarian receipt views show per-book fine context and totals.

---

## 5) Live Update & UX Improvements

- Polling interval standardized to **1 second** for dynamic pages.
- Auto-refresh behaves like soft live updates (no full-page refresh feel).
- Refresh safely pauses during interaction:
  - focused input/select/button/contenteditable elements
  - active text selection
  - recently shown alert window
- Global responsive styling improved for mobile/tablet/desktop.

---

## 6) Security and Access Control

- Role-based route protection (`ADMIN`, `LIBRARIAN`, `STUDENT`).
- Session-based login/logout with Spring Security.
- Registration + approval flow before account activation.
- UI-friendly error handling with user-facing messages.
- Autofill hardening on auth forms (`autocomplete` handling + client-side anti-autofill script).

---

## 7) Data Consistency and Concurrency

- Transactional service methods on critical workflows.
- Pessimistic locking for critical record transitions.
- Queue resequencing to maintain deterministic reservation ordering.
- Schema migration services for safe startup adjustments (e.g., reservation index strategy, borrow-request constraint updates).

---

## 8) Project Structure

- `src/main/java/com/library/controller` - web controllers
- `src/main/java/com/library/service` - business services
- `src/main/java/com/library/repository` - JPA repositories
- `src/main/java/com/library/entity` - domain entities
- `src/main/java/com/library/security` - auth/security integration
- `src/main/resources/templates` - Thymeleaf views
- `src/main/resources/static/css/app.css` - shared styling
- `src/main/resources/static/js` - frontend behavior (live updates, alerts, autofill hardening)
- `src/main/resources/application.properties` - runtime configuration

---

## 9) Prerequisites

- JDK 17+
- Maven 3.9+
- MySQL 8+
- Database: `library_db`

---

## 10) Setup and Run

1. Configure DB in `src/main/resources/application.properties`:
   - `spring.datasource.url`
   - `spring.datasource.username`
   - `spring.datasource.password`
2. Ensure schema exists (or allow JPA update strategy as configured).
3. Start application:

```powershell
cd C:\Users\CodeTech\Downloads\LibraryManagementSystem\LibraryMS
mvn spring-boot:run
```

Default port is `8081` (configurable).

---

## 11) Key Configuration

- `server.port=8081`
- `spring.jpa.hibernate.ddl-auto=update`
- `app.fine.per-day-rate=10.0`
- `app.reservation.pickup-window-hours=96` (4 days max)
- `app.reservation-auto-cancel-check-ms` (scheduled expiry check frequency)

---

## 12) API / Route Notes

Primary route families:
- Auth: `/login`, `/register`, `/logout`
- Admin: `/admin/...`
- Librarian: `/librarian/...`
- Student: `/student/...`
- Book JSON API: `/api/books` (for list/search integrations)

---

## 13) Maintenance Notes

- Keep MySQL server time and app timezone aligned for due-date/expiry precision.
- For production:
  - disable dev conveniences (such as browser auto-open)
  - externalize secrets and DB credentials
  - enable HTTPS and secure cookie/session settings
  - add backup and monitoring pipeline

---

## 14) Current Status

The system includes multi-role operational workflows, live-updating UI behavior, reservation automation, fine/report consistency improvements, and responsive design tuning across the main interface.

---

## 15) Database Schema Source of Truth

- Canonical schema file: `docs/library_db_schema.sql`
- This file should be treated as the baseline for new environments and handovers.
- It has been refreshed from the current running database structure to include recent reservation/fine/report related updates.

### Regenerate schema file (when DB structure changes)

Run from project root (`LibraryMS`):

```powershell
mysqldump -h localhost -u root -p<YOUR_PASSWORD> --no-data --routines --events --triggers --skip-comments --set-gtid-purged=OFF library_db > docs/library_db_schema.sql
```

After regeneration:
- review diff
- run app compile/start checks
- commit schema and code changes together

---

## 16) Developer Handover Notes (Important)

### Reservation logic
- Active reservation states are `PENDING` and `READY`.
- History states are `FULFILLED`, `CANCELLED`, `EXPIRED`.
- READY pickup expiry is automatic (scheduled job), with maximum 4-day window.
- Queue positions are resequenced with locking to avoid duplicate queue numbers for the same book.

### Live updates
- Global polling is configured for frequent updates.
- Refresh pauses while user is interacting (focus/selection/alerts) to avoid broken UX.
- If UI appears stale, confirm page is not in paused-interaction state.

### Fine/report calculations
- Net amounts and waived adjustments are intentionally separated.
- ReportService contains compatibility logic for older waived records.
- If totals look off, inspect both `amount` and `waivedAmount` data paths.

### Schema migration services
- Startup includes migration services that adjust constraints/indexes.
- If startup fails on schema migration:
  - inspect logs for index/foreign-key dependency conflicts
  - apply manual SQL migration once, then restart
- Keep code migrations and `docs/library_db_schema.sql` in sync.

### Build/verification checklist before merging
- `mvn -DskipTests compile`
- smoke test login + each role dashboard
- verify one end-to-end borrow/return/reservation/fine flow
- verify admin reports load without error

---

## 17) Recommended Future Enhancements

- Add integration tests for reservation expiry and queue ordering.
- Add migration versioning framework (Flyway/Liquibase) for explicit schema history.
- Add environment-specific config profiles and secret management.
- Add CI pipeline for compile + test + lint + schema consistency checks.
