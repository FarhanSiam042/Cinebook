// Static metadata describing the CineBook microservices architecture.
// Used by the "Microservices" overview page and the footer.

export const MICROSERVICES = [
  {
    key: 'api-gateway',
    name: 'API Gateway',
    port: 8080,
    kind: 'gateway',
    basePath: '/api/**',
    description:
      'Spring Cloud Gateway. Single entry point the frontend talks to — reverse-proxies every request to the right backend service via Eureka service discovery.',
    routes: [
      '/api/auth/**, /api/users/** → user-service',
      '/api/movies/** → movie-service',
      '/api/theaters/** → theater-service',
      '/api/bookings/** → booking-service',
      '/api/payments/** → payment-service',
      '/api/notifications/** → notification-service',
    ],
  },
  {
    key: 'user-service',
    name: 'User Service',
    port: 8081,
    kind: 'service',
    basePath: '/api/auth, /api/users',
    description:
      'Identity & access provider for the whole system. Handles registration, login, JWT issuing, profile management, and admin user management. The only service with a database of accounts.',
    routes: [
      'POST /api/auth/register — public',
      'POST /api/auth/login — public',
      'GET/PUT /api/users/me — authenticated',
      'PUT /api/users/me/password — authenticated',
      'GET /api/users — ADMIN only',
      'POST /api/users — ADMIN only (create user with roles)',
    ],
  },
  {
    key: 'movie-service',
    name: 'Movie Service',
    port: 8082,
    kind: 'service',
    basePath: '/api/movies',
    description:
      'Movie catalog: titles, genres, cast, and showtimes. Browsing is public; creating/editing/deleting movies, cast and showtimes is admin-only.',
    routes: [
      'GET /api/movies, /api/movies/{id} — public',
      'GET /api/movies/{id}/showtimes — public',
      'POST/PUT/DELETE /api/movies/{id} — ADMIN only',
      'POST /api/movies/{id}/showtimes, /cast — ADMIN only',
    ],
  },
  {
    key: 'theater-service',
    name: 'Theater Service',
    port: 8083,
    kind: 'service',
    basePath: '/api/theaters',
    description:
      'Theaters, screens, seat categories and seat maps. Browsing is public; managing theaters/screens/seat inventory is admin-only.',
    routes: [
      'GET /api/theaters, /screens, /seats, /seat-categories — public',
      'POST/PUT/DELETE on theaters, screens, seat categories — ADMIN only',
      'POST /api/theaters/screens/{id}/seats/generate — ADMIN only',
    ],
  },
  {
    key: 'booking-service',
    name: 'Booking Service',
    port: 8084,
    kind: 'service',
    basePath: '/api/bookings',
    description:
      'Creates and tracks bookings. On creation it resolves showtime/theater details from movie-service and theater-service via Feign, then publishes a booking.confirmed event to RabbitMQ. Requires login for every endpoint.',
    routes: [
      'POST /api/bookings — authenticated',
      'GET /api/bookings, /api/bookings/{id} — authenticated',
      'GET /api/bookings/reference/{ref} — authenticated',
    ],
  },
  {
    key: 'payment-service',
    name: 'Payment Service',
    port: 8085,
    kind: 'service',
    basePath: '/api/payments',
    description:
      'Consumes booking.confirmed events from RabbitMQ and records a payment automatically — there is no manual "pay" endpoint. Publishes payment.completed once done. Read-only REST API, requires login.',
    routes: [
      'GET /api/payments — authenticated',
      'GET /api/payments/{id} — authenticated',
      'GET /api/payments/booking-reference/{ref} — authenticated',
    ],
  },
  {
    key: 'notification-service',
    name: 'Notification Service',
    port: 8086,
    kind: 'service',
    basePath: '/api/notifications',
    description:
      'Consumes payment.completed events from RabbitMQ and records a notification (e.g. confirmation email/SMS receipt) for the booking. Read-only REST API, requires login.',
    routes: [
      'GET /api/notifications — authenticated',
      'GET /api/notifications/{id} — authenticated',
      'GET /api/notifications/booking-reference/{ref} — authenticated',
    ],
  },
  {
    key: 'service-registry',
    name: 'Service Registry (Eureka)',
    port: 8761,
    kind: 'infra',
    basePath: '/',
    description:
      'Netflix Eureka server. Every service registers itself here on startup; the API Gateway uses it to discover instances by name (lb://SERVICE-NAME).',
    routes: ['Dashboard at http://localhost:8761'],
  },
  {
    key: 'config-server',
    name: 'Config Server',
    port: 8888,
    kind: 'infra',
    basePath: '/',
    description:
      'Spring Cloud Config server serving shared configuration to every service from a native config repo at startup.',
    routes: ['http://localhost:8888/{service-name}/default'],
  },
  {
    key: 'rabbitmq',
    name: 'RabbitMQ',
    port: 5672,
    kind: 'infra',
    basePath: '-',
    description:
      'Message broker carrying the async event chain: booking.confirmed (booking → payment) and payment.completed (payment → notification) on the cinebook.events exchange.',
    routes: ['AMQP on 5672', 'Management UI on 15672 (guest/guest)'],
  },
]

export const CORE_SERVICES = MICROSERVICES.filter((s) => s.kind === 'service')
export const INFRA_SERVICES = MICROSERVICES.filter((s) => s.kind === 'infra')
export const GATEWAY = MICROSERVICES.find((s) => s.kind === 'gateway')
