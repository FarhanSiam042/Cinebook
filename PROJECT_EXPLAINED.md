# CineBook — Full Project Explanation & Q&A Prep

This document explains **what every part of this codebase does, in plain language**, and then
drills into likely viva/Q&A questions with model answers. It is meant to be read top-to-bottom
once, then used as a reference before a demo or a defense.

> Project: a movie-ticket booking system built as **Spring Boot microservices**.
> Course: SWE 4602 – Software Design and Architecture.

---

## Table of Contents

1. [The Big Picture](#1-the-big-picture)
2. [The Building Blocks (Infra)](#2-the-building-blocks-infra)
3. [The Business Services](#3-the-business-services)
4. [How a Request Actually Flows](#4-how-a-request-actually-flows-end-to-end)
5. [The Booking → Payment → Notification Event Chain](#5-the-booking--payment--notification-event-chain-the-heart-of-the-system)
6. [Security & Auth](#6-security--auth)
7. [Data & Databases](#7-data--databases)
8. [Ports & URLs Cheat Sheet](#8-ports--urls-cheat-sheet)
9. [Known Gaps / Things to Be Honest About](#9-known-gaps--things-to-be-honest-about)
10. [Q&A — Deep Dive](#10-qa--deep-dive)

---

## 1. The Big Picture

CineBook is **not one application** — it's 9 small Spring Boot applications that each own one
job, talk to each other over the network, and together behave like one product (browse movies,
pick a theater, book seats, pay, get notified).

```
                                   ┌───────────────────┐
                                   │   Config Server    │  (8888) — hands out application.yml
                                   │  (reads config-repo)│         to every other service at boot
                                   └─────────▲──────────┘
                                             │ pulls config
                     ┌───────────────────────┼────────────────────────┐
                     │                       │                        │
              ┌──────▼──────┐        ┌───────▼───────┐        ┌───────▼───────┐
              │  Eureka     │◄──────►│  API Gateway  │◄──────►│   Client /    │
              │  Registry   │register│    (8080)     │  HTTP  │   Postman /   │
              │  (8761)     │        │ single entry  │        │   Browser     │
              └──────▲──────┘        └───────┬───────┘        └───────────────┘
                     │ register/discover      │ routes by path
       ┌─────────────┼─────────────┬──────────┼───────────┬──────────────┐
       │             │             │          │           │              │
 ┌─────▼────┐  ┌─────▼─────┐ ┌─────▼─────┐┌───▼──────┐┌───▼───────┐┌─────▼──────────┐
 │  User    │  │  Movie    │ │  Theater  ││ Booking  ││ Payment   ││ Notification   │
 │ Service  │  │  Service  │ │  Service  ││ Service  ││ Service   ││ Service        │
 │  8081    │  │  8082     │ │  8083     ││  8084    ││  8085     ││  8086          │
 └────┬─────┘  └────┬──────┘ └────┬──────┘└────┬─────┘└─────┬─────┘└───────┬────────┘
      │             │             │            │  Feign      │ RabbitMQ    │ RabbitMQ
      │             │             │            │  (sync)     │ (async)     │ (async)
   user_db      movie_db      theater_db    booking_db   payment_db   notification_db
   (MySQL)       (MySQL)       (MySQL)       (MySQL)      (MySQL)      (MySQL)
```

Two very different communication styles are used on purpose, to demonstrate both:
- **Synchronous (REST via Feign)**: Booking Service calls Movie Service and Theater Service
  directly and waits for an answer, because it needs their data *right now* to build the booking.
- **Asynchronous (events via RabbitMQ)**: Booking → Payment → Notification is a *chain of
  reactions* — Booking doesn't need to wait around for Payment or Notification to finish, so it
  just fires an event and moves on.

---

## 2. The Building Blocks (Infra)

These 3 pieces aren't "business" services — nobody books a movie through them — but nothing else
works without them.

### 2.1 Service Registry — `service-registry` (Eureka Server, port `8761`)
- A **phone book for services**. Every business service, on startup, registers itself here
  ("Hi, I'm `movie-service`, I live at `192.168.x.x:8082`").
  This is the Netflix Eureka pattern (`spring-cloud-starter-netflix-eureka-client`).
- It's configured with `register-with-eureka: false` and `fetch-registry: false` on **itself**
  — meaning the registry server doesn't try to register with or discover from itself (it's not
  a peer cluster here, just a single standalone instance).
- **Why it matters:** nothing hardcodes another service's IP/port. The API Gateway routes to
  `lb://MOVIE-SERVICE` (a logical name), and Eureka + a client-side load balancer figure out
  the actual `host:port` at request time. Same for Booking Service's Feign clients.
- Dashboard: `http://localhost:8761` — shows every registered instance and its status.

### 2.2 Config Server — `config-server` (port `8888`)
- A **central settings warehouse**. Instead of each service's `application.yml` hardcoding
  everything, each service can also pull shared/override config from here at startup via
  `spring.config.import: optional:configserver:http://localhost:8888`.
- Backed by `config-repo/` (a plain folder of YAML files, one per service —
  `user-service.yml`, `movie-service.yml`, etc.) using the **native** profile
  (`spring.cloud.config.server.native.search-locations: file:../config-repo`) — i.e. it reads
  local files rather than a remote Git repo. In a "real" production setup this folder would
  usually be a separate Git repository, which is what makes Spring Cloud Config's version
  history/rollback features actually useful.
- The `optional:` prefix means "try to load config from the Config Server, but don't crash the
  app if the Config Server is down" — that's why every service can still boot from its own
  local `application.yml` even if `config-server` isn't running.

### 2.3 API Gateway — `api-gateway` (port `8080`)
- The **single front door**. All outside traffic (Postman, a frontend app) should go through
  `http://localhost:8080/api/...`, never directly to `8081`–`8086`.
- Built on **Spring Cloud Gateway** (`spring-cloud-starter-gateway-server-webflux` — the
  reactive/WebFlux flavor; Spring Cloud Gateway 5.x split the old single starter into
  WebFlux/WebMvc variants, so route config lives under `spring.cloud.gateway.server.webflux`).
- It does **path-based routing**: it looks at the URL prefix and forwards to the matching
  service by its Eureka name, e.g.:

  | Incoming path | Forwarded to |
  |---|---|
  | `/api/users/**` | `lb://USER-SERVICE` |
  | `/api/auth/**` | `lb://USER-SERVICE` |
  | `/api/movies/**` | `lb://MOVIE-SERVICE` |
  | `/api/theaters/**` | `lb://THEATER-SERVICE` |
  | `/api/bookings/**` | `lb://BOOKING-SERVICE` |
  | `/api/payments/**` | `lb://PAYMENT-SERVICE` |
  | `/api/notifications/**` | `lb://NOTIFICATION-SERVICE` |

- It also proxies each service's **Swagger/OpenAPI JSON** under `/api-docs/<service>` (with a
  `RewritePath` filter rewriting that to `/v3/api-docs` on the actual service), and aggregates
  them into **one combined Swagger UI** at the gateway (`springdoc.swagger-ui.urls` lists all 6).
  So `http://localhost:8080/swagger-ui.html` can browse and try every service's API from one page.
- **Global CORS** is enabled for all origins/methods (`allowedOriginPatterns: "*"`) so a
  browser-based frontend hosted anywhere can call the gateway without CORS errors.
- **What the gateway does NOT do:** it does not check JWTs, does not do rate limiting, and does
  not do request/response transformation beyond the docs rewrite. It's a pure router. (More on
  the security implication of this in [Section 9](#9-known-gaps--things-to-be-honest-about).)

---

## 3. The Business Services

Every one of the 6 services below follows the **same internal layered structure**:

```
controller/   → REST endpoints (HTTP in/out, calls service layer)
service/      → interface + serviceImpl/ (business logic)
repository/   → Spring Data JPA interfaces (JpaRepository<Entity, Long>)
model/        → JPA @Entity classes (the DB tables)
dto/          → request/response objects — entities are never exposed directly over REST
exception/    → custom exceptions + a @RestControllerAdvice GlobalExceptionHandler
config/       → OpenApiConfig (Swagger metadata), plus RabbitConfig / security config where needed
feign/        → (booking-service only) interfaces for calling other services over REST
event/        → (booking/payment/notification only) event DTOs for RabbitMQ messages
```

Base Java package for every service: `com.cinebook.<servicename>`. Java 21, Spring Boot 4.1.0
(via `spring-boot-starter-parent`), Spring Cloud `2025.1.2`, Maven.

### 3.1 User Service (`8081`) — Auth, profiles, roles
- **Owns:** the `users` table (`user_db`) — id, fullName, email, username, password (BCrypt
  hashed), enabled flag, and a `roles` set (`CUSTOMER` / `ADMIN`, stored in a separate
  `user_roles` join table via `@ElementCollection`).
- **`AuthController`** (`/api/auth/**`) — the only endpoints that don't require a token:
  - `POST /api/auth/register` — creates a new user (default role `CUSTOMER`), returns a JWT.
  - `POST /api/auth/login` — verifies username/password, returns a JWT.
- **`UserController`** (`/api/users/**`) — requires a valid JWT:
  - `GET /api/users/me` — the caller's own profile (identity comes from the token's subject).
  - `PUT /api/users/me`, `PUT /api/users/me/password` — self-service profile/password update.
  - `GET /api/users`, `POST /api/users` — admin-only (`@PreAuthorize("hasRole('ADMIN')")`),
    list all users / create a user directly with a chosen role.
- **How auth works internally:**
  - Spring Security is configured in `SecurityBeansConfig` (stateless sessions, CSRF disabled
    because there's no browser session/cookie to protect, JWT filter added before Spring's
    default `UsernamePasswordAuthenticationFilter`).
  - `JwtService` signs/verifies tokens with **HMAC-SHA (HS256)** using a secret from
    `app.jwt.secret` (base64 string in `application.yml`) via the `jjwt` library. The token's
    subject is the username; claims include `roles` and `fullName`; default expiry
    `app.jwt.expiration-minutes: 1440` (24h).
  - `JwtAuthenticationFilter` (a `OncePerRequestFilter`) reads the `Authorization: Bearer <token>`
    header on every request, validates it, loads the user via `JpaUserDetailsService`, and puts
    an authenticated `UsernamePasswordAuthenticationToken` into Spring Security's context so
    `@PreAuthorize` and `Principal principal` work downstream.
  - This is **entirely local to user-service** — see [Section 6](#6-security--auth) for why that
    matters.

### 3.2 Movie Service (`8082`) — Catalog
- **Owns:** `movies`, `genres` (many-to-many via `movie_genre`), `cast_members`, `showtimes`
  (`movie_db`).
- `MovieController` (`/api/movies/**`): full CRUD on movies (`POST`, `GET /{id}`,
  `GET ?genre=&title=` for filtered listing, `PUT`, `DELETE`), plus nested endpoints
  `POST/GET /{id}/showtimes` and `POST /{id}/cast` to attach showtimes and cast members to a
  movie.
- A `Movie` entity eagerly loads its `genres`, and cascades `showtimes`/`cast` (child rows are
  saved/deleted together with the parent movie — `orphanRemoval = true`).
- **No ticket price field currently exists** on `Movie`/`MovieResponse` — see
  [Section 9](#9-known-gaps--things-to-be-honest-about), this is relevant to how Booking Service
  is supposed to price a booking.

### 3.3 Theater Service (`8083`) — Venues, screens, seat maps
- **Owns:** `theaters`, `screens` (many-to-one to theater), `seat_categories`
  (Silver/Gold/Premium, seeded on startup by `DataInitializer` with price multipliers
  1.00/1.25/1.50), and `seats` (many-to-one to screen + category).
- `TheaterController` (`/api/theaters/**`): CRUD for theaters. Delete is a **soft delete**
  (`active=false`, row stays in the DB) — theaters, screens, and seats all use this
  active-flag pattern instead of hard deletes.
- `ScreenController`: CRUD for screens inside a theater (`totalRows` × `seatsPerRow` grid size).
- `SeatController` / `SeatServiceImpl`: `POST /api/theaters/screens/{screenId}/seats/generate`
  auto-generates the full seat grid for a screen (row labels `A, B, C…` × seat numbers
  `1..seatsPerRow`) against one chosen seat category — can only be run once per screen (throws
  `InvalidOperationException` if seats already exist). `GET .../seats` returns the seat map.
- `SeatCategoryController`: CRUD for the Silver/Gold/Premium categories and their price
  multipliers.
- **No "available seats count" is exposed** by `TheaterResponse` today — see
  [Section 9](#9-known-gaps--things-to-be-honest-about).

### 3.4 Booking Service (`8084`) — Orchestrates a booking
- **Owns:** `bookings` (`booking_db`) — a denormalized snapshot of the booking: customer info,
  `movieId`/`movieTitle`, `theaterId`/`theaterName`, `showTime`, `seatCount`, `amount`, a
  generated `bookingReference` (UUID string) used as the public-facing ID, and a `status`
  (`BookingStatus`, e.g. `CONFIRMED`).
- This is the **most "orchestration-heavy"** service — `POST /api/bookings` does, in order:
  1. **Feign call** to Movie Service (`GET /api/movies/{movieId}`) to fetch movie info
     (title + price).
  2. **Feign call** to Theater Service (`GET /api/theaters/{theaterId}`) to fetch theater info
     (name + available seats) and validates enough seats exist for `seatCount`.
  3. Computes `amount = ticketPrice × seatCount`.
  4. Saves the `Booking` row (status `CONFIRMED`).
  5. **Publishes** a `booking.confirmed` event to RabbitMQ.
  6. Returns the booking to the caller — it does **not** wait for Payment/Notification.
- **Feign clients** (`MovieClient`, `TheaterClient`) are interfaces annotated
  `@FeignClient(name = "movie-service")` / `("theater-service")` — Spring Cloud OpenFeign
  resolves those logical names via Eureka and load-balances the actual HTTP call. If a
  downstream call fails, `FeignException` is caught and rethrown as `ExternalServiceException`
  → mapped to HTTP `502 Bad Gateway` by `GlobalExceptionHandler`.
- Also exposes `GET /api/bookings`, `GET /api/bookings/{id}`,
  `GET /api/bookings/reference/{bookingReference}`.

### 3.5 Payment Service (`8085`) — Reacts to bookings, triggers notifications
- **Owns:** `payments` (`payment_db`) — linked to a booking by `bookingId`/`bookingReference`,
  with `amount`, `paymentMethod` (hardcoded `"ONLINE"` — there's no real payment gateway
  integration, this project simulates payment as "always succeeds"), a generated
  `transactionReference` (UUID), `status` (`PaymentStatus`, e.g. `COMPLETED`), timestamps.
- Has **no controller-driven "create payment" endpoint that starts anything** — payments are
  created **only as a reaction** to the `booking.confirmed` event (see
  [Section 5](#5-the-booking--payment--notification-event-chain-the-heart-of-the-system)).
  `PaymentController` only exposes read endpoints: `GET /api/payments`,
  `GET /api/payments/{id}`, `GET /api/payments/booking/{bookingReference}`.
- Is **idempotent** against duplicate events: `processBookingConfirmed` first looks up an
  existing payment by `bookingReference`; if one already exists and is already `COMPLETED` it
  just returns it without republishing — protects against RabbitMQ redelivering the same message
  twice (e.g. after a consumer crash/requeue).

### 3.6 Notification Service (`8086`) — Reacts to payments
- **Owns:** `notifications` (`notification_db`) — an audit trail of "messages sent":
  `paymentId`, `bookingId`, `bookingReference`, `recipientEmail`, `amount`,
  `transactionReference`, `channel` (`"EMAIL"`), `message` text, `status`
  (`NotificationStatus`, e.g. `SENT`), timestamps.
- **There is no real email/SMS provider wired up.** "Sending" a notification means: build a
  message string, `log.info(...)` it, and persist a `Notification` row as an audit record. This
  is explicitly a simulated integration point — swapping in a real provider (SendGrid, SES,
  Twilio) would only touch `NotificationServiceImpl.sendConfirmation`.
- `NotificationController` exposes read endpoints only: `GET /api/notifications`,
  `GET /api/notifications/{id}`, `GET /api/notifications/booking/{bookingReference}`.
- Like Payment Service, it's idempotent (looks up by `paymentId` before creating a duplicate).

---

## 4. How a Request Actually Flows (End-to-End)

Example: a client calls `POST http://localhost:8080/api/bookings`.

1. **API Gateway** (`8080`) matches the path `/api/bookings/**` → route `booking-service` →
   resolves `lb://BOOKING-SERVICE` via Eureka → picks a live instance → forwards the request
   (path/method/body unchanged, no auth check happens here).
2. **Booking Service** (`8084`) receives it at `BookingController.createBooking`.
3. Booking Service, as a **Feign client**, calls out to `movie-service` and `theater-service`
   directly (bypassing the gateway — internal service-to-service calls go straight through
   Eureka's load balancer, not back through `8080`).
4. Booking Service saves the row to `booking_db` (MySQL, port `3310`) and publishes
   `booking.confirmed` to **RabbitMQ**.
5. Booking Service returns `201 Created` with the booking to the original caller — **at this
   point the HTTP request is already done.**
6. Independently, **Payment Service** (which has been listening on the `booking.confirmed`
   queue since it started up) picks up the message, creates a `payment`, and publishes
   `payment.completed`.
7. Independently, **Notification Service** picks up `payment.completed`, logs + persists a
   `notification`.

Step 6 and 7 happen **asynchronously**, milliseconds to seconds after step 5 — the original HTTP
caller has no visibility into whether payment/notification succeeded unless they separately poll
`GET /api/payments/booking/{ref}` or `GET /api/notifications/booking/{ref}`.

---

## 5. The Booking → Payment → Notification Event Chain (the heart of the system)

This is the part most likely to be asked about in depth, so here's the full mechanical detail.

### 5.1 RabbitMQ topology
- **One exchange**, shared by all 3 services: a **direct exchange** named `cinebook.events`
  (`durable = true`, `autoDelete = false`).
- **Two queues**, each durable: `booking.confirmed` and `payment.completed`.
- **Routing key = queue name.** Because it's a *direct* exchange (not topic/fanout), a message
  published with routing key `"booking.confirmed"` goes to the queue named `booking.confirmed`
  and nowhere else. This is the simplest possible exchange type — appropriate here because
  there's exactly one consumer type per event, no need for wildcard routing.
- **Who declares what:** Payment Service declares *both* queues + bindings (because it both
  consumes `booking.confirmed` and produces `payment.completed`). Booking Service only declares
  the exchange (it's pure publisher — RabbitMQ's queue declaration is idempotent, so it's fine
  for Payment Service to also declare the exchange). Notification Service declares only the
  `payment.completed` queue/binding it consumes.

### 5.2 Publishing side
Both Booking Service and Payment Service publish the same way, via Spring AMQP's
`RabbitTemplate`:
```java
rabbitTemplate.convertAndSend(EVENTS_EXCHANGE, ROUTING_KEY, eventRecord);
```
The `eventRecord` is a plain Java `record`/POJO (e.g. `BookingConfirmedEvent`); Spring AMQP's
`Jackson2JsonMessageConverter` bean serializes it to JSON automatically.

### 5.3 Consuming side
```java
@RabbitListener(queues = RabbitConfig.BOOKING_CONFIRMED_QUEUE)
public void handleBookingConfirmed(BookingConfirmedEvent event) { ... }
```
Spring AMQP deserializes the JSON message body back into the DTO type declared as the method
parameter.

### 5.4 Event contracts (the actual JSON shapes)

**`booking.confirmed`** (Booking → Payment):
```json
{
  "bookingId": 1,
  "bookingReference": "uuid-string",
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

**`payment.completed`** (Payment → Notification):
```json
{
  "paymentId": 1,
  "bookingId": 1,
  "bookingReference": "uuid-string",
  "amount": 25.50,
  "transactionReference": "uuid-string",
  "status": "COMPLETED",
  "processedAt": "2026-08-08T19:52:13.565"
}
```
Note: `payment.completed` does **not** carry `customerEmail`. Notification Service fabricates a
placeholder recipient (`"booking-" + bookingReference + "@cinebook.local"`) since no real
customer email is available to it. Fixing this properly means adding `customerEmail` to the
`payment.completed` contract.

### 5.5 The `__TypeId__` deserialization gotcha (this is a genuinely subtle, good interview point)

Each service keeps its **own private copy** of each event DTO under its own package
(`com.cinebook.paymentservice.dto.PaymentCompletedEvent` in Payment Service,
`com.cinebook.notificationservice.dto.PaymentCompletedEvent` in Notification Service — same
shape, different fully-qualified class name). This is deliberate microservice hygiene: services
don't share a JAR of DTOs, so each stays independently deployable.

The problem: Spring AMQP's default `Jackson2JsonMessageConverter` stamps every outgoing message
with a `__TypeId__` header containing the **publisher's** fully-qualified class name. By default,
the **consumer** trusts that header and tries to `Class.forName(...)` it — which fails, because
`com.cinebook.paymentservice.dto.PaymentCompletedEvent` doesn't exist on Notification Service's
classpath.

**The fix**, in `notification-service`'s `RabbitConfig`:
```java
Jackson2JsonMessageConverter converter = new Jackson2JsonMessageConverter(objectMapper);
converter.setTypePrecedence(Jackson2JavaTypeMapper.TypePrecedence.INFERRED);
```
`INFERRED` tells the converter: "ignore the `__TypeId__` header, just deserialize into whatever
type the `@RabbitListener` method's parameter declares." Since JSON field names line up, this
works. **Any new consumer of a cross-service event must set this too**, or it will throw a
`ClassNotFoundException`/`MessageConversionException` at runtime the moment a message arrives.

### 5.6 Why RabbitMQ instead of a 3rd Feign call?
Booking *could* have synchronously called Payment, which could have synchronously called
Notification — but that would mean:
- Booking's HTTP response is now only as fast as the slowest downstream service.
- If Notification Service is down, does the whole booking fail? (Probably shouldn't.)
- Payment Service and Notification Service become tightly coupled to being *always up* at the
  exact moment a booking happens.

With events: Booking Service only needs RabbitMQ (the broker) to be up, not Payment Service
itself — RabbitMQ holds the durable message until Payment Service is available to consume it.
This is the classic sync-vs-async tradeoff the project is built to demonstrate.

---

## 6. Security & Auth

- **Only User Service has Spring Security configured.** Looking at each service's `pom.xml`,
  only `user-service` pulls in `spring-security` + `jjwt`. Movie/Theater/Booking/Payment/
  Notification have **no authentication filter of their own** — every endpoint on those 5
  services is open to anyone who can reach the port.
- **The API Gateway does not validate JWTs either** (no `TokenRelay`/auth filter configured in
  `api-gateway`'s route config) — it's a pure router.
- **Net effect:** a JWT obtained from `POST /api/auth/login` is meaningful *only* when calling
  `user-service`'s own protected endpoints (`/api/users/**`). Calling
  `/api/movies`, `/api/theaters`, `/api/bookings`, `/api/payments`, `/api/notifications` through
  the gateway requires no token at all today. This is a known scope limitation, not an oversight
  hidden from you — see [Section 9](#9-known-gaps--things-to-be-honest-about) and be ready to
  discuss how you'd fix it (see Q&A).
- **Password storage:** BCrypt, via a `PasswordEncoder` bean (`PasswordConfig`) — never stored
  or compared in plaintext.
- **Token mechanics:** stateless JWT (HS256/HMAC), `Authorization: Bearer <token>` header,
  24-hour default expiry, no refresh-token flow implemented (client must re-login after expiry).
- **Roles:** `CUSTOMER` (default on self-registration) and `ADMIN` (only assignable via the
  admin-only `POST /api/users` endpoint, or manually in the DB). Enforced with
  `@PreAuthorize("hasRole('ADMIN')")` at the method level — Spring prefixes the role with
  `ROLE_` internally, which `hasRole` accounts for automatically.

---

## 7. Data & Databases

- **Database-per-service**, strictly. Each service has its own MySQL 8 instance (via Docker
  Compose) on its own port, and **no service is allowed to touch another service's database
  directly** — the only way to get another service's data is through its API (Feign) or its
  events (RabbitMQ). This is what makes them independently deployable microservices instead of
  a distributed monolith.
- **Schema management:** `spring.jpa.hibernate.ddl-auto: update` everywhere — Hibernate
  auto-creates/updates tables from the `@Entity` classes on startup. Fine for coursework/local
  dev; in a real production system you'd use a migration tool (Flyway/Liquibase) instead, since
  `ddl-auto: update` can silently make destructive-adjacent changes and gives no rollback/audit
  trail.
- **Denormalization by design:** Booking Service stores `movieTitle`/`theaterName` as plain
  columns (not foreign keys into other services' DBs — that's impossible across separate
  databases anyway). This is intentional: it's a snapshot of what the movie/theater were called
  *at booking time*, so historical bookings display correctly even if a movie is later renamed
  or deleted. This is the standard "each service owns a local copy of the data it needs" pattern
  in microservices, sometimes called a read-model/snapshot.

| Service | DB Name | Host Port |
|---|---|---|
| user-service | `user_db` | 3307 |
| movie-service | `movie_db` | 3308 |
| theater-service | `theater_db` | 3309 |
| booking-service | `booking_db` | 3310 |
| payment-service | `payment_db` | 3311 |
| notification-service | `notification_db` | 3312 |

MySQL credentials (local dev only): `root` / `root`.

---

## 8. Ports & URLs Cheat Sheet

| Service | Port | Swagger UI | Health |
|---|---|---|---|
| service-registry (Eureka) | 8761 | — | dashboard at `/` |
| config-server | 8888 | — | `GET /user-service/default` returns JSON |
| api-gateway | 8080 | `/swagger-ui.html` (aggregates all) | `/actuator/health` |
| user-service | 8081 | `/swagger-ui.html` | `/actuator/health` |
| movie-service | 8082 | `/swagger-ui.html` | `/actuator/health` |
| theater-service | 8083 | `/swagger-ui.html` | `/actuator/health` |
| booking-service | 8084 | `/swagger-ui.html` | `/actuator/health` |
| payment-service | 8085 | `/swagger-ui.html` | `/actuator/health` |
| notification-service | 8086 | `/swagger-ui.html` | `/actuator/health` |
| RabbitMQ broker | 5672 | management UI: `:15672` (guest/guest) | — |

**Startup order that actually works:** `service-registry` → `config-server` → Docker Compose
(MySQL ×6 + RabbitMQ) → `payment-service` **before** `booking-service` (so a consumer is ready
before the first `booking.confirmed` event can be published) → everything else → `api-gateway`
last (so it has services to route to when the routes actually get exercised; it'll still start
fine either way, `lb://` just returns `503` until Eureka has entries).

---

## 9. Known Gaps / Things to Be Honest About

Be upfront about these if asked — they read as *awareness*, not as bugs you missed.

1. **`movie.ticketPrice()` / `theater.availableSeats()` don't exist on the actual DTOs.**
   Booking Service's Feign clients (`MovieClient`, `TheaterClient`) declare local response
   records `MovieInfoResponse(Long id, String title, BigDecimal ticketPrice)` and
   `TheaterInfoResponse(Long id, String name, Integer availableSeats)` — but Movie Service's
   real `MovieResponse` has no `ticketPrice` field, and Theater Service's real `TheaterResponse`
   has no `availableSeats` field. Because Feign/Jackson deserializes leniently (unknown JSON
   fields ignored, missing fields left `null` on the target record), this means:
   - `theater.availableSeats()` is always `null` → `validateSeatAvailability` always throws
     `ExternalServiceException` ("Not enough seats available") — **booking creation is broken
     end-to-end today** unless/until those fields are added upstream.
   - `movie.ticketPrice()` would be `null` → `NullPointerException` on
     `.multiply(BigDecimal.valueOf(seatCount))` if the seat check didn't already fail first.
   - **Why this happened:** classic "contract agreed on paper, not enforced in code" —
     the README's cross-service rules say to agree on payload shape before building, but no
     shared contract test/schema enforces it. Good talking point on **consumer-driven contract
     testing** (e.g. Pact) as the real-world fix.
   - **The fix** would be adding a ticket price to `Movie`/`MovieResponse` (Movie Service) and
     an available/total seat count to `TheaterResponse` (Theater Service, likely computed from
     `SeatRepository` counts minus booked seats) — currently Theater Service has no concept of
     "seat is booked" at all (Booking Service doesn't call back to reserve/release seats).
2. **No seat reservation/locking.** Theater Service's seats have no "booked" state — Booking
   Service never marks specific seats as taken. Two customers could "book" the same seats for
   the same showtime with no conflict detection, because seat availability isn't actually
   checked against real seat inventory (see gap #1) or against other bookings for the same
   showtime.
3. **API Gateway and 5 of 6 services have no authentication.** See [Section 6](#6-security--auth).
4. **Payment always succeeds.** `paymentMethod` is hardcoded `"ONLINE"`, there's no real payment
   gateway (Stripe/PayPal) call, no failure/decline path, no retry logic.
5. **No real notification delivery.** Logging + a DB row substitute for actually sending an
   email/SMS.
6. **No API versioning.** All routes are `/api/<resource>`, no `/v1/` prefix — a breaking change
   to any DTO would break every consumer immediately.
7. **`ddl-auto: update`** in every service — acceptable for a course project, not for production.
8. **`config-repo` is a local folder, not Git-backed** — so Config Server's native profile
   doesn't get the version history / rollback that the Git-backed profile would give.

---

## 10. Q&A — Deep Dive

### Architecture & Design

**Q: Why microservices instead of one Spring Boot monolith for a project this size?**
A: Given the actual scale of this app, a monolith would honestly be simpler. The point here is
pedagogical — this project exists to demonstrate the microservices *patterns*: service
discovery, centralized config, an API gateway, sync inter-service calls (Feign), async
event-driven communication (RabbitMQ), and database-per-service isolation. In a real system
you'd only pay this operational complexity tax (9 apps to deploy/monitor instead of 1, network
calls instead of method calls, eventual consistency instead of one transaction) once a single
team/codebase actually becomes a bottleneck — often described as "don't start with
microservices, grow into them."

**Q: What's the difference between what Eureka does and what the Config Server does?**
A: Eureka answers "**where** is `movie-service` running right now" (dynamic, changes every
restart/scale event — an IP:port). Config Server answers "**what settings** should
`movie-service` use" (comparatively static — DB URLs, feature flags, exchange names). They're
orthogonal and both are consulted at startup, but Eureka is also consulted continuously at
request time (every Feign call / gateway route does a fresh lookup / uses a cached registry),
while Config Server is (in this project's setup) essentially a one-shot pull at boot.

**Q: Why does the API Gateway use WebFlux/reactive and not a normal Spring MVC servlet stack?**
A: Spring Cloud Gateway is built specifically on Project Reactor/Netty (non-blocking I/O). A
gateway's job is almost entirely "wait for I/O" (waiting on the downstream service's response) —
a reactive, event-loop-based server handles many concurrent in-flight requests with far fewer
threads than a traditional one-thread-per-request servlet model, which matters a lot for
something that's on the hot path of *every* request in the system.

**Q: Why is `lb://` used in the gateway routes instead of a fixed URL?**
A: `lb://SERVICE-NAME` tells Spring Cloud Gateway to resolve `SERVICE-NAME` through the
load-balancer (backed by Eureka) instead of DNS/a hardcoded host. This means routing survives a
service restarting on a different port/IP, and would transparently load-balance across multiple
instances of the same service if more than one were running (horizontal scaling) — none of that
would work with a literal `http://localhost:8082` URL.

**Q: What is `eureka.instance.prefer-ip-address: true` for, and why did some services need it?**
A: By default a Eureka client registers using its hostname. On this dev setup
(Hyper-V/WSL on Windows), some machines' hostnames resolve to something like `*.mshome.net`
that isn't resolvable from other processes on the same box, which broke the gateway's `lb://`
routing with a 500 error. Forcing registration by IP address sidesteps hostname resolution
entirely. It's a local-dev-environment quirk, not a architectural requirement — a real deployment
(k8s, proper DNS) usually wouldn't need it.

### RabbitMQ / Messaging

**Q: Why a direct exchange and not fanout or topic?**
A: Direct exchange = exact routing-key match to a queue. That's exactly the requirement here:
`booking.confirmed` has exactly one consumer type (Payment Service) and `payment.completed`
has exactly one consumer type (Notification Service) — no need for topic wildcards (`*.confirmed`)
or fanout's broadcast-to-everyone semantics. If, say, both Payment *and* an Analytics service
needed `booking.confirmed`, you'd add a second queue bound to the same exchange with the same
routing key — direct exchanges support multiple queues on one key just fine (it's not
one-consumer-per-key, it's "queues bound to that key get the message").

**Q: What happens if Payment Service is down when Booking Service publishes an event?**
A: Nothing is lost — the queue is declared `durable = true` and RabbitMQ persists messages to
disk for durable queues, so the message sits in the `booking.confirmed` queue until Payment
Service comes back up and its `@RabbitListener` starts consuming again. This is exactly the
resilience benefit of async messaging over a direct synchronous call.

**Q: What if a listener throws an exception while processing a message?**
A: With Spring AMQP's default listener container settings, an unhandled exception triggers the
message to be **requeued** (redelivered) rather than acknowledged — so it'll be retried
(potentially forever, if it deterministically fails every time — this project doesn't configure
a dead-letter queue or a retry-limit, which is a legitimate improvement to mention: e.g. a DLQ
for messages that fail N times, so a bad message can't infinite-loop). Both consumers
(`BookingConfirmedListener`, `PaymentCompletedListener`) here also guard against duplicate
processing (idempotency check by `bookingReference`/`paymentId`), which specifically protects
against a message being redelivered after a *successful* process but *before* acknowledgment.

**Q: Explain the `__TypeId__` header issue in your own words, like I'm not familiar with Spring AMQP.**
A: See [Section 5.5](#55-the-__typeid__-deserialization-gotcha-this-is-a-genuinely-subtle-good-interview-point)
— but the one-sentence version: "the publisher stamps the message with its own Java class name;
the consumer doesn't have that class, so we tell the JSON converter to trust the listener
method's parameter type instead of that header."

**Q: Could you swap RabbitMQ for Kafka here? What would change?**
A: Functionally the pub/sub role is similar, but the semantics differ: RabbitMQ here is used as
a **task queue with fire-and-forget delivery to one logical consumer group per event type** —
message is consumed once, gone from the queue. Kafka is log-based — messages persist for a
retention window and multiple independent *consumer groups* can each read the full stream
independently (e.g. Payment Service and a future Analytics Service could both replay
`booking.confirmed` from the beginning). If replayability/audit-log semantics or very high
throughput were requirements, Kafka would be the better fit; for this project's scale and
"react once, move on" semantics, RabbitMQ is the simpler, correct choice.

### Sync vs Async / Feign

**Q: Why does Booking Service use Feign (synchronous) for Movie/Theater but RabbitMQ
(asynchronous) for Payment/Notification?**
A: Booking genuinely **needs an answer before it can proceed** — it can't create a booking
without knowing the movie's price and confirming theater/seat availability *right now*. That's
a natural fit for a blocking request/response call. Payment and Notification, by contrast, are
**downstream reactions** that don't block the booking from succeeding — the booking is valid and
complete the moment it's saved, whether or not payment has been "processed" yet (in this
simulated flow, payment always succeeds anyway). That asymmetry — "I need this to make a
decision" vs "this needs to happen next" — is the general heuristic for choosing sync vs async.

**Q: What does `@FeignClient(name = "movie-service")` actually do at runtime?**
A: At startup, Spring generates a dynamic proxy implementing the `MovieClient` interface. When
`movieClient.getMovieById(id)` is called, that proxy: (1) asks the load balancer to resolve
`movie-service` to a live `host:port` via Eureka, (2) builds an HTTP `GET` to
`/api/movies/{movieId}` on that instance, (3) sends it, (4) deserializes the JSON response body
into `MovieInfoResponse`. All the HTTP plumbing is hidden — from the calling code's perspective
it looks like a plain Java method call.

**Q: What happens if Movie Service is completely down when Booking Service tries to create a
booking?**
A: The Feign call throws a `FeignException` (e.g. connection refused, or a 5xx from a load
balancer with no healthy instances). `BookingServiceImpl.fetchMovie` catches that and rethrows
as `ExternalServiceException`, which `GlobalExceptionHandler` maps to HTTP `502 Bad Gateway` —
the whole booking fails fast rather than being created with missing/wrong data. This is the
tradeoff of synchronous calls: a downstream outage becomes *your* outage too (unlike the
RabbitMQ case, where Booking Service doesn't care if Payment Service is briefly down).

### Data / Consistency

**Q: How do you keep data consistent across 6 separate databases without distributed
transactions (2PC)?**
A: This project doesn't attempt distributed transactions — it relies on the **eventual
consistency** + **choreography** pattern common in microservices ("Saga pattern" is the formal
name for the overall approach, specifically choreography-based since there's no central
orchestrator — each service reacts to the previous service's event independently). Each local
step (save the booking row, publish the event) happens in one local DB transaction
(`@Transactional` on `createBooking`, `processBookingConfirmed`, etc.) — the multi-step
"business transaction" (booking → payment → notification) is not atomic across services, but
each step is durable and, given RabbitMQ's persistence, will eventually complete even through
restarts. The tradeoff: there's a window where a booking exists but payment hasn't happened yet
— acceptable here since payment can't actually fail in this simulation, but in a real system
you'd need compensating actions (e.g. a "cancel booking" event if payment genuinely failed).

**Q: Why store `movieTitle`/`theaterName` directly on the `Booking` row instead of just the IDs
and looking them up when needed?**
A: Two reasons. First, practically: Booking Service's database can't do a SQL join into Movie
Service's database — they're physically separate MySQL instances, so a foreign key isn't even
possible. Second, by design: it's a point-in-time snapshot. If a movie's title is corrected or a
theater is renamed six months after a booking was made, that booking's receipt should still show
what it said *at the time* — re-fetching live data on every read would make historical records
mutate underneath you.

**Q: What's a compensating transaction, and where would you need one here if you added seat
locking?**
A: If Booking Service actually reserved specific seats via a call to Theater Service and *then*
Payment Service later determined payment genuinely failed, you'd need to publish a
`booking.cancelled` (or `payment.failed`) event that Theater Service listens for, to release
those seats back to available — undoing the earlier side effect since there's no ACID rollback
across services. That's the compensating-transaction half of the Saga pattern, and it's exactly
what's missing today (see gap #2 in [Section 9](#9-known-gaps--things-to-be-honest-about)) since
there's no seat-locking step to compensate for in the first place.

### Security

**Q: Is this system actually secure end-to-end?**
A: No, and it's worth being direct about that rather than overselling it: JWT auth is fully
implemented and correct *within* User Service, but the API Gateway doesn't enforce it, and the
other 5 services have no security filter of their own — so today, anyone who can reach the
gateway can hit `/api/bookings`, `/api/payments`, etc. without a token. See
[Section 6](#6-security--auth).

**Q: How would you actually fix that?**
A: The idiomatic Spring Cloud Gateway approach: add a global `GatewayFilter` (or a
`AuthenticationWebFilter` in the WebFlux security chain) at the gateway that validates the JWT
signature/expiry on every request *before* it's routed downstream, and rejects with `401` if
missing/invalid — centralizing auth enforcement at the one choke point all traffic passes
through, instead of duplicating a JWT filter into 5 more services. The gateway would need the
same signing secret (or, better, move to asymmetric JWT signing — RS256 — so the gateway only
needs the public key, not the secret User Service uses to *sign* tokens). Downstream services
could then trust a header the gateway injects (e.g. `X-User-Id`, `X-User-Roles`) rather than
re-validating the token themselves.

**Q: Why HMAC (HS256) instead of RSA (RS256) for the JWT signature here?**
A: HS256 is symmetric — the same secret both signs and verifies, which is simpler to set up
(one shared secret string in config) and is a completely reasonable choice when only *one*
service (User Service) both issues and would ever need to verify tokens, as is effectively true
today. RS256 (asymmetric) becomes worth the added complexity once multiple services need to
*verify* tokens without being trusted to *issue* them — e.g. if the gateway starts validating
JWTs (see previous answer), giving it the HS256 secret would also let it forge tokens, whereas
handing it only an RSA public key could not.

**Q: Why is CSRF disabled?**
A: CSRF protection defends against a browser being tricked into replaying a user's *cookie-based*
session against a state-changing endpoint. This API is stateless and cookie-free — auth is a
bearer token an explicit client attaches to each request's `Authorization` header, which a
malicious page can't do on the victim's behalf (unlike cookies, which the browser attaches
automatically). CSRF protection is meaningless here and would only get in the way.

### Practical / "show me" questions

**Q: Walk me through what happens if I hit `POST /api/bookings` right now, given the known gap
about `availableSeats`.**
A: Booking Service fetches the theater via Feign; `TheaterInfoResponse.availableSeats()`
deserializes to `null` because Theater Service's actual response has no such field;
`validateSeatAvailability` sees `null`, treats it as "not enough seats," and throws
`ExternalServiceException` → the client gets `502 Bad Gateway` with a message like "Not enough
seats available for theater X." So booking creation currently fails for every request, which is
an honest, demonstrable gap rather than something to paper over.

**Q: How would I verify the whole event chain actually worked for one booking?**
A: Check three things in order: (1) RabbitMQ management UI (`localhost:15672`) — queue message
counts on `booking.confirmed`/`payment.completed` should return to 0 shortly after publishing
(meaning a consumer picked them up); (2) `GET /api/payments/booking/{bookingReference}` — should
show a `COMPLETED` payment tied to that reference; (3)
`GET /api/notifications/booking/{bookingReference}` — should show a `SENT` notification, and the
notification-service logs should show the "Sending EMAIL notification to..." line.

**Q: How do you add a 7th microservice to this system?**
A: (1) Add `spring.config.import: optional:configserver:...` and
`eureka.client.service-url.defaultZone: http://localhost:8761/eureka` to its `application.yml`
so it self-registers; (2) drop a `<service-name>.yml` into `config-repo/` for shared config;
(3) if it needs to call an existing service, add a `@FeignClient(name = "existing-service")`
interface; if it needs to react to an existing event, add a `@RabbitListener` + apply the same
`INFERRED` type-precedence fix from [Section 5.5](#55-the-__typeid__-deserialization-gotcha-this-is-a-genuinely-subtle-good-interview-point);
(4) add a route block to `api-gateway`'s `application.yml` (`Path=/api/<new>/**` →
`lb://<NEW-SERVICE>`) plus a docs-proxy route if you want it in the aggregated Swagger UI.
No existing service needs to be redeployed or even restarted for this — that's the actual payoff
of the architecture.
