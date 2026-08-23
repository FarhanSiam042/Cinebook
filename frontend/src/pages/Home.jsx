import { useEffect, useMemo, useState } from 'react'
import { Link } from 'react-router-dom'
import * as moviesApi from '../api/movies'
import { extractErrorMessage } from '../lib/api'
import { Badge, Card, EmptyState, ErrorState, Input, PageLoader, Select, SectionHeading } from '../components/ui'

export default function Home() {
  const [movies, setMovies] = useState(null)
  const [error, setError] = useState('')
  const [title, setTitle] = useState('')
  const [genre, setGenre] = useState('')

  async function load() {
    setError('')
    try {
      const params = {}
      if (title.trim()) params.title = title.trim()
      if (genre.trim()) params.genre = genre.trim()
      const data = await moviesApi.listMovies(params)
      setMovies(data)
    } catch (err) {
      setError(extractErrorMessage(err))
    }
  }

  useEffect(() => {
    load()
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [])

  const genres = useMemo(() => {
    if (!movies) return []
    const set = new Set()
    movies.forEach((m) => (m.genres || []).forEach((g) => set.add(g)))
    return [...set].sort()
  }, [movies])

  function handleSubmit(e) {
    e.preventDefault()
    load()
  }

  return (
    <div>
      <SectionHeading
        eyebrow="Now showing"
        title="Browse movies"
        description="Search the catalog, filter by genre, and jump into a movie to see showtimes and book seats."
      />

      <form onSubmit={handleSubmit} className="mb-8 flex flex-wrap gap-3">
        <Input
          placeholder="Search by title…"
          value={title}
          onChange={(e) => setTitle(e.target.value)}
          className="w-56"
        />
        <Select value={genre} onChange={(e) => setGenre(e.target.value)} className="w-48">
          <option value="">All genres</option>
          {genres.map((g) => (
            <option key={g} value={g}>
              {g}
            </option>
          ))}
        </Select>
        <button
          type="submit"
          className="rounded-lg bg-indigo-500 px-4 py-2 text-sm font-medium text-white hover:bg-indigo-400"
        >
          Search
        </button>
      </form>

      {movies === null && !error && <PageLoader label="Loading movies…" />}
      {error && <ErrorState message={error} onRetry={load} />}
      {movies && movies.length === 0 && (
        <EmptyState
          title="No movies found"
          description="Try clearing your search or filters — or check back later."
        />
      )}

      {movies && movies.length > 0 && (
        <div className="grid grid-cols-2 gap-5 sm:grid-cols-3 lg:grid-cols-4">
          {movies.map((movie) => (
            <Link key={movie.id} to={`/movies/${movie.id}`}>
              <Card className="group h-full overflow-hidden transition-colors hover:border-indigo-500/60">
                <div className="aspect-[2/3] w-full overflow-hidden bg-slate-800">
                  {movie.posterUrl ? (
                    <img
                      src={movie.posterUrl}
                      alt={movie.title}
                      className="h-full w-full object-cover transition-transform group-hover:scale-105"
                      onError={(e) => {
                        e.currentTarget.style.display = 'none'
                      }}
                    />
                  ) : (
                    <div className="flex h-full w-full items-center justify-center text-4xl text-slate-700">
                      🎬
                    </div>
                  )}
                </div>
                <div className="p-3">
                  <p className="truncate font-semibold text-white">{movie.title}</p>
                  <div className="mt-1 flex flex-wrap items-center gap-1.5 text-xs text-slate-400">
                    {movie.language && <span>{movie.language}</span>}
                    {movie.durationMinutes && <span>· {movie.durationMinutes}m</span>}
                    {movie.rating != null && <span>· ★ {movie.rating}</span>}
                  </div>
                  {movie.genres?.length > 0 && (
                    <div className="mt-2 flex flex-wrap gap-1">
                      {movie.genres.slice(0, 2).map((g) => (
                        <Badge key={g}>{g}</Badge>
                      ))}
                    </div>
                  )}
                </div>
              </Card>
            </Link>
          ))}
        </div>
      )}
    </div>
  )
}
