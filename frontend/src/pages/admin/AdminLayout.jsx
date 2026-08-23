import { NavLink, Outlet } from 'react-router-dom'
import { SectionHeading } from '../../components/ui'

const items = [
  { to: '/admin', label: 'Overview', end: true },
  { to: '/admin/movies', label: 'Movies & Showtimes' },
  { to: '/admin/theaters', label: 'Theaters & Seats' },
  { to: '/admin/users', label: 'Users' },
  { to: '/admin/bookings', label: 'Bookings' },
  { to: '/admin/payments', label: 'Payments' },
  { to: '/admin/notifications', label: 'Notifications' },
]

export default function AdminLayout() {
  return (
    <div>
      <SectionHeading
        eyebrow="Admin"
        title="Admin dashboard"
        description="Manage the catalog, theaters, and users, and monitor bookings across every service."
      />
      <div className="grid gap-6 md:grid-cols-[220px_1fr]">
        <nav className="flex gap-1 overflow-x-auto md:flex-col md:overflow-visible">
          {items.map((item) => (
            <NavLink
              key={item.to}
              to={item.to}
              end={item.end}
              className={({ isActive }) =>
                `whitespace-nowrap rounded-lg px-3 py-2 text-sm font-medium transition-colors ${
                  isActive
                    ? 'bg-indigo-500/15 text-indigo-300'
                    : 'text-slate-400 hover:bg-slate-800 hover:text-white'
                }`
              }
            >
              {item.label}
            </NavLink>
          ))}
        </nav>
        <div className="min-w-0">
          <Outlet />
        </div>
      </div>
    </div>
  )
}
