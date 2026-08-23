import { useEffect, useState } from 'react'
import { Link } from 'react-router-dom'
import * as paymentsApi from '../../api/payments'
import { extractErrorMessage } from '../../lib/api'
import { Badge, Card, ErrorState, PageLoader, SectionHeading } from '../../components/ui'

const statusTone = { COMPLETED: 'success', PROCESSING: 'warning', FAILED: 'danger' }

export default function AdminPayments() {
  const [payments, setPayments] = useState(null)
  const [error, setError] = useState('')

  async function load() {
    setError('')
    try {
      const data = await paymentsApi.listPayments()
      data.sort((a, b) => new Date(b.createdAt) - new Date(a.createdAt))
      setPayments(data)
    } catch (err) {
      setError(extractErrorMessage(err))
    }
  }

  useEffect(() => {
    load()
  }, [])

  if (error) return <ErrorState message={error} onRetry={load} />
  if (!payments) return <PageLoader label="Loading payments…" />

  return (
    <div>
      <SectionHeading
        title="All payments"
        description={`${payments.length} payment(s) recorded from the booking.confirmed event stream — payment-service.`}
      />
      <Card className="overflow-x-auto">
        <table className="w-full min-w-[720px] text-left text-sm">
          <thead className="border-b border-slate-800 text-xs uppercase tracking-wide text-slate-500">
            <tr>
              <th className="px-4 py-3">Booking ref</th>
              <th className="px-4 py-3">Customer</th>
              <th className="px-4 py-3">Method</th>
              <th className="px-4 py-3">Transaction ref</th>
              <th className="px-4 py-3">Amount</th>
              <th className="px-4 py-3">Status</th>
              <th className="px-4 py-3">Processed</th>
            </tr>
          </thead>
          <tbody>
            {payments.map((p) => (
              <tr key={p.id} className="border-b border-slate-900 last:border-0 hover:bg-slate-900/50">
                <td className="px-4 py-3">
                  <Link to={`/bookings/${p.bookingReference}`} className="text-indigo-400 hover:underline">
                    {p.bookingReference}
                  </Link>
                </td>
                <td className="px-4 py-3 text-slate-400">{p.customerEmail}</td>
                <td className="px-4 py-3 text-slate-300">{p.paymentMethod || '—'}</td>
                <td className="px-4 py-3 text-slate-500">{p.transactionReference}</td>
                <td className="px-4 py-3 text-emerald-400">${Number(p.amount).toFixed(2)}</td>
                <td className="px-4 py-3">
                  <Badge tone={statusTone[p.status] || 'default'}>{p.status}</Badge>
                </td>
                <td className="px-4 py-3 text-slate-500">
                  {p.processedAt ? new Date(p.processedAt).toLocaleString() : '—'}
                </td>
              </tr>
            ))}
          </tbody>
        </table>
      </Card>
    </div>
  )
}
