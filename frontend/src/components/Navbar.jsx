import { useState } from 'react'
import { Link, NavLink, useNavigate } from 'react-router-dom'
import { useAuth } from '../context/AuthContext'
import { Button } from './ui'

const linkClass = ({ isActive }) =>
  `rounded-md px-3 py-2 text-sm font-medium transition-colors ${
    isActive ? 'bg-slate-800 text-white' : 'text-slate-300 hover:bg-slate-800/60 hover:text-white'
  }`

export default function Navbar() {
  const { isAuthenticated, isAdmin, user, logout } = useAuth()
  const navigate = useNavigate()
  const [open, setOpen] = useState(false)

  function handleLogout() {
    logout()
    setOpen(false)
    navigate('/')
  }

  const links = (
    <>
      <NavLink to="/" end className={linkClass} onClick={() => setOpen(false)}>
        Movies
      </NavLink>
      <NavLink to="/theaters" className={linkClass} onClick={() => setOpen(false)}>
        Theaters
      </NavLink>
      {isAuthenticated && (
        <NavLink to="/bookings" className={linkClass} onClick={() => setOpen(false)}>
          My Bookings
        </NavLink>
      )}
      {isAdmin && (
        <NavLink to="/admin" className={linkClass} onClick={() => setOpen(false)}>
          Admin
        </NavLink>
      )}
    </>
  )

  return (
    <header className="sticky top-0 z-40 border-b border-slate-800 bg-slate-950/90 backdrop-blur">
      <div className="mx-auto flex h-16 max-w-7xl items-center justify-between px-4 sm:px-6">
        <div className="flex items-center gap-6">
          <Link to="/" className="flex items-center gap-2 text-lg font-bold text-white">
            <span className="rounded-lg bg-indigo-500 px-2 py-1 text-sm">CB</span>
            CineBook
          </Link>
          <nav className="hidden items-center gap-1 md:flex">{links}</nav>
        </div>

        <div className="hidden items-center gap-3 md:flex">
          {isAuthenticated ? (
            <>
              <Link
                to="/profile"
                className="text-sm text-slate-300 hover:text-white"
                title={user?.email}
              >
                {user?.fullName || user?.username}
                {isAdmin && (
                  <span className="ml-2 rounded-full bg-indigo-500/20 px-2 py-0.5 text-xs text-indigo-300">
                    ADMIN
                  </span>
                )}
              </Link>
              <Button variant="secondary" size="sm" onClick={handleLogout}>
                Log out
              </Button>
            </>
          ) : (
            <>
              <Button as={Link} to="/login" variant="ghost" size="sm">
                Log in
              </Button>
              <Button as={Link} to="/register" variant="primary" size="sm">
                Sign up
              </Button>
            </>
          )}
        </div>

        <button
          className="rounded-md p-2 text-slate-300 hover:bg-slate-800 md:hidden"
          onClick={() => setOpen((v) => !v)}
          aria-label="Toggle menu"
        >
          {open ? '✕' : '☰'}
        </button>
      </div>

      {open && (
        <div className="border-t border-slate-800 px-4 pb-4 md:hidden">
          <nav className="flex flex-col gap-1 pt-2">{links}</nav>
          <div className="mt-3 flex flex-col gap-2">
            {isAuthenticated ? (
              <>
                <Link
                  to="/profile"
                  onClick={() => setOpen(false)}
                  className="text-sm text-slate-300"
                >
                  Signed in as {user?.fullName || user?.username}
                  {isAdmin ? ' (Admin)' : ''}
                </Link>
                <Button variant="secondary" size="sm" onClick={handleLogout}>
                  Log out
                </Button>
              </>
            ) : (
              <>
                <Button as={Link} to="/login" variant="secondary" size="sm" onClick={() => setOpen(false)}>
                  Log in
                </Button>
                <Button as={Link} to="/register" variant="primary" size="sm" onClick={() => setOpen(false)}>
                  Sign up
                </Button>
              </>
            )}
          </div>
        </div>
      )}
    </header>
  )
}
