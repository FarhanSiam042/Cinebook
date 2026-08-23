import { useEffect, useState } from 'react'
import * as theatersApi from '../api/theaters'
import { extractErrorMessage } from '../lib/api'
import { Card, EmptyState, ErrorState, PageLoader, SectionHeading, Spinner } from '../components/ui'

export default function Theaters() {
  const [theaters, setTheaters] = useState(null)
  const [error, setError] = useState('')
  const [expanded, setExpanded] = useState(null)
  const [screensByTheater, setScreensByTheater] = useState({})
  const [loadingScreens, setLoadingScreens] = useState(null)

  async function load() {
    setError('')
    try {
      setTheaters(await theatersApi.listTheaters())
    } catch (err) {
      setError(extractErrorMessage(err))
    }
  }

  useEffect(() => {
    load()
  }, [])

  async function toggle(theaterId) {
    if (expanded === theaterId) {
      setExpanded(null)
      return
    }
    setExpanded(theaterId)
    if (!screensByTheater[theaterId]) {
      setLoadingScreens(theaterId)
      try {
        const screens = await theatersApi.listScreens(theaterId)
        setScreensByTheater((prev) => ({ ...prev, [theaterId]: screens }))
      } catch {
        setScreensByTheater((prev) => ({ ...prev, [theaterId]: [] }))
      } finally {
        setLoadingScreens(null)
      }
    }
  }

  if (error) return <ErrorState message={error} onRetry={load} />
  if (!theaters) return <PageLoader label="Loading theaters…" />

  return (
    <div>
      <SectionHeading
        eyebrow="Locations"
        title="Theaters"
        description="Browse participating theaters and their screens."
      />
      {theaters.length === 0 && <EmptyState title="No theaters yet" />}
      <div className="flex flex-col gap-3">
        {theaters.map((t) => (
          <Card key={t.id} className="overflow-hidden">
            <button
              onClick={() => toggle(t.id)}
              className="flex w-full items-center justify-between px-5 py-4 text-left"
            >
              <div>
                <p className="font-semibold text-white">{t.name}</p>
                <p className="text-sm text-slate-400">
                  {t.city} · {t.address}
                </p>
              </div>
              <span className="text-slate-500">{expanded === t.id ? '▲' : '▼'}</span>
            </button>
            {expanded === t.id && (
              <div className="border-t border-slate-800 px-5 py-4">
                {loadingScreens === t.id ? (
                  <Spinner className="h-5 w-5 text-slate-500" />
                ) : screensByTheater[t.id]?.length ? (
                  <div className="grid gap-3 sm:grid-cols-2 lg:grid-cols-3">
                    {screensByTheater[t.id].map((s) => (
                      <div
                        key={s.id}
                        className="rounded-lg border border-slate-800 bg-slate-950 p-3 text-sm"
                      >
                        <p className="font-medium text-white">{s.name}</p>
                        <p className="text-slate-500">
                          {s.totalRows} rows × {s.seatsPerRow} seats
                        </p>
                      </div>
                    ))}
                  </div>
                ) : (
                  <p className="text-sm text-slate-500">No screens configured yet.</p>
                )}
              </div>
            )}
          </Card>
        ))}
      </div>
    </div>
  )
}
