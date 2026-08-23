import api from '../lib/api'

// movie-service — /api/movies/**
export const listMovies = (params = {}) =>
  api.get('/api/movies', { params }).then((r) => r.data)

export const getMovie = (id) => api.get(`/api/movies/${id}`).then((r) => r.data)

export const createMovie = (payload) => api.post('/api/movies', payload).then((r) => r.data)

export const updateMovie = (id, payload) =>
  api.put(`/api/movies/${id}`, payload).then((r) => r.data)

export const deleteMovie = (id) => api.delete(`/api/movies/${id}`).then((r) => r.data)

export const listShowtimes = (movieId) =>
  api.get(`/api/movies/${movieId}/showtimes`).then((r) => r.data)

export const addShowtime = (movieId, payload) =>
  api.post(`/api/movies/${movieId}/showtimes`, payload).then((r) => r.data)

export const getShowtime = (showtimeId) =>
  api.get(`/api/movies/showtimes/${showtimeId}`).then((r) => r.data)

export const addCastMember = (movieId, payload) =>
  api.post(`/api/movies/${movieId}/cast`, payload).then((r) => r.data)
