import api from '../lib/api'

// user-service — /api/users/**
export const getMe = () => api.get('/api/users/me').then((r) => r.data)

export const updateMe = (fullName, email) =>
  api.put('/api/users/me', { fullName, email }).then((r) => r.data)

export const changePassword = (currentPassword, newPassword) =>
  api.put('/api/users/me/password', { currentPassword, newPassword }).then((r) => r.data)

export const listUsers = () => api.get('/api/users').then((r) => r.data)

export const adminCreateUser = (payload) =>
  api.post('/api/users', payload).then((r) => r.data)
