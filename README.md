# CineBook — Movie Ticket Booking System

**Course:** SWE 4602 – Software Design and Architecture
**Architecture:** Spring Boot Microservices (Eureka + Config Server + API Gateway + RabbitMQ + MySQL)

This README is the single source of truth for the project. Read it before writing any code.
It covers: what's already built, the shared conventions everyone must follow, who owns what,
and the step-by-step instructions for building your assigned service.

---

## 1. Current Status

| Component                            | Status      | Owner    | Port |
| ------------------------------------ | ----------- | -------- | ---- |
| Service Registry (Eureka)            | ✅ Done     | Member 1 | 8761 |
| Config Server                        | ✅ Done     | Member 1 | 8888 |
| Docker Compose (MySQL x5 + RabbitMQ) | ✅ Done     | Member 1 | —    |
| API Gateway                          | ✅ Done     | Member 1 | 8080 |
| User Service                         | ✅ Done     | Member 2 | 8081 |
| Movie Service                        | ✅ Done     | Member 3 | 8082 |
| Theater Service                      | ✅ Done     | Member 4 | 8083 |
| Booking Service                      | ✅ Done     | Member 5 | 8084 |
| Payment Service                      | ✅ Done     | Member 5 | 8085 |
| Notification Service                 | ✅ Done     | Member 1 | 8086 |
| Frontend (React + Vite)              | ✅ Done     | —        | 5173 |

The infra layer (registry, config server, Docker) is already running and pushed to `main`.
**Pull `main` before starting your service.**

---

## 2. Team Task Assignment

> Fill in real names next to each Member number and keep this table updated.

| Member | Name | Owns | Notes |
|---|---|---|---|
| **Member 1** | _______ | API Gateway + Notification Service *(+ infra already built)* | Gateway routes to all 6 services; Notification just listens for `payment.completed` and logs/sends confirmation |
| **Member 2** | _______ | User Service | Auth, JWT issuance, profile, roles (CUSTOMER/ADMIN) |
| **Member 3** | _______ | Movie Service | Movie catalog, genres, cast, showtimes |
| **Member 4** | _______ | Theater Service | Theaters, screens, seat layout, seat categories |
| **Member 5** | _______ | Booking Service + Payment Service | Booking calls Movie + Theater via Feign, then publishes `booking.confirmed`; Payment consumes it and publishes `payment.completed` |

**Why this split:** Booking and Payment are tightly coupled (one triggers the other via RabbitMQ),
so one person owning both avoids cross-person blocking on that event chain. Member 1 balances
out the infra work already done with the two lightest remaining pieces (Gateway is config-only,
Notification is the simplest service).

---

## 3. Fixed Conventions (do not deviate — this is what keeps services compatible)

### Ports
| Service | Port |
|---|---|
| service-registry (Eureka) | 8761 |
| config-server | 8888 |
| api-gateway | 8080 |
| user-service | 8081 |
| movie-service | 8082 |
| theater-service | 8083 |
| booking-service | 8084 |
| payment-service | 8085 |
| notification-service | 8086 |

### Databases (MySQL, via Docker Compose)
| Service | DB Name | Host Port |
|---|---|---|
| user-service | user_db | 3307 |
| movie-service | movie_db | 3308 |
| theater-service | theater_db | 3309 |
| booking-service | booking_db | 3310 |
| payment-service | payment_db | 3311 |
| notification-service | notification_db | 3312 |

MySQL credentials (local dev): `root` / `root`

### RabbitMQ
- Host: `localhost:5672`
- Management UI: `http://localhost:15672` (login: `guest` / `guest`)
- Events used in this project:
  - `booking.confirmed` — published by Booking Service, consumed by Payment Service
  - `payment.completed` — published by Payment Service, consumed by Notification Service

### Java / Package Naming
- Java 17+, Spring Boot 3.x, Maven
- Base package: `com.cinebook.<servicename>` (e.g. `com.cinebook.userservice`)
- Layered structure inside each service:
  ```
  controller/
  service/
  repository/
  model/        (entities)
  dto/
  feign/        (only if your service makes sync calls to another service)
  event/        (only if your service publishes/consumes RabbitMQ events)
  config/
  ```

### Every service must have
- `@EnableDiscoveryClient` (or it's implied by the Eureka client dependency) so it registers with Eureka
- `spring.config.import: optional:configserver:http://localhost:8888` in `application.yml`
- `eureka.client.service-url.defaultZone: http://localhost:8761/eureka`
- Spring Boot Actuator enabled (`/actuator/health` should return `UP`)
- Springdoc OpenAPI enabled (`/swagger-ui.html` should load)

---

## 4. One-Time Setup (everyone runs this after cloning/pulling)

```bash
git pull origin main

# Start infra: MySQL x5 + RabbitMQ
docker compose up -d

# Terminal 1
cd service-registry
mvn spring-boot:run

# Terminal 2 (wait for registry to finish starting first)
cd config-server
mvn spring-boot:run
```

Run `docker compose up -d` from a terminal opened at the project root (`F:\SDA Lab\SDA-Lab-Project`), not inside a Java source file.

**Verify before doing anything else:**
- `http://localhost:8761` → Eureka dashboard loads
- `http://localhost:8888/user-service/default` → returns JSON (not an error)
- `http://localhost:15672` → RabbitMQ management UI loads
- `docker ps` → 5 MySQL containers + RabbitMQ container all show "Up"

For Member 5, start `payment-service` before `booking-service` so the booking event has a consumer ready.

If any of these fail, fix it before building your service — don't build on a broken foundation.

---

## 5. Building Your Service — Step by Step

Each of the 6 services follows the same pattern. Do this inside your assigned service folder.

### Step 1 — Generate the project
Go to [start.spring.io](https://start.spring.io):
- Maven, Java 17, Spring Boot 3.x
- Group: `com.cinebook`, Artifact: `<your-service-name>`
- Dependencies (pick based on your service — see table below)

| Dependency | user | movie | theater | booking | payment | notification |
|---|---|---|---|---|---|---|
| Spring Web | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ |
| Spring Data JPA | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ (or skip if log-only) |
| MySQL Driver | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ (or skip) |
| Eureka Client | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ |
| Config Client | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ |
| Validation | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ |
| Lombok | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ |
| Spring Boot Actuator | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ |
| Springdoc OpenAPI (add manually to pom.xml) | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ |
| Spring Security + JJWT | ✅ only | | | | | |
| OpenFeign | | | | ✅ | | |
| Spring for RabbitMQ (amqp) | | | | ✅ | ✅ | ✅ |

Unzip the download directly into your service folder (e.g. `booking-service/`), replacing the empty folder contents.

### Step 2 — application.yml
Use the port/DB values from Section 3. Example (`booking-service`):

```yaml
server:
  port: 8084
spring:
  application:
    name: booking-service
  config:
    import: optional:configserver:http://localhost:8888
  datasource:
    url: jdbc:mysql://localhost:3310/booking_db
    username: root
    password: root
  jpa:
    hibernate:
      ddl-auto: update
  rabbitmq:
    host: localhost
    port: 5672
    username: guest
    password: guest
eureka:
  client:
    service-url:
      defaultZone: http://localhost:8761/eureka
```

### Step 3 — Build your layers
- **model/** — JPA entities for your domain (e.g. Booking Service: `Booking`, `Ticket`)
- **repository/** — `JpaRepository<Entity, Long>` interfaces
- **service/** — interface + impl with your business logic
- **controller/** — REST endpoints, document with `@Tag`/`@Operation` for Swagger
- **dto/** — request/response objects, don't expose entities directly over REST
- **feign/** — (booking-service only) client interfaces to call Movie Service and Theater Service
- **event/** — (booking/payment/notification only) RabbitMQ publisher/listener classes

### Step 4 — Run and verify
```bash
mvn spring-boot:run
```
Check:
- Your service shows up as `UP` on the Eureka dashboard (`http://localhost:8761`)
- `http://localhost:<your-port>/swagger-ui.html` loads and lists your endpoints
- `http://localhost:<your-port>/actuator/health` returns `{"status":"UP"}`

### Step 5 — Commit
```bash
git checkout -b feature/<your-service-name>
git add .
git commit -m "Implement <your-service-name>: <short summary>"
git push -u origin feature/<your-service-name>
```
Open a PR into `main` when ready. Don't push directly to `main`.

---

## 6. Cross-Service Rules (read this if your service talks to another)

- **Booking Service → Movie Service / Theater Service:** use Feign clients, call by Eureka
  service name (e.g. `movie-service`), not hardcoded `localhost:8082`.
- **Booking → Payment → Notification:** use RabbitMQ events, not direct REST calls.
  Booking publishes `booking.confirmed`; Payment listens, does its thing, then publishes
  `payment.completed`; Notification listens to that.
- **Never** call another service's database directly. Every service owns its own DB — go
  through that service's API/event only.
- Agree on event payload shape (fields in the JSON message) with the relevant teammate
  **before** building your publisher/consumer, so both sides match. Document the agreed
  shape in this README under a new "Event Contracts" section once decided.

### Event Contracts

Both events go through the `cinebook.events` direct exchange, routed by a key equal to the
queue name (e.g. routing key `payment.completed` → queue `payment.completed`).

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
Note: this event does **not** carry `customerEmail` — Notification Service currently derives a
placeholder recipient from `bookingReference` alone. If real email delivery is added later,
extend this event with `customerEmail` (coordinate with Payment Service's owner first).

**Cross-service deserialization gotcha:** Spring AMQP's `Jackson2JsonMessageConverter` stamps
messages with a `__TypeId__` header containing the *publisher's* fully-qualified class name
(e.g. `com.cinebook.paymentservice.dto.PaymentCompletedEvent`). By default the *consumer* trusts
that header and tries to load that exact class — which doesn't exist on the consumer's classpath,
since each service keeps its own copy of the DTO under its own package. Notification Service
works around this by setting `Jackson2JavaTypeMapper.TypePrecedence.INFERRED` on its converter
bean (see `notification-service/.../config/RabbitConfig.java`), which makes it deserialize using
the `@RabbitListener` method's declared parameter type instead of the header. Any new consumer
of a cross-service event should do the same.

---

## 7. API Gateway (Member 1) — ✅ Done

Built on `spring-cloud-starter-gateway-server-webflux` (Spring Cloud Gateway 5.x split the
old `spring-cloud-starter-gateway` into separate WebFlux/WebMvc starters, which also moved the
route config under a `server.webflux` prefix — note this differs from older Gateway tutorials):

```yaml
spring:
  cloud:
    gateway:
      server:
        webflux:
          routes:
            - id: user-service
              uri: lb://USER-SERVICE
              predicates: [Path=/api/users/**]
            - id: movie-service
              uri: lb://MOVIE-SERVICE
              predicates: [Path=/api/movies/**]
            - id: theater-service
              uri: lb://THEATER-SERVICE
              predicates: [Path=/api/theaters/**]
            - id: booking-service
              uri: lb://BOOKING-SERVICE
              predicates: [Path=/api/bookings/**]
            - id: payment-service
              uri: lb://PAYMENT-SERVICE
              predicates: [Path=/api/payments/**]
            - id: notification-service
              uri: lb://NOTIFICATION-SERVICE
              predicates: [Path=/api/notifications/**]
```

All frontend/testing traffic should go through `http://localhost:8080/api/...`, not directly
to individual service ports, once the Gateway is up. Global CORS (allow all origins/methods) is
configured so a browser-based frontend can call the gateway directly.

**Local dev note:** if a downstream service registers with Eureka using an unresolvable hostname
(seen on this Hyper-V/WSL machine as `*.mshome.net`, which the Gateway's DNS resolver can't
resolve, breaking `lb://` routing with a 500), add `eureka.instance.prefer-ip-address: true` to
that service's `application.yml` so it registers with its IP instead.

---

## 8. Definition of Done (per service)

Before marking your service "done" for this phase:
- [ ] Registers with Eureka (visible on dashboard)
- [ ] Pulls config from Config Server without errors
- [ ] Connects to its own MySQL DB, tables auto-created
- [ ] Core CRUD/endpoints for your domain implemented and documented in Swagger
- [ ] If applicable: Feign calls to other services tested and working
- [ ] If applicable: RabbitMQ publish/consume tested (check RabbitMQ UI queues for messages)
- [ ] Reachable through the API Gateway at `/api/<your-service>/...`
- [ ] Pushed to your feature branch, PR opened

---

## 9. Team Sync Checklist

- [ ] All 5 members have pulled `main` and confirmed infra runs locally (Section 4)
- [ ] Names filled into Section 2 table
- [ ] Event payload shapes agreed for `booking.confirmed` and `payment.completed`
- [ ] Weekly check-in to confirm cross-service integration still works after each merge

---

## 10. Frontend

A React + Vite UI lives in `frontend/`, talking only to the API Gateway
(`http://localhost:8080`) — never to individual service ports. It covers
public movie/theater browsing, customer login/booking, and a full admin
dashboard (movies, theaters/screens/seats, users, and system-wide
bookings/payments/notifications monitors) gated behind the `ADMIN` role.

```bash
cd frontend
npm install
npm run dev   # http://localhost:5173
```

Log in as the seeded admin (`admin` / `Admin123!`) to reach `/admin`. See
`frontend/README.md` for the full breakdown of pages and which service each
one calls.