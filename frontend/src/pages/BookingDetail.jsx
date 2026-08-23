import { useCallback, useEffect, useRef, useState } from 'react'
import { useParams } from 'react-router-dom'
import * as bookingsApi from '../api/bookings'
import * as paymentsApi from '../api/payments'
import * as notificationsApi from '../api/notifications'
import { extractErrorMessage } from '../lib/api'
import { Badge, Card, ErrorState, PageLoader, SectionHeading, Spinner } from '../components/ui'

const statusTone = {
  CONFIRMED: 'success',
  COMPLETED: 'success',
  SENT: 'success',
  PENDING: 'warning',
  PROCESSING: 'warning',
  FAILED: 'danger',
  CANCELLED: 'danger',
}

function Timeline({ booking, payment, notification }) {
  const steps = [
    { label: 'Booking confirmed', done: !!booking, sub: booking?.bookingReference },
    {
      label: 'Payment processed',
      done: !!payment,
      pending: !payment,
      sub: payment ? `${payment.paymentMethod || 'Payment'} · ${payment.status}` : 'Waiting for payment-service to process the event…',
    },
    {
      label: 'Notification sent',
      done: !!notification,
      pending: !notification,
      sub: notification ? `${notification.channel || 'Notification'} · ${notification.status}` : 'Waiting for notification-service…',
    },
  ]
  return (
    <div className="flex flex-col gap-4">
      {steps.map((s, i) => (
        <div key={i} className="flex items-start gap-3">
          <div
            className={`mt-0.5 flex h-6 w-6 shrink-0 items-center justify-center rounded-full text-xs ${
              s.done ? 'bg-emerald-500/20 text-emerald-300' : 'bg-slate-800 text-slate-500'
            }`}
          >
            {s.done ? '✓' : i + 1}
          </div>
          <div>
            <p className={`text-sm font-medium ${s.done ? 'text-white' : 'text-slate-400'}`}>
              {s.label}
            </p>
            <p className="flex items-center gap-2 text-xs text-slate-500">
              {s.pending && <Spinner className="h-3 w-3" />}
              {s.sub}
            </p>
          </div>
        </div>
      ))}
    </div>
  )
}

export default function BookingDetail() {
  const { reference } = useParams()
  const [booking, setBooking] = useState(null)
  const [payment, setPayment] = useState(null)
  const [notification, setNotification] = useState(null)
  const [error, setError] = useState('')
  const pollRef = useRef(null)

  const loadAll = useCallback(async () => {
    try {
      const b = await bookingsApi.getBookingByReference(reference)
      setBooking(b)
    } catch (err) {
      setError(extractErrorMessage(err))
      return
    }
    try {
      setPayment(await paymentsApi.getPaymentByBookingReference(reference))
    } catch {
      setPayment(null)
    }
    try {
      setNotification(await notificationsApi.getNotificationByBookingReference(reference))
    } catch {
      setNotification(null)
    }
  }, [reference])

  useEffect(() => {
    loadAll()
  }, [loadAll])

  // Payment/notification are created asynchronously off a RabbitMQ event chain,
  // so poll briefly until both show up (or give up after ~30s).
  useEffect(() => {
    if (payment && notification) return
    if (!booking) return
    let attempts = 0
    pollRef.current = setInterval(() => {
      attempts += 1
      if (attempts > 10) {
        clearInterval(pollRef.current)
        return
      }
      loadAll()
    }, 3000)
    return () => clearInterval(pollRef.current)
  }, [booking, payment, notification, loadAll])

  if (error) return <ErrorState message={error} onRetry={loadAll} />
  if (!booking) return <PageLoader label="Loading booking…" />

  return (
    <div className="mx-auto max-w-3xl">
      <SectionHeading
        eyebrow="Booking"
        title={booking.movieTitle || `Booking ${booking.bookingReference}`}
        description={`Reference ${booking.bookingReference}`}
        action={<Badge tone={statusTone[booking.status] || 'default'}>{booking.status}</Badge>}
      />

      <div className="grid gap-6 md:grid-cols-2">
        <Card className="p-5">
          <h2 className="mb-3 font-semibold text-white">Details</h2>
          <dl className="flex flex-col gap-2 text-sm">
            <Row label="Theater" value={booking.theaterName} />
            <Row
              label="Showtime"
              value={booking.showTime ? new Date(booking.showTime).toLocaleString() : '—'}
            />
            <Row label="Seats" value={booking.seatCount} />
            <Row label="Amount" value={`$${Number(booking.amount).toFixed(2)}`} />
            <Row label="Booked by" value={`${booking.customerName} (${booking.customerEmail})`} />
            <Row
              label="Created"
              value={booking.createdAt ? new Date(booking.createdAt).toLocaleString() : '—'}
            />
          </dl>
        </Card>

        <Card className="p-5">
          <h2 className="mb-4 font-semibold text-white">Order status</h2>
          <Timeline booking={booking} payment={payment} notification={notification} />
        </Card>
      </div>

      {(payment || notification) && (
        <div className="mt-6 grid gap-6 md:grid-cols-2">
          {payment && (
            <Card className="p-5 text-sm">
              <h2 className="mb-3 font-semibold text-white">Payment</h2>
              <dl className="flex flex-col gap-2">
                <Row label="Method" value={payment.paymentMethod} />
                <Row label="Transaction ref" value={payment.transactionReference} />
                <Row label="Status" value={payment.status} />
              </dl>
            </Card>
          )}
          {notification && (
            <Card className="p-5 text-sm">
              <h2 className="mb-3 font-semibold text-white">Notification</h2>
              <dl className="flex flex-col gap-2">
                <Row label="Channel" value={notification.channel} />
                <Row label="Recipient" value={notification.recipientEmail} />
                <Row label="Message" value={notification.message} />
                <Row label="Status" value={notification.status} />
              </dl>
            </Card>
          )}
        </div>
      )}
    </div>
  )
}

function Row({ label, value }) {
  return (
    <div className="flex justify-between gap-4">
      <dt className="text-slate-500">{label}</dt>
      <dd className="text-right text-slate-200">{value ?? '—'}</dd>
    </div>
  )
}
