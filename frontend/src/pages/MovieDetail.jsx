import { useEffect, useRef, useState } from 'react'
import { useNavigate, useParams } from 'react-router-dom'
import * as moviesApi from '../api/movies'
import * as theatersApi from '../api/theaters'
import * as bookingsApi from '../api/bookings'
import * as seatLocksApi from '../api/seatLocks'
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

function seatLabel(seats, seatId) {
  const seat = seats?.find((s) => s.id === seatId)
  return seat ? `${seat.rowLabel}${seat.seatNumber}` : `#${seatId}`
}

export default function MovieDetail() {
  const { id } = useParams()
  const navigate = useNavigate()
  const { isAuthenticated, user } = useAuth()
  const toast = useToast()

  const [movie, setMovie] = useState(null)
  const [theaters, setTheaters] = useState({})
  const [error, setError] = useState('')
  const [selectedShowtime, setSelectedShowtime] = useState(null)
  const [customerName, setCustomerName] = useState('')
  const [customerEmail, setCustomerEmail] = useState('')
  const [submitting, setSubmitting] = useState(false)
  const [formError, setFormError] = useState('')

  const [seats, setSeats] = useState(null)
  const [seatMap, setSeatMap] = useState({})
  const [seatsLoading, setSeatsLoading] = useState(false)
  const [seatsError, setSeatsError] = useState('')
  const [selectedSeatIds, setSelectedSeatIds] = useState([])
  const [hold, setHold] = useState(null) // { holdToken, seatIds, expiresAt }
  const [holding, setHolding] = useState(false)
  const [remainingSeconds, setRemainingSeconds] = useState(0)
  const timerRef = useRef(null)

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

  useEffect(() => () => clearInterval(timerRef.current), [])

  function startCountdown(expiresAt) {
    clearInterval(timerRef.current)
    const tick = () => {
      const secs = Math.max(0, Math.round((new Date(expiresAt).getTime() - Date.now()) / 1000))
      setRemainingSeconds(secs)
      if (secs <= 0) {
        clearInterval(timerRef.current)
        setHold(null)
        toast.error('Your seat hold expired. Please reselect your seats.')
      }
    }
    tick()
    timerRef.current = setInterval(tick, 1000)
  }

  async function loadSeatState(showtime) {
    setSeatsLoading(true)
    setSeatsError('')
    try {
      const [seatList, statuses] = await Promise.all([
        theatersApi.listSeats(showtime.screenId),
        seatLocksApi.getSeatMap(showtime.id),
      ])
      setSeats(seatList)
      const map = {}
      statuses.forEach((s) => (map[s.seatId] = s.status))
      setSeatMap(map)
    } catch (err) {
      setSeatsError(extractErrorMessage(err))
    } finally {
      setSeatsLoading(false)
    }
  }

  function openBooking(showtime) {
    if (!isAuthenticated) {
      toast.info('Please log in to book seats.')
      navigate('/login', { state: { from: { pathname: `/movies/${id}` } } })
      return
    }
    setSelectedShowtime(showtime)
    setCustomerName(user?.fullName || '')
    setCustomerEmail(user?.email || '')
    setFormError('')
    setSelectedSeatIds([])
    setHold(null)
    setSeats(null)
    loadSeatState(showtime)
  }

  function closeModal() {
    clearInterval(timerRef.current)
    if (hold) seatLocksApi.releaseHold(hold.holdToken).catch(() => {})
    setSelectedShowtime(null)
    setHold(null)
  }

  function toggleSeat(seatId) {
    setSelectedSeatIds((ids) =>
      ids.includes(seatId) ? ids.filter((s) => s !== seatId) : [...ids, seatId],
    )
  }

  async function handleHoldSeats() {
    if (selectedSeatIds.length === 0) return
    setHolding(true)
    setSeatsError('')
    try {
      const result = await seatLocksApi.holdSeats(selectedShowtime.id, selectedSeatIds)
      setHold(result)
      startCountdown(result.expiresAt)
    } catch (err) {
      toast.error(extractErrorMessage(err))
      await loadSeatState(selectedShowtime) // someone else grabbed a seat -- refresh reality
      setSelectedSeatIds([])
    } finally {
      setHolding(false)
    }
  }

  function backToSeatSelection() {
    clearInterval(timerRef.current)
    if (hold) seatLocksApi.releaseHold(hold.holdToken).catch(() => {})
    setHold(null)
    setSelectedSeatIds([])
    loadSeatState(selectedShowtime)
  }

  async function submitBooking(e) {
    e.preventDefault()
    setFormError('')
    setSubmitting(true)
    try {
      const seatLabels = hold.seatIds.map((seatId) => seatLabel(seats, seatId))
      const booking = await bookingsApi.createBooking({
        customerName,
        customerEmail,
        showtimeId: selectedShowtime.id,
        seatIds: hold.seatIds,
        seatLabels,
        holdToken: hold.holdToken,
      })
      clearInterval(timerRef.current)
      toast.success(`Booking confirmed! Reference ${booking.bookingReference}`)
      setSelectedShowtime(null)
      setHold(null)
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

      <Modal open={!!selectedShowtime} onClose={closeModal} title={`Book "${movie.title}"`} wide>
        {selectedShowtime && (
          <div className="flex flex-col gap-4">
            <p className="text-sm text-slate-400">
              {new Date(selectedShowtime.startTime).toLocaleString()} ·{' '}
              {theaters[selectedShowtime.theaterId]?.name || `Theater #${selectedShowtime.theaterId}`}
            </p>

            {!hold ? (
              <SeatGrid
                seats={seats}
                seatMap={seatMap}
                loading={seatsLoading}
                error={seatsError}
                selectedSeatIds={selectedSeatIds}
                onToggle={toggleSeat}
                price={selectedShowtime.price}
                holding={holding}
                onContinue={handleHoldSeats}
                onCancel={closeModal}
              />
            ) : (
              <form onSubmit={submitBooking} className="flex flex-col gap-4">
                <div className="flex items-center justify-between rounded-lg border border-indigo-500/30 bg-indigo-500/5 px-3 py-2 text-sm">
                  <span className="text-slate-300">
                    Seats: <span className="font-medium text-white">{hold.seatIds.map((id) => seatLabel(seats, id)).join(', ')}</span>
                  </span>
                  <span className="font-medium text-amber-300">
                    Held for {Math.floor(remainingSeconds / 60)}:{String(remainingSeconds % 60).padStart(2, '0')}
                  </span>
                </div>
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
                <p className="text-sm text-slate-400">
                  Total:{' '}
                  <span className="font-semibold text-emerald-400">
                    ${(hold.seatIds.length * (selectedShowtime.price || 0)).toFixed(2)}
                  </span>
                </p>
                {formError && <p className="text-sm text-red-400">{formError}</p>}
                <div className="flex justify-end gap-2">
                  <Button type="button" variant="secondary" onClick={backToSeatSelection}>
                    Change seats
                  </Button>
                  <Button type="submit" disabled={submitting}>
                    {submitting ? 'Booking…' : 'Confirm booking'}
                  </Button>
                </div>
              </form>
            )}
          </div>
        )}
      </Modal>
    </div>
  )
}

function SeatGrid({ seats, seatMap, loading, error, selectedSeatIds, onToggle, price, holding, onContinue, onCancel }) {
  if (error) return <ErrorState message={error} />
  if (loading || !seats) {
    return <p className="py-8 text-center text-sm text-slate-500">Loading seat map…</p>
  }
  if (seats.length === 0) {
    return <p className="py-8 text-center text-sm text-slate-500">No seats configured for this screen yet.</p>
  }

  const rows = {}
  seats.forEach((s) => {
    if (!rows[s.rowLabel]) rows[s.rowLabel] = []
    rows[s.rowLabel].push(s)
  })
  const rowLabels = Object.keys(rows).sort()
  rowLabels.forEach((r) => rows[r].sort((a, b) => a.seatNumber - b.seatNumber))

  return (
    <div className="flex flex-col gap-4">
      <div className="flex flex-col gap-1.5 rounded-lg border border-slate-800 bg-slate-950 p-4">
        {rowLabels.map((rowLabel) => (
          <div key={rowLabel} className="flex items-center gap-1.5">
            <span className="w-5 shrink-0 text-xs text-slate-600">{rowLabel}</span>
            <div className="flex flex-wrap gap-1.5">
              {rows[rowLabel].map((seat) => {
                const status = seatMap[seat.id]
                const selected = selectedSeatIds.includes(seat.id)
                const disabled = !seat.active || (status && !selected)
                let tone = 'border-slate-700 text-slate-300 hover:border-indigo-400'
                if (!seat.active || status === 'BOOKED') tone = 'border-transparent bg-slate-800 text-slate-700 cursor-not-allowed'
                else if (status === 'HELD') tone = 'border-transparent bg-amber-500/20 text-amber-500 cursor-not-allowed'
                else if (selected) tone = 'border-indigo-500 bg-indigo-500 text-white'
                return (
                  <button
                    key={seat.id}
                    type="button"
                    title={`${seat.rowLabel}${seat.seatNumber} · ${seat.categoryCode}`}
                    disabled={disabled}
                    onClick={() => onToggle(seat.id)}
                    className={`flex h-7 w-7 items-center justify-center rounded text-[10px] font-medium transition-colors ${tone}`}
                  >
                    {seat.seatNumber}
                  </button>
                )
              })}
            </div>
          </div>
        ))}
      </div>

      <div className="flex flex-wrap items-center gap-4 text-xs text-slate-500">
        <Legend swatch="border border-slate-700" label="Available" />
        <Legend swatch="bg-indigo-500" label="Selected" />
        <Legend swatch="bg-amber-500/20" label="Held by another customer" />
        <Legend swatch="bg-slate-800" label="Booked / inactive" />
      </div>

      <div className="flex items-center justify-between border-t border-slate-800 pt-4">
        <p className="text-sm text-slate-400">
          {selectedSeatIds.length} seat(s) selected ·{' '}
          <span className="font-semibold text-emerald-400">
            ${(selectedSeatIds.length * (price || 0)).toFixed(2)}
          </span>
        </p>
        <div className="flex gap-2">
          <Button type="button" variant="secondary" onClick={onCancel}>
            Cancel
          </Button>
          <Button type="button" disabled={selectedSeatIds.length === 0 || holding} onClick={onContinue}>
            {holding ? 'Holding…' : 'Continue'}
          </Button>
        </div>
      </div>
    </div>
  )
}

function Legend({ swatch, label }) {
  return (
    <span className="flex items-center gap-1.5">
      <span className={`h-3 w-3 rounded ${swatch}`} />
      {label}
    </span>
  )
}
