import { Link } from 'react-router-dom'
import { MICROSERVICES } from '../lib/microservices'

export default function Footer() {
  return (
    <footer className="border-t border-slate-800 bg-slate-950">
      <div className="mx-auto max-w-7xl px-4 py-10 sm:px-6">
        <div className="grid gap-8 md:grid-cols-3">
          <div>
            <p className="flex items-center gap-2 text-lg font-bold text-white">
              <span className="rounded-lg bg-indigo-500 px-2 py-1 text-sm">CB</span>
              CineBook
            </p>
            <p className="mt-2 max-w-xs text-sm text-slate-500">
              A microservices-based movie ticket booking platform. This UI talks to every
              backend service through a single API gateway.
            </p>
          </div>

          <div>
            <p className="mb-3 text-sm font-semibold text-slate-300">Explore</p>
            <ul className="space-y-2 text-sm text-slate-500">
              <li><Link className="hover:text-white" to="/">Movies</Link></li>
              <li><Link className="hover:text-white" to="/theaters">Theaters</Link></li>
              <li><Link className="hover:text-white" to="/login">Admin login</Link></li>
            </ul>
          </div>

          <div>
            <p className="mb-3 text-sm font-semibold text-slate-300">
              Backend services ({MICROSERVICES.length})
            </p>
            <ul className="grid grid-cols-2 gap-x-4 gap-y-1.5 text-sm text-slate-500">
              {MICROSERVICES.map((s) => (
                <li key={s.key} className="truncate" title={`${s.name} — port ${s.port}`}>
                  {s.name}{' '}
                  <span className="text-slate-700">:{s.port}</span>
                </li>
              ))}
            </ul>
          </div>
        </div>

        <p className="mt-8 border-t border-slate-900 pt-6 text-xs text-slate-600">
          CineBook — built on 7 independent Spring Boot microservices behind an API gateway.
        </p>
      </div>
    </footer>
  )
}
