import { Navigate, Outlet, useLocation } from 'react-router-dom'
import { useAuth } from '../context/AuthContext'
import { PageLoader } from './ui'

export function RequireAuth() {
  const { isAuthenticated, ready } = useAuth()
  const location = useLocation()

  if (!ready) return <PageLoader />
  if (!isAuthenticated) return <Navigate to="/login" state={{ from: location }} replace />
  return <Outlet />
}

export function RequireAdmin() {
  const { isAuthenticated, isAdmin, ready } = useAuth()
  const location = useLocation()

  if (!ready) return <PageLoader />
  if (!isAuthenticated) return <Navigate to="/login" state={{ from: location }} replace />
  if (!isAdmin) return <Navigate to="/" replace />
  return <Outlet />
}
