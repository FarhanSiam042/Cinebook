import { useEffect, useState } from 'react'
import { Link } from 'react-router-dom'
import * as notificationsApi from '../../api/notifications'
import { extractErrorMessage } from '../../lib/api'
import { Badge, Card, ErrorState, PageLoader, SectionHeading } from '../../components/ui'

const statusTone = { SENT: 'success', PENDING: 'warning', FAILED: 'danger' }

export default function AdminNotifications() {
  const [items, setItems] = useState(null)
  const [error, setError] = useState('')

  async function load() {
    setError('')
    try {
      const data = await notificationsApi.listNotifications()
      data.sort((a, b) => new Date(b.createdAt) - new Date(a.createdAt))
      setItems(data)
    } catch (err) {
      setError(extractErrorMessage(err))
    }
  }

  useEffect(() => {
    load()
  }, [])

  if (error) return <ErrorState message={error} onRetry={load} />
  if (!items) return <PageLoader label="Loading notifications…" />

  return (
    <div>
      <SectionHeading
        title="All notifications"
        description={`${items.length} notification(s) sent from the payment.completed event stream — notification-service.`}
      />
      <Card className="overflow-x-auto">
        <table className="w-full min-w-[720px] text-left text-sm">
          <thead className="border-b border-slate-800 text-xs uppercase tracking-wide text-slate-500">
            <tr>
              <th className="px-4 py-3">Booking ref</th>
              <th className="px-4 py-3">Recipient</th>
              <th className="px-4 py-3">Channel</th>
              <th className="px-4 py-3">Message</th>
              <th className="px-4 py-3">Status</th>
              <th className="px-4 py-3">Sent</th>
            </tr>
          </thead>
          <tbody>
            {items.map((n) => (
              <tr key={n.id} className="border-b border-slate-900 last:border-0 hover:bg-slate-900/50">
                <td className="px-4 py-3">
                  <Link to={`/bookings/${n.bookingReference}`} className="text-indigo-400 hover:underline">
                    {n.bookingReference}
                  </Link>
                </td>
                <td className="px-4 py-3 text-slate-400">{n.recipientEmail}</td>
                <td className="px-4 py-3 text-slate-300">{n.channel || '—'}</td>
                <td className="px-4 py-3 max-w-xs truncate text-slate-500" title={n.message}>
                  {n.message}
                </td>
                <td className="px-4 py-3">
                  <Badge tone={statusTone[n.status] || 'default'}>{n.status}</Badge>
                </td>
                <td className="px-4 py-3 text-slate-500">
                  {n.sentAt ? new Date(n.sentAt).toLocaleString() : '—'}
                </td>
              </tr>
            ))}
          </tbody>
        </table>
      </Card>
    </div>
  )
}
