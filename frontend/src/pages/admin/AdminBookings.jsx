import { useEffect, useState } from 'react'
import { Link } from 'react-router-dom'
import * as bookingsApi from '../../api/bookings'
import { extractErrorMessage } from '../../lib/api'
import { Badge, Card, ErrorState, PageLoader, SectionHeading } from '../../components/ui'

const statusTone = { CONFIRMED: 'success', PENDING: 'warning', CANCELLED: 'danger' }

export default function AdminBookings() {
  const [bookings, setBookings] = useState(null)
  const [error, setError] = useState('')

  async function load() {
    setError('')
    try {
      const data = await bookingsApi.listBookings()
      data.sort((a, b) => new Date(b.createdAt) - new Date(a.createdAt))
      setBookings(data)
    } catch (err) {
      setError(extractErrorMessage(err))
    }
  }

  useEffect(() => {
    load()
  }, [])

  if (error) return <ErrorState message={error} onRetry={load} />
  if (!bookings) return <PageLoader label="Loading bookings…" />

  return (
    <div>
      <SectionHeading
        title="All bookings"
        description={`${bookings.length} booking(s) across every customer — from booking-service.`}
      />
      <Card className="overflow-x-auto">
        <table className="w-full min-w-[720px] text-left text-sm">
          <thead className="border-b border-slate-800 text-xs uppercase tracking-wide text-slate-500">
            <tr>
              <th className="px-4 py-3">Reference</th>
              <th className="px-4 py-3">Movie</th>
              <th className="px-4 py-3">Customer</th>
              <th className="px-4 py-3">Showtime</th>
              <th className="px-4 py-3">Seats</th>
              <th className="px-4 py-3">Amount</th>
              <th className="px-4 py-3">Status</th>
            </tr>
          </thead>
          <tbody>
            {bookings.map((b) => (
              <tr key={b.id} className="border-b border-slate-900 last:border-0 hover:bg-slate-900/50">
                <td className="px-4 py-3">
                  <Link to={`/bookings/${b.bookingReference}`} className="text-indigo-400 hover:underline">
                    {b.bookingReference}
                  </Link>
                </td>
                <td className="px-4 py-3 text-white">{b.movieTitle}</td>
                <td className="px-4 py-3 text-slate-400">{b.customerName}<br /><span className="text-xs">{b.customerEmail}</span></td>
                <td className="px-4 py-3 text-slate-400">{new Date(b.showTime).toLocaleString()}</td>
                <td className="px-4 py-3">{b.seatCount}</td>
                <td className="px-4 py-3 text-emerald-400">${Number(b.amount).toFixed(2)}</td>
                <td className="px-4 py-3">
                  <Badge tone={statusTone[b.status] || 'default'}>{b.status}</Badge>
                </td>
              </tr>
            ))}
          </tbody>
        </table>
      </Card>
    </div>
  )
}
