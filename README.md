# spring-auth

A Spring Boot study project: a small Todo REST API with JWT-based authentication and role-based authorization.

## Stack

- Java 25, Spring Boot 4.0.x, Gradle (Kotlin DSL)
- Spring Security + OAuth2 Resource Server (JWT, HS256)
- Spring Data JPA — H2 (dev), PostgreSQL (prod)
- MapStruct, Lombok, Bean Validation

## Roles

- `ADMIN` — full access: manage all users, manage own todos. Can create users of any role.
- `OPERATOR` — can register `USER` accounts only.
- `USER` — manage own todos.

An `ADMIN` user is seeded on startup from `app.bootstrap.*` properties. `OPERATOR` and `USER` accounts are created by an `ADMIN` via `POST /users`.

### User registration rules

| Caller | May create |
| --- | --- |
| `ADMIN` | `USER`, `OPERATOR`, `ADMIN` |
| `OPERATOR` | `USER` only |
| anyone else | nothing (403) |

Enforced at `POST /users` via `@PreAuthorize`; violations return `403 Forbidden`.

## Run

### Dev (H2, in-memory)

```bash
./gradlew bootRun
```

Default seeded credentials: `admin/admin`. H2 console at `http://localhost:8080/h2-console`.

### Prod (PostgreSQL via compose)

```bash
docker compose up -d
APP_ADMIN_PASSWORD=... \
APP_JWT_SECRET=... \
SPRING_PROFILES_ACTIVE=prod \
./gradlew bootRun
```

## Authentication

Authentication is handled with JWTs signed with HMAC-SHA256. Obtain a token at `POST /auth/login`, then send it as `Authorization: Bearer <token>` on subsequent requests.

```bash
TOKEN=$(curl -s -X POST http://localhost:8080/auth/login \
  -H 'Content-Type: application/json' \
  -d '{"username":"admin","password":"admin"}' | jq -r .accessToken)

curl http://localhost:8080/auth/me -H "Authorization: Bearer $TOKEN"
```

JWT settings live under `app.security.jwt`:

| Property | Env | Default (dev) |
| --- | --- | --- |
| `secret` | `APP_JWT_SECRET` | dev-only placeholder (≥ 32 bytes) |
| `ttl-minutes` | `APP_JWT_TTL_MINUTES` | `60` |
| `issuer` | `APP_JWT_ISSUER` | `spring-auth` |

In `prod`, `APP_JWT_SECRET` is required.

## Endpoints

| Method | Path | Auth | Notes |
| --- | --- | --- | --- |
| POST | `/auth/login` | public | returns `{ tokenType, accessToken, expiresInSeconds }` |
| GET | `/auth/me` | any role | current user |
| POST | `/users` | `ADMIN` (any role) / `OPERATOR` (`USER` only) | register a user — see [registration rules](#user-registration-rules) |
| GET | `/users` | `ADMIN` | list users |
| GET | `/users/{id}` | `ADMIN` | get user |
| DELETE | `/users/{id}` | `ADMIN` | delete user |
| POST | `/todos` | `ADMIN`, `USER` | create todo |
| GET | `/todos` | `ADMIN`, `USER` | list accessible todos |
| GET | `/todos/{id}` | `ADMIN`, `USER` | get todo (owner or admin) |
| PUT | `/todos/{id}` | `ADMIN`, `USER` | update todo (owner or admin) |
| DELETE | `/todos/{id}` | `ADMIN`, `USER` | delete todo (owner or admin) |

## Project layout

```
src/main/java/com/santos/spring_auth/
├── config/         # SecurityConfig, JwtProperties, BootstrapProperties, DataBootstrap
├── controller/     # AuthController, UserController, TodoController
├── dto/            # auth, user, todo, error DTOs
├── entity/         # User, Todo
├── enumeration/    # Role
├── exception/      # domain exceptions + GlobalExceptionHandler
├── gateway/        # AuthenticatedUserGateway
├── mapper/         # MapStruct mappers
├── repository/     # JPA repositories
└── service/        # UserService, TodoService, JwtService, AppUserDetailsService
```

## Build & test

```bash
./gradlew build      # compile + test
./gradlew test       # tests only
```
