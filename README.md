# BookStore

A book insights/takeaways platform: each book has a cover and a set of 1–10 short
takeaway pages instead of full text. Users browse and rate books, keep a personal
"My Books" list, and share individual books via a public link. Built with Spring
Boot 3.1.2, Java 17, Thymeleaf, MySQL, and Spring Security 6.

## Features

- Browse/search books by genre, with pagination
- Book detail page with cover, takeaway pages, genre, ratings and reviews
- Public share page for a single book (`/available_books/{id}/share`) - viewable
  without logging in
- "My Books" personal list
- Ratings and reviews on books
- Account: signup with email verification, login, forgot/reset password,
  in-app password change, self-service account deletion
- Login is rate-limited per IP to slow down brute-force attempts
- Librarian role: add/edit/delete books, manage genres, bulk import
- Admin panel (`/admin/users`) to change user roles, reset a user's password,
  or delete a user
- Separate JWT-based JSON API under `/api/auth/**` (signup/login) for
  non-browser clients, alongside normal session/cookie login for the website

## Requirements

- Java 17
- Maven
- MySQL running locally (or update the datasource URL for a remote instance)

## Running locally

1. Create a MySQL database named `book` (or change `spring.datasource.url`).
2. Set the environment variables below as needed (all have working defaults
   for local dev except the mail credentials).
3. `mvn spring-boot:run`
4. The app starts on `http://localhost:8081` by default.

On first run, a librarian account is created automatically using the
`LIBRARIAN_*` variables below - there's no separate signup flow for the first
librarian, since regular signup always creates a `ROLE_USER` account.

## Configuration (environment variables)

| Variable             | Default                             | Purpose                                                                                                                                                                                                                                              |
| -------------------- | ----------------------------------- | ---------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------- |
| `DB_USERNAME`        | `abrar`                             | MySQL username                                                                                                                                                                                                                                       |
| `DB_PASSWORD`        | `abrar`                             | MySQL password                                                                                                                                                                                                                                       |
| `JWT_SECRET`         | auto-generated                      | Signing key for JWT API tokens. If not set, one is generated on first run and saved to `~/.bookstore-jwt-secret`, then reused on subsequent runs. Only set this yourself if you need a fixed value (e.g. multiple app instances sharing one secret). |
| `UPLOAD_DIR`         | `uploads` (relative to working dir) | Where uploaded book covers and avatars are stored                                                                                                                                                                                                    |
| `LIBRARIAN_USERNAME` | `librarian`                         | Username for the auto-created librarian account                                                                                                                                                                                                      |
| `LIBRARIAN_EMAIL`    | `librarian@bookstore.local`         | Email for the auto-created librarian account                                                                                                                                                                                                         |
| `LIBRARIAN_PASSWORD` | `change-this-password`              | Password for the auto-created librarian account - change this in any real deployment                                                                                                                                                                 |
| `APP_BASE_URL`       | `http://localhost:8081`             | Used to build links in verification/password-reset emails                                                                                                                                                                                            |
| `MAIL_HOST`          | `smtp.gmail.com`                    | SMTP host                                                                                                                                                                                                                                            |
| `MAIL_PORT`          | `587`                               | SMTP port                                                                                                                                                                                                                                            |
| `MAIL_USERNAME`      | _(empty)_                           | SMTP username                                                                                                                                                                                                                                        |
| `MAIL_PASSWORD`      | _(empty)_                           | SMTP password                                                                                                                                                                                                                                        |
| `MAIL_SSL_ENABLE`    | `false`                             | Set to `true` and `MAIL_PORT=465` if your network blocks STARTTLS on 587                                                                                                                                                                             |

Without real mail credentials, verification and password-reset emails won't
actually be delivered, but the tokens are still created and logged - see
`EmailService`. That also means newly registered accounts stay unverified and
can't log in until mail is configured (or a librarian/admin promotes them
manually).

## Auth model

- The website (`/`, `/login`, `/register`, book pages, etc.) uses standard
  session/cookie login via Spring Security's form login.
- `/api/auth/**` is a separate JWT-based API: `POST /api/auth/signup` and
  `POST /api/auth/login` return a bearer token for use on subsequent
  `/api/**` requests.
- New accounts (from either signup path) must verify their email before they
  can log in at all. The verification link is valid for 24 hours; a new one
  can be requested from `/resend-verification`.

## Tests

```
mvn test
```
