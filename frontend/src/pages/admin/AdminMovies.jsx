import { useEffect, useState } from 'react'
import * as moviesApi from '../../api/movies'
import * as theatersApi from '../../api/theaters'
import { extractErrorMessage } from '../../lib/api'
import { useToast } from '../../context/ToastContext'
import {
  Badge,
  Button,
  Card,
  EmptyState,
  ErrorState,
  Input,
  Modal,
  PageLoader,
  SectionHeading,
  Select,
  Textarea,
} from '../../components/ui'

const emptyMovieForm = {
  title: '',
  description: '',
  durationMinutes: '',
  language: '',
  releaseDate: '',
  rating: '',
  posterUrl: '',
  genres: '',
}

export default function AdminMovies() {
  const toast = useToast()
  const [movies, setMovies] = useState(null)
  const [error, setError] = useState('')

  const [editing, setEditing] = useState(null) // null | 'new' | movie object
  const [form, setForm] = useState(emptyMovieForm)
  const [formError, setFormError] = useState('')
  const [saving, setSaving] = useState(false)

  const [managing, setManaging] = useState(null) // movie being managed (showtimes/cast)

  async function load() {
    setError('')
    try {
      setMovies(await moviesApi.listMovies())
    } catch (err) {
      setError(extractErrorMessage(err))
    }
  }

  useEffect(() => {
    load()
  }, [])

  function openNew() {
    setForm(emptyMovieForm)
    setFormError('')
    setEditing('new')
  }

  function openEdit(movie) {
    setForm({
      title: movie.title || '',
      description: movie.description || '',
      durationMinutes: movie.durationMinutes || '',
      language: movie.language || '',
      releaseDate: movie.releaseDate || '',
      rating: movie.rating ?? '',
      posterUrl: movie.posterUrl || '',
      genres: (movie.genres || []).join(', '),
    })
    setFormError('')
    setEditing(movie)
  }

  async function submitMovie(e) {
    e.preventDefault()
    setFormError('')
    setSaving(true)
    try {
      const payload = {
        title: form.title,
        description: form.description || null,
        durationMinutes: form.durationMinutes ? Number(form.durationMinutes) : null,
        language: form.language || null,
        releaseDate: form.releaseDate || null,
        rating: form.rating !== '' ? Number(form.rating) : null,
        posterUrl: form.posterUrl || null,
        genres: form.genres
          .split(',')
          .map((g) => g.trim())
          .filter(Boolean),
      }
      if (editing === 'new') {
        const created = await moviesApi.createMovie(payload)
        toast.success('Movie created — now add a showtime and price below.')
        setEditing(null)
        setManaging(created)
      } else {
        await moviesApi.updateMovie(editing.id, payload)
        toast.success('Movie updated.')
        setEditing(null)
      }
      load()
    } catch (err) {
      setFormError(extractErrorMessage(err))
    } finally {
      setSaving(false)
    }
  }

  async function handleDelete(movie) {
    if (!confirm(`Delete "${movie.title}"? This cannot be undone.`)) return
    try {
      await moviesApi.deleteMovie(movie.id)
      toast.success('Movie deleted.')
      load()
    } catch (err) {
      toast.error(extractErrorMessage(err))
    }
  }

  if (error) return <ErrorState message={error} onRetry={load} />
  if (!movies) return <PageLoader label="Loading movies…" />

  return (
    <div>
      <SectionHeading
        title="Movies & showtimes"
        description="Create and edit the catalog. Manage showtimes and cast per movie."
        action={<Button onClick={openNew}>+ New movie</Button>}
      />

      {movies.length === 0 && <EmptyState title="No movies yet" />}

      <div className="flex flex-col gap-3">
        {movies.map((m) => (
          <Card key={m.id} className="flex flex-wrap items-center justify-between gap-3 p-4">
            <div className="min-w-0">
              <p className="font-semibold text-white">{m.title}</p>
              <p className="text-sm text-slate-400">
                {m.language} {m.durationMinutes ? `· ${m.durationMinutes}m` : ''}{' '}
                {m.releaseDate ? `· ${m.releaseDate}` : ''}
              </p>
              <div className="mt-1 flex flex-wrap gap-1">
                {m.genres?.map((g) => (
                  <Badge key={g}>{g}</Badge>
                ))}
              </div>
            </div>
            <div className="flex flex-wrap gap-2">
              <Button size="sm" variant="outline" onClick={() => setManaging(m)}>
                Showtimes & cast
              </Button>
              <Button size="sm" variant="secondary" onClick={() => openEdit(m)}>
                Edit
              </Button>
              <Button size="sm" variant="danger" onClick={() => handleDelete(m)}>
                Delete
              </Button>
            </div>
          </Card>
        ))}
      </div>

      <Modal
        open={!!editing}
        onClose={() => setEditing(null)}
        title={editing === 'new' ? 'New movie' : `Edit "${editing?.title}"`}
        wide
      >
        <form onSubmit={submitMovie} className="grid gap-4 sm:grid-cols-2">
          <Input
            label="Title"
            required
            className="sm:col-span-2"
            value={form.title}
            onChange={(e) => setForm((f) => ({ ...f, title: e.target.value }))}
          />
          <Textarea
            label="Description"
            className="sm:col-span-2"
            rows={3}
            value={form.description}
            onChange={(e) => setForm((f) => ({ ...f, description: e.target.value }))}
          />
          <Input
            label="Duration (minutes)"
            type="number"
            min={1}
            value={form.durationMinutes}
            onChange={(e) => setForm((f) => ({ ...f, durationMinutes: e.target.value }))}
          />
          <Input
            label="Language"
            value={form.language}
            onChange={(e) => setForm((f) => ({ ...f, language: e.target.value }))}
          />
          <Input
            label="Release date"
            type="date"
            value={form.releaseDate}
            onChange={(e) => setForm((f) => ({ ...f, releaseDate: e.target.value }))}
          />
          <Input
            label="Rating"
            type="number"
            step="0.1"
            min={0}
            max={10}
            value={form.rating}
            onChange={(e) => setForm((f) => ({ ...f, rating: e.target.value }))}
          />
          <Input
            label="Poster URL"
            className="sm:col-span-2"
            value={form.posterUrl}
            onChange={(e) => setForm((f) => ({ ...f, posterUrl: e.target.value }))}
          />
          <Input
            label="Genres (comma separated)"
            className="sm:col-span-2"
            placeholder="Action, Sci-Fi"
            value={form.genres}
            onChange={(e) => setForm((f) => ({ ...f, genres: e.target.value }))}
          />
          {editing === 'new' && (
            <p className="text-xs text-slate-500 sm:col-span-2">
              Showtimes (theater, screen, time, price) and cast are added on the next step, after
              the movie is created.
            </p>
          )}
          {formError && <p className="text-sm text-red-400 sm:col-span-2">{formError}</p>}
          <div className="flex justify-end gap-2 sm:col-span-2">
            <Button type="button" variant="secondary" onClick={() => setEditing(null)}>
              Cancel
            </Button>
            <Button type="submit" disabled={saving}>
              {saving ? 'Saving…' : 'Save'}
            </Button>
          </div>
        </form>
      </Modal>

      {managing && (
        <ManageShowtimesAndCast
          movie={managing}
          onClose={() => setManaging(null)}
          onChanged={load}
        />
      )}
    </div>
  )
}

function ManageShowtimesAndCast({ movie, onClose, onChanged }) {
  const toast = useToast()
  const [showtimes, setShowtimes] = useState(movie.showtimes || [])
  const [cast, setCast] = useState(movie.cast || [])
  const [theaters, setTheaters] = useState([])
  const [screens, setScreens] = useState([])

  const [stForm, setStForm] = useState({ theaterId: '', screenId: '', startTime: '', price: '' })
  const [stError, setStError] = useState('')
  const [stSaving, setStSaving] = useState(false)

  const [castForm, setCastForm] = useState({ actorName: '', roleInMovie: '' })
  const [castError, setCastError] = useState('')
  const [castSaving, setCastSaving] = useState(false)

  useEffect(() => {
    theatersApi.listTheaters().then(setTheaters).catch(() => setTheaters([]))
  }, [])

  useEffect(() => {
    if (!stForm.theaterId) {
      setScreens([])
      return
    }
    theatersApi
      .listScreens(stForm.theaterId)
      .then(setScreens)
      .catch(() => setScreens([]))
  }, [stForm.theaterId])

  async function refreshMovie() {
    const fresh = await moviesApi.getMovie(movie.id)
    setShowtimes(fresh.showtimes || [])
    setCast(fresh.cast || [])
    onChanged()
  }

  async function submitShowtime(e) {
    e.preventDefault()
    setStError('')
    setStSaving(true)
    try {
      await moviesApi.addShowtime(movie.id, {
        theaterId: Number(stForm.theaterId),
        screenId: Number(stForm.screenId),
        startTime: stForm.startTime,
        price: Number(stForm.price),
      })
      toast.success('Showtime added.')
      setStForm({ theaterId: '', screenId: '', startTime: '', price: '' })
      await refreshMovie()
    } catch (err) {
      setStError(extractErrorMessage(err))
    } finally {
      setStSaving(false)
    }
  }

  async function submitCast(e) {
    e.preventDefault()
    setCastError('')
    setCastSaving(true)
    try {
      await moviesApi.addCastMember(movie.id, castForm)
      toast.success('Cast member added.')
      setCastForm({ actorName: '', roleInMovie: '' })
      await refreshMovie()
    } catch (err) {
      setCastError(extractErrorMessage(err))
    } finally {
      setCastSaving(false)
    }
  }

  return (
    <Modal open onClose={onClose} title={`Showtimes & cast — ${movie.title}`} wide>
      <div className="grid gap-8 sm:grid-cols-2">
        <div>
          <h4 className="mb-2 text-sm font-semibold text-slate-300">Showtimes</h4>
          <div className="mb-4 flex flex-col gap-2">
            {showtimes.length === 0 && <p className="text-sm text-slate-500">None yet.</p>}
            {showtimes.map((st) => (
              <div
                key={st.id}
                className="rounded-lg border border-slate-800 bg-slate-950 p-2.5 text-sm"
              >
                <p className="text-slate-200">{new Date(st.startTime).toLocaleString()}</p>
                <p className="text-xs text-slate-500">
                  Theater #{st.theaterId} · Screen #{st.screenId} · ${st.price}
                </p>
              </div>
            ))}
          </div>
          <form onSubmit={submitShowtime} className="flex flex-col gap-2 border-t border-slate-800 pt-4">
            <Select
              label="Theater"
              required
              value={stForm.theaterId}
              onChange={(e) => setStForm((f) => ({ ...f, theaterId: e.target.value, screenId: '' }))}
            >
              <option value="">Select theater</option>
              {theaters.map((t) => (
                <option key={t.id} value={t.id}>
                  {t.name}
                </option>
              ))}
            </Select>
            <Select
              label="Screen"
              required
              value={stForm.screenId}
              onChange={(e) => setStForm((f) => ({ ...f, screenId: e.target.value }))}
              disabled={!stForm.theaterId}
            >
              <option value="">Select screen</option>
              {screens.map((s) => (
                <option key={s.id} value={s.id}>
                  {s.name}
                </option>
              ))}
            </Select>
            <Input
              label="Start time"
              type="datetime-local"
              required
              value={stForm.startTime}
              onChange={(e) => setStForm((f) => ({ ...f, startTime: e.target.value }))}
            />
            <Input
              label="Price"
              type="number"
              step="0.01"
              min={0}
              required
              value={stForm.price}
              onChange={(e) => setStForm((f) => ({ ...f, price: e.target.value }))}
            />
            {stError && <p className="text-sm text-red-400">{stError}</p>}
            <Button type="submit" size="sm" disabled={stSaving} className="self-start">
              {stSaving ? 'Adding…' : 'Add showtime'}
            </Button>
          </form>
        </div>

        <div>
          <h4 className="mb-2 text-sm font-semibold text-slate-300">Cast</h4>
          <div className="mb-4 flex flex-wrap gap-2">
            {cast.length === 0 && <p className="text-sm text-slate-500">None yet.</p>}
            {cast.map((c) => (
              <span
                key={c.id}
                className="rounded-full border border-slate-800 bg-slate-950 px-3 py-1 text-sm text-slate-300"
              >
                {c.actorName}
                {c.roleInMovie ? ` as ${c.roleInMovie}` : ''}
              </span>
            ))}
          </div>
          <form onSubmit={submitCast} className="flex flex-col gap-2 border-t border-slate-800 pt-4">
            <Input
              label="Actor name"
              required
              value={castForm.actorName}
              onChange={(e) => setCastForm((f) => ({ ...f, actorName: e.target.value }))}
            />
            <Input
              label="Role in movie"
              value={castForm.roleInMovie}
              onChange={(e) => setCastForm((f) => ({ ...f, roleInMovie: e.target.value }))}
            />
            {castError && <p className="text-sm text-red-400">{castError}</p>}
            <Button type="submit" size="sm" disabled={castSaving} className="self-start">
              {castSaving ? 'Adding…' : 'Add cast member'}
            </Button>
          </form>
        </div>
      </div>
    </Modal>
  )
}
