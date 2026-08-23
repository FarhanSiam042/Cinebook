import api from '../lib/api'

// booking-service — /api/bookings/**
export const createBooking = (payload) => api.post('/api/bookings', payload).then((r) => r.data)
export const listBookings = () => api.get('/api/bookings').then((r) => r.data)
export const getBooking = (id) => api.get(`/api/bookings/${id}`).then((r) => r.data)
export const getBookingByReference = (reference) =>
  api.get(`/api/bookings/reference/${reference}`).then((r) => r.data)
