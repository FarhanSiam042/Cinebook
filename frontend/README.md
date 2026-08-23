# CineBook Frontend

A React + Vite single-page app for the CineBook microservices backend. It talks
to every backend service through the **API Gateway** — no service is called
directly.

## Running it

Prerequisites: the CineBook backend running locally (service-registry,
config-server, the 6 microservices, MySQL, RabbitMQ — see the repo root
`README.md`), with the API Gateway reachable at `http://localhost:8080`.

```bash
npm install
npm run dev       # http://localhost:5173
```

To point the app at a different gateway URL, copy `.env.example` to `.env`
and set `VITE_API_BASE_URL`.

```bash
npm run build      # production build to dist/
npm run preview    # preview the production build
```

## Logging in

- **Sign up** at `/register` to create a regular customer account.
- **Admin access**: the backend seeds a default administrator on first boot —
  username `admin`, password `Admin123!`. The login page has a shortcut to
  fill these in. Admin users get an extra "Admin" link in the navbar leading
  to `/admin`, a full management dashboard.

## What's implemented

**Public (no login required)**
- Browse/search the movie catalog, filter by genre, view movie details, cast
  and showtimes
- Browse theaters and their screens
- `/services` — a page describing every microservice, its port, and its role

**Customer (logged in)**
- Book seats for a showtime (name/email prefilled from your profile, seat
  count, live price estimate)
- View "My Bookings" and a booking detail page that shows live payment and
  notification status, polling briefly since those are produced
  asynchronously by the backend's RabbitMQ event chain
- Edit profile, change password

**Admin (`/admin`)**
- Dashboard overview with record counts per service and links to each
  service's own Swagger UI
- Movies: create/edit/delete, manage showtimes and cast per movie
- Theaters: create/edit/deactivate, manage screens, manage seat categories,
  generate a screen's seat map and preview it
- Users: list all users, create new users with arbitrary roles
  (CUSTOMER/ADMIN)
- System-wide monitors for all bookings, payments, and notifications

## Microservices this UI talks to

All requests go to the gateway at `http://localhost:8080`, which routes by
path to the service below. See `/services` in the running app, or
`src/lib/microservices.js`, for the full breakdown (ports, endpoints,
auth rules).

| Service | Port | Purpose |
|---|---|---|
| API Gateway | 8080 | Single entry point, routes to every service below |
| user-service | 8081 | Auth (register/login/JWT), profiles, admin user management |
| movie-service | 8082 | Movie catalog, genres, cast, showtimes |
| theater-service | 8083 | Theaters, screens, seat categories, seat maps |
| booking-service | 8084 | Creates/reads bookings; publishes `booking.confirmed` |
| payment-service | 8085 | Consumes `booking.confirmed`, records payments |
| notification-service | 8086 | Consumes `payment.completed`, records notifications |

Plus infrastructure: **service-registry** (Eureka, 8761), **config-server**
(8888), and **RabbitMQ** (5672 / management UI 15672).

## Notes on backend behavior that shape the UI

- Booking only takes a seat *count*, not specific seat IDs — there's no
  backend support for seat-level selection yet, so the booking form is a
  quantity picker rather than a seat map.
- Payments and notifications are created asynchronously off RabbitMQ events,
  not synchronously when a booking is made — the booking detail page polls
  for a few seconds until they appear.
- Booking/payment/notification list endpoints are system-wide (not scoped to
  the current user), so "My Bookings" filters client-side by the logged-in
  user's email; admins see everything.
