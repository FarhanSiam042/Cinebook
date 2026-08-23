import api from '../lib/api'

// seat-lock-service — /api/seat-locks/**
export const getSeatMap = (showtimeId) =>
  api.get(`/api/seat-locks/showtimes/${showtimeId}`).then((r) => r.data)

export const holdSeats = (showtimeId, seatIds) =>
  api.post('/api/seat-locks', { showtimeId, seatIds }).then((r) => r.data)

export const releaseHold = (holdToken) => api.delete(`/api/seat-locks/${holdToken}`)
