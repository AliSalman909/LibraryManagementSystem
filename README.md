# Library Management System

This repository contains a **Spring Boot** web application for library user registration, authentication, and role-based dashboards.

## Where the code lives

All source code, Maven build, and documentation are under **`LibraryMS/`**.

- [LibraryMS/README.md](LibraryMS/README.md) — prerequisites, configuration, run instructions, and feature overview

Quick start: install JDK 17+, Maven, and MySQL; create database `library_db` using `LibraryMS/docs/library_db_schema.sql`; configure `LibraryMS/src/main/resources/application.properties`; then from `LibraryMS` run:

```bash
mvn spring-boot:run
```

The app uses `server.port=0` by default so Spring Boot picks a free port; check the console for the URL.
