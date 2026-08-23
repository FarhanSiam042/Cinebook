import { BrowserRouter, Route, Routes } from 'react-router-dom'
import { AuthProvider } from './context/AuthContext'
import { ToastProvider } from './context/ToastContext'
import Layout from './components/Layout'
import { RequireAdmin, RequireAuth } from './components/RouteGuards'

import Home from './pages/Home'
import MovieDetail from './pages/MovieDetail'
import Theaters from './pages/Theaters'
import Login from './pages/Login'
import Register from './pages/Register'
import Profile from './pages/Profile'
import MyBookings from './pages/MyBookings'
import BookingDetail from './pages/BookingDetail'
import NotFound from './pages/NotFound'

import AdminLayout from './pages/admin/AdminLayout'
import AdminOverview from './pages/admin/AdminOverview'
import AdminMovies from './pages/admin/AdminMovies'
import AdminTheaters from './pages/admin/AdminTheaters'
import AdminUsers from './pages/admin/AdminUsers'
import AdminBookings from './pages/admin/AdminBookings'
import AdminPayments from './pages/admin/AdminPayments'
import AdminNotifications from './pages/admin/AdminNotifications'

export default function App() {
  return (
    <BrowserRouter>
      <AuthProvider>
        <ToastProvider>
          <Routes>
            <Route element={<Layout />}>
              <Route path="/" element={<Home />} />
              <Route path="/movies/:id" element={<MovieDetail />} />
              <Route path="/theaters" element={<Theaters />} />
              <Route path="/login" element={<Login />} />
              <Route path="/register" element={<Register />} />

              <Route element={<RequireAuth />}>
                <Route path="/profile" element={<Profile />} />
                <Route path="/bookings" element={<MyBookings />} />
                <Route path="/bookings/:reference" element={<BookingDetail />} />
              </Route>

              <Route element={<RequireAdmin />}>
                <Route path="/admin" element={<AdminLayout />}>
                  <Route index element={<AdminOverview />} />
                  <Route path="movies" element={<AdminMovies />} />
                  <Route path="theaters" element={<AdminTheaters />} />
                  <Route path="users" element={<AdminUsers />} />
                  <Route path="bookings" element={<AdminBookings />} />
                  <Route path="payments" element={<AdminPayments />} />
                  <Route path="notifications" element={<AdminNotifications />} />
                </Route>
              </Route>

              <Route path="*" element={<NotFound />} />
            </Route>
          </Routes>
        </ToastProvider>
      </AuthProvider>
    </BrowserRouter>
  )
}
