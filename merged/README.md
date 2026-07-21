# Bookstore Application

A full-stack Java Spring Boot bookstore web app with JWT-secured authentication, book catalog management, and a personal "my books" list feature.

## Features

- User signup/login with JWT authentication (`Login/auth`, `Login/jwt`)
- Role-based endpoint security (`@PreAuthorize`) via Spring Security
- Book catalog CRUD (add, edit, list, delete)
- Personal book list per user
- Server-rendered UI with Thymeleaf (`login`, `register`, `profile`, `bookList`, `bookEdit`, `bookRegister`, `myBooks`, `home`)
- Unit tests for controllers and services (JUnit 5 + Mockito)

## Tech Stack

- Java, Spring Boot, Spring Security, Spring Data JPA
- Thymeleaf, HTML5, CSS
- Maven (via `mvnw`)
- JUnit 5, Mockito

## Running locally

```bash
./mvnw spring-boot:run
```

The app will start on `http://localhost:8080` by default (see `src/main/resources/application.properties` for DB config).

## Running tests

```bash
./mvnw test
```

## Project structure

```
src/main/java/com/abrar/BOOKSTORE/
  Login/         -> auth, JWT, user/role management
  controller/     -> book & book-list endpoints
  entity/         -> Book, MyBookList
  repository/     -> Spring Data repositories
  service/        -> business logic
src/main/resources/templates/  -> Thymeleaf views
src/test/java/...               -> unit tests
```

## Notes

This repo consolidates earlier iterations that were previously split across separate
repositories (with/without JWT auth). This version is the canonical one going forward;
use branches for future variants instead of separate repos.
