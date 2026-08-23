import { useEffect, useState } from 'react'
import { useNavigate, useParams } from 'react-router-dom'
import * as moviesApi from '../api/movies'
import * as theatersApi from '../api/theaters'
import * as bookingsApi from '../api/bookings'
import { extractErrorMessage } from '../lib/api'
import { useAuth } from '../context/AuthContext'
import { useToast } from '../context/ToastContext'
import {
  Badge,
  Button,
  Card,
  ErrorState,
  Input,
  Modal,
  PageLoader,
  SectionHeading,
} from '../components/ui'

export default function MovieDetail() {
  const { id } = useParams()
  const navigate = useNavigate()
  const { isAuthenticated, user } = useAuth()
  const toast = useToast()

  const [movie, setMovie] = useState(null)
  const [theaters, setTheaters] = useState({})
  const [error, setError] = useState('')
  const [selectedShowtime, setSelectedShowtime] = useState(null)
  const [seatCount, setSeatCount] = useState(1)
  const [customerName, setCustomerName] = useState('')
  const [customerEmail, setCustomerEmail] = useState('')
  const [submitting, setSubmitting] = useState(false)
  const [formError, setFormError] = useState('')

  async function load() {
    setError('')
    try {
      const [movieData, theaterList] = await Promise.all([
        moviesApi.getMovie(id),
        theatersApi.listTheaters().catch(() => []),
      ])
      setMovie(movieData)
      const map = {}
      theaterList.forEach((t) => (map[t.id] = t))
      setTheaters(map)
    } catch (err) {
      setError(extractErrorMessage(err))
    }
  }

  useEffect(() => {
    load()
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [id])

  function openBooking(showtime) {
    if (!isAuthenticated) {
      toast.info('Please log in to book seats.')
      navigate('/login', { state: { from: { pathname: `/movies/${id}` } } })
      return
    }
    setSelectedShowtime(showtime)
    setSeatCount(1)
    setCustomerName(user?.fullName || '')
    setCustomerEmail(user?.email || '')
    setFormError('')
  }

  async function submitBooking(e) {
    e.preventDefault()
    setFormError('')
    setSubmitting(true)
    try {
      const booking = await bookingsApi.createBooking({
        customerName,
        customerEmail,
        showtimeId: selectedShowtime.id,
        seatCount: Number(seatCount),
      })
      toast.success(`Booking confirmed! Reference ${booking.bookingReference}`)
      setSelectedShowtime(null)
      navigate(`/bookings/${booking.bookingReference}`)
    } catch (err) {
      setFormError(extractErrorMessage(err))
    } finally {
      setSubmitting(false)
    }
  }

  if (error) return <ErrorState message={error} onRetry={load} />
  if (!movie) return <PageLoader label="Loading movie…" />

  return (
    <div>
      <div className="grid gap-8 md:grid-cols-[280px_1fr]">
        <div className="aspect-[2/3] w-full max-w-xs overflow-hidden rounded-xl border border-slate-800 bg-slate-800 justify-self-center md:justify-self-start">
          {movie.posterUrl ? (
            <img src={movie.posterUrl} alt={movie.title} className="h-full w-full object-cover" />
          ) : (
            <div className="flex h-full w-full items-center justify-center text-6xl text-slate-700">
              🎬
            </div>
          )}
        </div>

        <div>
          <h1 className="text-3xl font-bold text-white">{movie.title}</h1>
          <div className="mt-2 flex flex-wrap items-center gap-2 text-sm text-slate-400">
            {movie.language && <span>{movie.language}</span>}
            {movie.durationMinutes && <span>· {movie.durationMinutes} min</span>}
            {movie.releaseDate && <span>· {movie.releaseDate}</span>}
            {movie.rating != null && <span>· ★ {movie.rating}</span>}
          </div>
          {movie.genres?.length > 0 && (
            <div className="mt-3 flex flex-wrap gap-1.5">
              {movie.genres.map((g) => (
                <Badge key={g} tone="info">
                  {g}
                </Badge>
              ))}
            </div>
          )}
          {movie.description && <p className="mt-4 max-w-2xl text-slate-300">{movie.description}</p>}

          {movie.cast?.length > 0 && (
            <div className="mt-6">
              <h2 className="mb-2 text-sm font-semibold uppercase tracking-wide text-slate-400">
                Cast
              </h2>
              <div className="flex flex-wrap gap-2">
                {movie.cast.map((c) => (
                  <span
                    key={c.id}
                    className="rounded-full border border-slate-800 bg-slate-900 px-3 py-1 text-sm text-slate-300"
                  >
                    {c.actorName}
                    {c.roleInMovie ? ` as ${c.roleInMovie}` : ''}
                  </span>
                ))}
              </div>
            </div>
          )}
        </div>
      </div>

      <div className="mt-10">
        <SectionHeading title="Showtimes" description="Pick a showtime to book your seats." />
        {(!movie.showtimes || movie.showtimes.length === 0) && (
          <p className="text-slate-500">No showtimes scheduled for this movie yet.</p>
        )}
        <div className="grid gap-3 sm:grid-cols-2 lg:grid-cols-3">
          {movie.showtimes?.map((st) => {
            const theater = theaters[st.theaterId]
            return (
              <Card key={st.id} className="flex flex-col gap-2 p-4">
                <p className="font-medium text-white">
                  {new Date(st.startTime).toLocaleString(undefined, {
                    weekday: 'short',
                    month: 'short',
                    day: 'numeric',
                    hour: '2-digit',
                    minute: '2-digit',
                  })}
                </p>
                <p className="text-sm text-slate-400">
                  {theater ? theater.name : `Theater #${st.theaterId}`}
                  {theater?.city ? `, ${theater.city}` : ''}
                </p>
                <p className="text-sm text-slate-500">Screen #{st.screenId}</p>
                <div className="mt-2 flex items-center justify-between">
                  <span className="font-semibold text-emerald-400">${st.price?.toFixed?.(2) ?? st.price}</span>
                  <Button size="sm" onClick={() => openBooking(st)}>
                    Book
                  </Button>
                </div>
              </Card>
            )
          })}
        </div>
      </div>

      <Modal
        open={!!selectedShowtime}
        onClose={() => setSelectedShowtime(null)}
        title={`Book "${movie.title}"`}
      >
        {selectedShowtime && (
          <form onSubmit={submitBooking} className="flex flex-col gap-4">
            <p className="text-sm text-slate-400">
              {new Date(selectedShowtime.startTime).toLocaleString()} ·{' '}
              {theaters[selectedShowtime.theaterId]?.name || `Theater #${selectedShowtime.theaterId}`}
            </p>
            <Input
              label="Your name"
              required
              value={customerName}
              onChange={(e) => setCustomerName(e.target.value)}
            />
            <Input
              label="Email"
              type="email"
              required
              value={customerEmail}
              onChange={(e) => setCustomerEmail(e.target.value)}
            />
            <Input
              label="Number of seats"
              type="number"
              min={1}
              max={20}
              required
              value={seatCount}
              onChange={(e) => setSeatCount(e.target.value)}
            />
            <p className="text-sm text-slate-400">
              Estimated total:{' '}
              <span className="font-semibold text-emerald-400">
                ${(Number(seatCount || 0) * (selectedShowtime.price || 0)).toFixed(2)}
              </span>
            </p>
            {formError && <p className="text-sm text-red-400">{formError}</p>}
            <div className="flex justify-end gap-2">
              <Button type="button" variant="secondary" onClick={() => setSelectedShowtime(null)}>
                Cancel
              </Button>
              <Button type="submit" disabled={submitting}>
                {submitting ? 'Booking…' : 'Confirm booking'}
              </Button>
            </div>
          </form>
        )}
      </Modal>
    </div>
  )
}
