import { useEffect, useState } from 'react'
import { Link } from 'react-router-dom'
import * as moviesApi from '../../api/movies'
import * as theatersApi from '../../api/theaters'
import * as usersApi from '../../api/users'
import * as bookingsApi from '../../api/bookings'
import * as paymentsApi from '../../api/payments'
import * as notificationsApi from '../../api/notifications'
import { CORE_SERVICES } from '../../lib/microservices'
import { Card, Spinner } from '../../components/ui'

const stats = [
  { key: 'movies', label: 'Movies', to: '/admin/movies', loader: () => moviesApi.listMovies() },
  { key: 'theaters', label: 'Theaters', to: '/admin/theaters', loader: () => theatersApi.listTheaters() },
  { key: 'users', label: 'Users', to: '/admin/users', loader: () => usersApi.listUsers() },
  { key: 'bookings', label: 'Bookings', to: '/admin/bookings', loader: () => bookingsApi.listBookings() },
  { key: 'payments', label: 'Payments', to: '/admin/payments', loader: () => paymentsApi.listPayments() },
  {
    key: 'notifications',
    label: 'Notifications',
    to: '/admin/notifications',
    loader: () => notificationsApi.listNotifications(),
  },
]

export default function AdminOverview() {
  const [counts, setCounts] = useState({})

  useEffect(() => {
    stats.forEach(async (s) => {
      try {
        const data = await s.loader()
        setCounts((prev) => ({ ...prev, [s.key]: data.length }))
      } catch {
        setCounts((prev) => ({ ...prev, [s.key]: '—' }))
      }
    })
  }, [])

  return (
    <div className="flex flex-col gap-8">
      <div className="grid gap-4 sm:grid-cols-2 lg:grid-cols-3">
        {stats.map((s) => (
          <Link key={s.key} to={s.to}>
            <Card className="p-5 transition-colors hover:border-indigo-500/60">
              <p className="text-sm text-slate-400">{s.label}</p>
              <p className="mt-1 text-3xl font-bold text-white">
                {counts[s.key] === undefined ? <Spinner className="h-6 w-6 text-slate-600" /> : counts[s.key]}
              </p>
            </Card>
          </Link>
        ))}
      </div>

      <div>
        <h2 className="mb-3 font-semibold text-white">Backend services</h2>
        <div className="grid gap-3 sm:grid-cols-2">
          {CORE_SERVICES.map((s) => (
            <Card key={s.key} className="p-4">
              <div className="flex items-center justify-between">
                <p className="font-medium text-white">{s.name}</p>
                <span className="rounded-full bg-slate-800 px-2 py-0.5 text-xs text-slate-400">
                  :{s.port}
                </span>
              </div>
              <p className="mt-1 text-xs text-slate-500">{s.basePath}</p>
              <a
                href={`http://localhost:${s.port}/swagger-ui.html`}
                target="_blank"
                rel="noreferrer"
                className="mt-2 inline-block text-xs text-indigo-400 hover:underline"
              >
                Open Swagger UI ↗
              </a>
            </Card>
          ))}
        </div>
      </div>
    </div>
  )
}
