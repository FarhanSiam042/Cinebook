# CineBook — Movie Ticket Booking Platform

CineBook is a full-stack movie ticket booking system built as a Spring Boot microservices
architecture with a React frontend. A customer can browse movies and theaters, pick a showtime,
select specific seats on a live seat map, and book, payment and confirmation are then processed
automatically through an event-driven backend. An admin dashboard manages the entire catalog:
movies, theaters, screens, seats, users, and a system-wide view of every booking, payment, and
notification.


---

## Table of Contents

1. [What's Included](#whats-included)
2. [Features](#features)
3. [How It Works (Workflow)](#how-it-works-workflow)
4. [Architecture](#architecture)
5. [Authentication & Authorization](#authentication--authorization)
6. [Event-Driven Backend (RabbitMQ)](#event-driven-backend-rabbitmq)
7. [Media Service](#media-service)
8. [Seat Lock Service](#seat-lock-service)
9. [Project Structure](#project-structure)
10. [Getting Started](#getting-started)
11. [Verifying Everything Is Up](#verifying-everything-is-up)
12. [Known Gotchas](#known-gotchas)

---

## What's Included

| Component | Port | Purpose |
|---|---|---|
| Service Registry (Eureka) | 8761 | Service discovery — every service registers itself here |
| Config Server | 8888 | Central configuration, served from `config-repo/` |
| API Gateway | 8080 | Single entry point; routes `/api/**` to the right service |
| User Service | 8081 | Registration, login, JWT issuance, profile, admin user management |
| Movie Service | 8082 | Movie catalog, genres, cast, showtimes |
| Theater Service | 8083 | Theaters, screens, seat layout, seat categories |
| Booking Service | 8084 | Creates bookings; talks to Movie/Theater/Seat Lock services |
| Payment Service | 8085 | Auto-processes payment when a booking is confirmed (event-driven) |
| Notification Service | 8086 | Sends a confirmation notification once payment completes |
| Media Service | 8087 | Image uploads (movie posters), served back by URL |
| Seat Lock Service | 8088 | Real-time, race-condition-safe seat holds (Redis) |
| Frontend | 5173 | React + Vite SPA — the only thing a user/admin actually opens |

Everything above is built and working end-to-end, verified with the full stack running.

---

## Features

**For customers**
- Browse movies and theaters without needing an account
- Register / log in (JWT-based sessions)
- Pick a showtime and select specific seats on a live, color-coded seat map
- Seats are held for 5 minutes while checking out, with a visible countdown, so nobody else can grab them
- Booking confirmation, payment status, and notification status all shown on one page, updating live
- View booking history

**For admins**
- Full CRUD on movies (title, description, cast, genres, poster image, showtimes)
- Upload a real poster image (drag-and-drop file picker) or paste an external URL
- Full CRUD on theaters, screens, and seat layouts (generate a seat grid per screen/category)
- User management
- System-wide dashboards for every booking, payment, and notification in the platform

**Under the hood**
- Every write endpoint is authenticated and role-checked (`ADMIN` vs `CUSTOMER`) — not just hidden in the UI
- Seat selection is protected against double-booking with atomic Redis locks, not just a client-side check
- Payment and notification are never called directly — they react to RabbitMQ events, the way a real production system would decouple them
- Centralized configuration via Spring Cloud Config, with per-service local fallbacks
- Every service is independently discoverable, documented (Swagger/OpenAPI), and horizontally scalable in principle (`lb://` load-balanced routing through Eureka)

---

## How It Works (Workflow)

**Customer booking journey:**
1. Browse movies/theaters on the public homepage — no login required.
2. Log in (or register). A JWT is issued and stored client-side.
3. Open a movie, pick a showtime → the seat picker loads the screen's seat layout and current
   availability.
4. Select seats → click **Continue** → the frontend asks Seat Lock Service to hold those exact
   seats for 5 minutes (atomically — if someone else already grabbed one, you're told immediately
   and only that seat is blocked, nothing else).
5. Enter name/email → **Confirm booking** → Booking Service verifies the hold is still valid,
   fetches showtime/movie/theater details, saves the booking, and publishes a `booking.confirmed`
   event to RabbitMQ.
6. Payment Service consumes that event, auto-creates a payment record, and publishes
   `payment.completed`.
7. Notification Service consumes that event and records a confirmation notification.
8. The booking detail page polls briefly and shows payment + notification status flipping to
   "done" in real time — no manual "pay" step exists; it's fully automatic.

**Admin catalog journey:**
1. Log in as admin.
2. Create a theater → add a screen → generate its seat grid (rows × seats-per-row, by category).
3. Create a movie → optionally upload a poster image → add a showtime (links the movie to a
   theater + screen + price).
4. The movie is now bookable by customers immediately.

---

## Architecture

```
                         ┌─────────────────┐
                         │   Frontend (SPA) │  :5173
                         └────────┬─────────┘
                                  │  every request goes through the gateway
                         ┌────────▼─────────┐
                         │   API Gateway     │  :8080
                         └────────┬─────────┘
        ┌───────────┬────────────┼────────────┬───────────┬─────────────┐
        ▼           ▼            ▼             ▼           ▼             ▼
   User Service  Movie Service  Theater     Booking     Media        Seat Lock
     :8081         :8082       Service     Service      Service      Service
                                :8083       :8084        :8087        :8088
                                               │
                                   booking.confirmed (RabbitMQ)
                                               ▼
                                        Payment Service :8085
                                               │
                                   payment.completed (RabbitMQ)
                                               ▼
                                     Notification Service :8086

   Service Registry (Eureka, :8761) — every service above registers with it
   Config Server (:8888) — every service pulls its config from config-repo/ at startup
   Redis (:6379) — backs Seat Lock Service only
   MySQL ×6 (via Docker) — one database per business service, never shared
```

**Tech stack:** Java 21 (Spring Boot 4.1.0, Spring Cloud 2025.1.2), Maven, MySQL 8, Redis 7,
RabbitMQ 3, React 19 + Vite + Tailwind CSS.

---

## Authentication & Authorization

Every business service validates JWTs itself (no separate auth gateway filter) — each has its
own `security/JwtAuthFilter` + `security/JwtSupport` + `config/SecurityConfig`.

- **Algorithm:** HMAC-SHA384 via `io.jsonwebtoken` (jjwt) 0.12.6
- **Shared secret:** the same `app.jwt.secret` value in every service's `application.yml`.
  User Service is the only service that *issues* tokens; every other service only *validates*
  them.
- **Claims:** `sub` = username, `roles` = list of role strings (`CUSTOMER`, `ADMIN`)
- **Authorization pattern:**
  - User/Movie/Theater/Media Service: `GET` is public, everything else requires `ADMIN`
  - Booking/Payment/Notification Service: every endpoint requires a logged-in user
  - Seat Lock Service: reading the seat map is public, holding seats requires login, but the
    `confirm`/`release` endpoints are deliberately unauthenticated — they're protected by
    possession of an unguessable hold token instead (see [Seat Lock Service](#seat-lock-service))
- **Default admin account:** `admin` / `Admin123!`, seeded automatically by User Service on
  first boot if it doesn't already exist
- **Swagger:** the "Authorize" token persists across every service in the API Gateway's
  aggregated Swagger UI dropdown

---

## Event-Driven Backend (RabbitMQ)

Booking → Payment → Notification never call each other directly over REST — they're chained
through RabbitMQ so each service stays independently deployable and the chain degrades
gracefully if one consumer is temporarily down.

Both events go through the `cinebook.events` direct exchange, routed by a key equal to the
queue name.

**`booking.confirmed`** — published by Booking Service, consumed by Payment Service:
```json
{
  "bookingId": 1,
  "bookingReference": "string",
  "customerName": "string",
  "customerEmail": "string",
  "movieId": 1,
  "movieTitle": "string",
  "theaterId": 1,
  "theaterName": "string",
  "showTime": "2026-08-09T20:00:00",
  "seatCount": 2,
  "amount": 25.50
}
```
(Specific seat IDs/labels aren't in the event — they're only needed for display, so they're
fetched via `GET /api/bookings/reference/{ref}` instead.)

**`payment.completed`** — published by Payment Service, consumed by Notification Service:
```json
{
  "paymentId": 1,
  "bookingId": 1,
  "bookingReference": "string",
  "amount": 25.50,
  "transactionReference": "string",
  "status": "COMPLETED",
  "processedAt": "2026-08-08T19:52:13.565584"
}
```

**Cross-service deserialization note:** Spring AMQP's `Jackson2JsonMessageConverter` stamps
messages with a `__TypeId__` header containing the *publisher's* fully-qualified class name,
which doesn't exist on the consumer's classpath (each service keeps its own copy of the DTO
under its own package). Notification Service works around this with
`Jackson2JavaTypeMapper.TypePrecedence.INFERRED` on its converter bean, deserializing using the
`@RabbitListener` method's declared parameter type instead of the header.

---

## Media Service

Handles image uploads (movie posters today; generic enough for anything else later). No
database — uploaded files are written straight to `media-service/media-storage/` (gitignored)
as `<uuid>.<ext>` and served back by that filename.

| Endpoint | Auth | Notes |
|---|---|---|
| `POST /api/media/images` | ADMIN | `multipart/form-data`, field name `file`. PNG/JPEG/WEBP/GIF up to 5MB. Returns `{ id, url, contentType, size }`. |
| `GET /api/media/images/{id}` | public | Streams the file with a long-lived cache header — has to be public since it's used directly in an `<img src>`. |
| `DELETE /api/media/images/{id}` | ADMIN | Removes the file from disk. |

The admin movie form uploads directly through this service and auto-fills the poster field with
a live preview; pasting an external image URL still works too.

---

## Seat Lock Service

Owns real-time seat availability per showtime — the piece that actually prevents two customers
from booking the same seat at the same time.

**Why Redis, not MySQL:** seat holds are short-lived (5-minute TTL) and high-churn — a textbook
fit for Redis instead of a relational table. The concurrency guarantee comes from
`SET key value NX EX <ttl>` (atomic set-if-absent-with-expiry): two simultaneous requests for
the same seat can never both win.

| Endpoint | Auth | Notes |
|---|---|---|
| `GET /api/seat-locks/showtimes/{id}` | public | `[{seatId, status}]` for every HELD/BOOKED seat; anything absent is available. |
| `POST /api/seat-locks` | logged in | Atomically claims every requested seat, or fails with `409` + the conflicting seat IDs and rolls back anything it partially acquired — never a partial hold. |
| `POST /api/seat-locks/{token}/confirm` | unauthenticated | Converts a hold into a permanent booking. Called server-to-server by Booking Service. |
| `DELETE /api/seat-locks/{token}` | unauthenticated | Releases a hold early. |

The `confirm`/`release` endpoints skip JWT auth deliberately: Booking Service calls `confirm`
without forwarding the customer's token, so the hold token itself — a random UUID only ever
handed to whoever created the hold — acts as the authorization, the same pattern used by
password-reset links.

---

## Project Structure

```
SDA-Lab-Project/
├── service-registry/       Eureka server
├── config-server/          Spring Cloud Config server (reads config-repo/)
├── config-repo/            Centralized YAML config per service
├── api-gateway/             Spring Cloud Gateway
├── user-service/            Auth, users, roles
├── movie-service/           Movies, genres, cast, showtimes
├── theater-service/         Theaters, screens, seats, seat categories
├── booking-service/         Bookings (Feign → movie/theater/seat-lock, publishes events)
├── payment-service/         Consumes booking.confirmed, publishes payment.completed
├── notification-service/    Consumes payment.completed
├── media-service/           Image upload/serving
├── seat-lock-service/       Redis-backed seat holds
├── frontend/                 React + Vite SPA
└── docker-compose.yml        MySQL ×6, RabbitMQ, Redis
```

Every backend service follows the same internal layout: `controller/ · service/ · repository/ ·
model/ · dto/ · config/ · security/`, plus `feign/` (if it calls another service) and `event/`
(if it publishes/consumes RabbitMQ messages).

---

## Getting Started

### Prerequisites
- **Java 21** (movie-service specifically needs **Java 17** — keep both installed if you plan
  to run it; every other service targets 21)
- **Maven** (or just use the included `mvnw`/`mvnw.cmd` wrapper in each service — no separate
  install needed)
- **Docker Desktop** (for MySQL, RabbitMQ, Redis)
- **Node.js 18+** and npm (for the frontend)

### 1. Clone and start infrastructure
```bash
git clone <repo-url>
cd SDA-Lab-Project

# Starts 6 MySQL containers + RabbitMQ + Redis
docker compose up -d
```

### 2. Start the platform services, in order

Open a separate terminal per service. **Registry and Config Server must be up first**;
after that, the rest can start in any order.

```bash
# Terminal 1
cd service-registry
./mvnw spring-boot:run          # Windows: mvnw.cmd spring-boot:run

# Terminal 2 — wait for Terminal 1 to finish starting
cd config-server
./mvnw spring-boot:run

# Terminals 3–10 — any order, any number in parallel
cd user-service          && ./mvnw spring-boot:run
cd movie-service         && ./mvnw spring-boot:run
cd theater-service       && ./mvnw spring-boot:run
cd booking-service       && ./mvnw spring-boot:run
cd payment-service       && ./mvnw spring-boot:run
cd notification-service  && ./mvnw spring-boot:run
cd media-service         && ./mvnw spring-boot:run
cd seat-lock-service     && ./mvnw spring-boot:run

# Terminal 11 — start last, so its routes see every service already registered
cd api-gateway
./mvnw spring-boot:run
```

### 3. Start the frontend
```bash
cd frontend
npm install
npm run dev              # http://localhost:5173
```

### 4. Log in
Use the seeded admin account to reach the `/admin` dashboard:
- **Username:** `admin`
- **Password:** `Admin123!`

Or register a new account for the regular customer experience.

---

## Verifying Everything Is Up

| Check | URL |
|---|---|
| Eureka dashboard — every service should show `UP` | http://localhost:8761 |
| Config Server serving a service's config | http://localhost:8888/user-service/default |
| RabbitMQ management UI (`guest`/`guest`) | http://localhost:15672 |
| Aggregated Swagger UI (every service, via the gateway) | http://localhost:8080/swagger-ui.html |
| Frontend | http://localhost:5173 |
| `docker ps` | 6 MySQL containers + RabbitMQ + Redis, all "Up" |

---

## Known Gotchas

- **movie-service on Java 17:** its `pom.xml` is pinned to Java 17 while every other service
  targets 21 — harmless, just make sure you have JDK 17 available if you run it.
- **Eureka `*.mshome.net` hostname issue:** on some Hyper-V/WSL setups, a service registers with
  an unresolvable hostname, breaking `lb://` gateway routing with a 500. Fixed by
  `eureka.instance.prefer-ip-address: true`, already set on every service.
- **Config precedence:** `config-repo/*.yml` (served by Config Server) overrides a service's
  local `application.yml` for any key defined in both. Local files exist as an offline fallback
  (`spring.config.import: optional:configserver:...`) if Config Server is unreachable at
  startup — keep the two in sync by hand when changing shared settings like gateway routes.
- **Swagger losing your token:** only happens if `springdoc.swagger-ui.persist-authorization:
  true` is missing from whichever service is actually serving the Swagger page you're on
  (usually api-gateway, for the aggregated dropdown).
