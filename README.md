# Resource Booking System

Production-ready RESTful Resource Booking System — Spring Boot 3.3, Java 17, Spring Security 6, JWT, Spring Data JPA.

## Architecture

```
com.example.bookingsystem
├── config          SecurityConfig, JWTFilter, SwaggerConfig, DataInitializer
├── controller      AuthController, ResourceController, ReservationController
├── dto             AuthRequest, AuthResponse, ResourceDTO, ReservationDTO, ErrorResponse
├── entity          User, Resource, Reservation, Role, Status
├── exception       GlobalExceptionHandler, ResourceNotFoundException, UnauthorizedAccessException
├── repository      UserRepository, ResourceRepository, ReservationRepository
├── security        CustomUserDetails, CustomUserDetailsService, JWTUtil
└── service         UserService, ResourceService, ReservationService
```

## Security model

- **Stateless JWT auth.** `POST /auth/login` returns a bearer token (subject = username, custom `roles` claim). `JWTFilter` runs once per request, validates the token, and populates the `SecurityContext` — no server-side session state.
- **BCrypt** (strength 12) for password hashing everywhere; raw passwords are never stored or logged.
- **RBAC via `@PreAuthorize`** at the service layer (defense in depth — also mirrored at the controller layer for resources):
  - `ADMIN` → full CRUD on resources and reservations.
  - `USER` → read-only on resources; can create/view/manage only their **own** reservations.
- **Identity spoofing is structurally prevented**, not just checked: `ReservationDTO.userId` is annotated `@JsonProperty(access = READ_ONLY)`, so Jackson silently drops any `userId` a client sends in the request body. The service layer always takes the owner from `SecurityContextHolder` (see `UserService#getCurrentAuthenticatedUser`).
- **Data isolation** is enforced in `ReservationService#enforceOwnership`: a request for a reservation that doesn't exist returns `404`, and a request for a reservation that exists but belongs to someone else returns `403` (`UnauthorizedAccessException`), matching the spec. If you need to prevent id-enumeration (hiding *whether* a record exists from non-owners), collapse both cases to a single `404` instead.
- **Global exception handling** (`@RestControllerAdvice`) returns a consistent JSON error shape (`timestamp`, `status`, `error`, `message`, `path`, and `details` for validation errors) for validation failures, not-found, bad credentials, and access-denied — plus a safety-net 500 handler that never leaks stack traces.

## Filtering / pagination / sorting

`GET /api/reservations?status=CONFIRMED&minPrice=10&maxPrice=500&page=0&size=20&sort=startTime,desc`

Built with `JpaSpecificationExecutor` — filters compose dynamically, and for non-admin callers an owner predicate (`user.id = <caller>`) is appended server-side regardless of query params, so a `USER` can never widen results to other people's reservations.

## Running locally

1. Set environment variables (see `application.properties` for the full list and defaults):
   ```bash
   export DB_URL=jdbc:postgresql://localhost:5432/booking_db
   export DB_USERNAME=booking_app
   export DB_PASSWORD=your_db_password
   export JWT_SECRET=$(openssl rand -base64 48)   # 256+ bit random secret
   ```
2. Build & run:
   ```bash
   mvn spring-boot:run
   ```
3. Seeded accounts (created on first startup via `DataInitializer`):
   - `admin` / `admin123` (ROLE_ADMIN)
   - `user` / `user123` (ROLE_USER)
   
   **Change or remove these before deploying to any real environment.**
4. Swagger UI: `http://localhost:8080/swagger-ui.html`

## Example flow

```bash
# 1. Login
curl -X POST http://localhost:8080/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username":"user","password":"user123"}'

# 2. Use the returned token
curl http://localhost:8080/api/reservations \
  -H "Authorization: Bearer <token>"
```

## Notes / things to review before production

- Switch `spring.jpa.hibernate.ddl-auto` to `validate` and manage schema changes with Flyway or Liquibase.
- Tighten CORS `allowedOriginPatterns` in `SecurityConfig` to your real frontend origin(s).
- Rotate `JWT_SECRET` via a secrets manager (AWS Secrets Manager, Vault, etc.), not a plain env var, in real production infra.
- Add refresh-token support if you need sessions longer than the access-token TTL.
