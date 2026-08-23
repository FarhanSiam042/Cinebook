import api from '../lib/api'

// theater-service — /api/theaters/**
export const listTheaters = () => api.get('/api/theaters').then((r) => r.data)
export const getTheater = (id) => api.get(`/api/theaters/${id}`).then((r) => r.data)
export const createTheater = (payload) => api.post('/api/theaters', payload).then((r) => r.data)
export const updateTheater = (id, payload) =>
  api.put(`/api/theaters/${id}`, payload).then((r) => r.data)
export const deleteTheater = (id) => api.delete(`/api/theaters/${id}`).then((r) => r.data)

export const listScreens = (theaterId) =>
  api.get(`/api/theaters/${theaterId}/screens`).then((r) => r.data)
export const createScreen = (theaterId, payload) =>
  api.post(`/api/theaters/${theaterId}/screens`, payload).then((r) => r.data)
export const getScreen = (screenId) =>
  api.get(`/api/theaters/screens/${screenId}`).then((r) => r.data)
export const updateScreen = (screenId, payload) =>
  api.put(`/api/theaters/screens/${screenId}`, payload).then((r) => r.data)
export const deleteScreen = (screenId) =>
  api.delete(`/api/theaters/screens/${screenId}`).then((r) => r.data)

export const generateSeats = (screenId, categoryCode) =>
  api
    .post(`/api/theaters/screens/${screenId}/seats/generate`, { categoryCode })
    .then((r) => r.data)
export const listSeats = (screenId) =>
  api.get(`/api/theaters/screens/${screenId}/seats`).then((r) => r.data)
export const getSeat = (seatId) => api.get(`/api/theaters/seats/${seatId}`).then((r) => r.data)

export const listSeatCategories = () =>
  api.get('/api/theaters/seat-categories').then((r) => r.data)
export const getSeatCategory = (id) =>
  api.get(`/api/theaters/seat-categories/${id}`).then((r) => r.data)
export const createSeatCategory = (payload) =>
  api.post('/api/theaters/seat-categories', payload).then((r) => r.data)
export const updateSeatCategory = (id, payload) =>
  api.put(`/api/theaters/seat-categories/${id}`, payload).then((r) => r.data)
export const deleteSeatCategory = (id) =>
  api.delete(`/api/theaters/seat-categories/${id}`).then((r) => r.data)
