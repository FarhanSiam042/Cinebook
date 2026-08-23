import api from '../lib/api'

// user-service — /api/auth/**
export const login = (username, password) =>
  api.post('/api/auth/login', { username, password }).then((r) => r.data)

export const register = (fullName, email, username, password) =>
  api.post('/api/auth/register', { fullName, email, username, password }).then((r) => r.data)
