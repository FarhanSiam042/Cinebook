import { useEffect, useState } from 'react'
import { Link } from 'react-router-dom'
import * as bookingsApi from '../api/bookings'
import { useAuth } from '../context/AuthContext'
import { extractErrorMessage } from '../lib/api'
import { Badge, Card, EmptyState, ErrorState, PageLoader, SectionHeading } from '../components/ui'

const statusTone = {
  CONFIRMED: 'success',
  PENDING: 'warning',
  CANCELLED: 'danger',
}

export default function MyBookings() {
  const { user, isAdmin } = useAuth()
  const [bookings, setBookings] = useState(null)
  const [error, setError] = useState('')

  async function load() {
    setError('')
    try {
      const all = await bookingsApi.listBookings()
      const mine = isAdmin
        ? all
        : all.filter((b) => b.customerEmail?.toLowerCase() === user?.email?.toLowerCase())
      mine.sort((a, b) => new Date(b.createdAt) - new Date(a.createdAt))
      setBookings(mine)
    } catch (err) {
      setError(extractErrorMessage(err))
    }
  }

  useEffect(() => {
    load()
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [])

  if (error) return <ErrorState message={error} onRetry={load} />
  if (!bookings) return <PageLoader label="Loading your bookings…" />

  return (
    <div>
      <SectionHeading
        eyebrow="Account"
        title="My bookings"
        description={
          isAdmin
            ? 'Showing every booking in the system (admin view).'
            : `Bookings made with ${user?.email}.`
        }
      />
      {bookings.length === 0 && (
        <EmptyState
          title="No bookings yet"
          description="Once you book a showtime, it'll show up here with live payment status."
        />
      )}
      <div className="flex flex-col gap-3">
        {bookings.map((b) => (
          <Link key={b.id} to={`/bookings/${b.bookingReference}`}>
            <Card className="flex flex-wrap items-center justify-between gap-3 p-4 transition-colors hover:border-indigo-500/60">
              <div>
                <p className="font-semibold text-white">{b.movieTitle || `Booking #${b.id}`}</p>
                <p className="text-sm text-slate-400">
                  {b.theaterName} · {new Date(b.showTime).toLocaleString()}
                </p>
                <p className="mt-1 text-xs text-slate-500">
                  Ref: {b.bookingReference} · {b.seatCount} seat(s)
                </p>
              </div>
              <div className="flex items-center gap-3">
                <span className="font-semibold text-emerald-400">
                  ${Number(b.amount).toFixed(2)}
                </span>
                <Badge tone={statusTone[b.status] || 'default'}>{b.status}</Badge>
              </div>
            </Card>
          </Link>
        ))}
      </div>
    </div>
  )
}
