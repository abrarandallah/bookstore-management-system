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

## Requirements

- Java 17
- Maven
- MySQL running locally (or update the datasource URL for a remote instance)

## Running with Docker (recommended - no local MySQL or Maven needed)

1. `cp .env.example .env` and fill in real values (at minimum, set
   `MAIL_USERNAME`/`MAIL_PASSWORD` if you want verification/reset emails to
   actually be delivered - everything else has a working default).
2. `docker compose up --build`
3. Open `http://localhost:8081`.

That's it - MySQL, the app, and uploaded files all persist in Docker
volumes across restarts (`docker compose down` keeps them;
`docker compose down -v` wipes everything for a clean slate).

## Running locally without Docker

1. Create a MySQL database named `book` (or set `DB_HOST`/`DB_PORT`/`DB_NAME`
   to point elsewhere).
2. Set the environment variables below. A `.env` file by itself does
   **nothing** here - only docker-compose reads it automatically. For a
   manual run you need these as real environment variables: either
   `export` them in the same terminal you'll run the app from (add the
   `export` lines to `~/.zshrc`/`~/.bashrc` so they survive new terminals),
   or set them in your IDE's Run Configuration.
3. `./mvnw spring-boot:run` (or `mvnw.cmd spring-boot:run` on Windows)
4. The app starts on `http://localhost:8081` by default.

On first run, a librarian account is created automatically using the
`LIBRARIAN_*` variables below - there's no separate signup flow for the first
librarian, since regular signup always creates a `ROLE_USER` account.

## Configuration (environment variables)

| Variable             | Default                             | Purpose                                                                              |
| -------------------- | ----------------------------------- | ------------------------------------------------------------------------------------ |
| `DB_HOST`            | `localhost`                         | MySQL host (docker-compose sets this to `mysql` automatically)                       |
| `DB_PORT`            | `3306`                              | MySQL port                                                                           |
| `DB_NAME`            | `book`                              | MySQL database name                                                                  |
| `DB_USERNAME`        | `abrar`                             | MySQL username                                                                       |
| `DB_PASSWORD`        | `abrar`                             | MySQL password                                                                       |
| `UPLOAD_DIR`         | `uploads` (relative to working dir) | Where uploaded book covers and avatars are stored                                    |
| `LIBRARIAN_USERNAME` | `librarian`                         | Username for the auto-created librarian account                                      |
| `LIBRARIAN_EMAIL`    | `librarian@bookstore.local`         | Email for the auto-created librarian account                                         |
| `LIBRARIAN_PASSWORD` | `change-this-password`              | Password for the auto-created librarian account - change this in any real deployment |
| `APP_BASE_URL`       | `http://localhost:8081`             | Used to build links in verification/password-reset emails                            |
| `MAIL_HOST`          | `smtp.gmail.com`                    | SMTP host                                                                            |
| `MAIL_PORT`          | `587`                               | SMTP port                                                                            |
| `MAIL_USERNAME`      | _(empty)_                           | SMTP username                                                                        |
| `MAIL_PASSWORD`      | _(empty)_                           | SMTP password                                                                        |
| `MAIL_SSL_ENABLE`    | `false`                             | Set to `true` and `MAIL_PORT=465` if your network blocks STARTTLS on 587             |

Without real mail credentials, verification and password-reset emails won't
actually be delivered, but the tokens are still created and logged - see
`EmailService`. That also means newly registered accounts stay unverified and
can't log in until mail is configured (or a librarian/admin promotes them
manually).

## Auth model

- The website (`/`, `/login`, `/register`, book pages, etc.) uses standard
  session/cookie login via Spring Security's form login. There's no separate
  API - every page is server-rendered Thymeleaf.
- New accounts must verify their email before they can log in at all. The
  verification link is valid for 24 hours; a new one can be requested from
  `/resend-verification`.

## Deployment / HTTPS

This app doesn't terminate TLS or force an HTTPS redirect itself - it
expects a reverse proxy (nginx, Caddy, a cloud load balancer, etc.) in
front of it handling both, and forwarding plain HTTP to `server.port`
(8081) with `X-Forwarded-Proto`/`X-Forwarded-For` headers set correctly.
`server.forward-headers-strategy=framework` (set in
`application.properties`) tells Spring to trust and act on those headers -
this is what makes the session cookie's `Secure` flag turn on correctly in
production, and is only safe to rely on because the assumption is that
nothing untrusted can reach the app directly and spoof them. If you deploy
this without a proxy in front (e.g. exposing port 8081 straight to the
internet), that assumption breaks and you'd need to reconsider this
setting and add your own TLS termination.

For local development (`docker compose up` or `./mvnw spring-boot:run`,
both accessed directly over plain HTTP), none of this matters - there's no
proxy in the loop and the app behaves the same either way.

## Tests

```
mvn test
```
