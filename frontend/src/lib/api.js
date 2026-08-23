import axios from 'axios'

export const API_BASE_URL = import.meta.env.VITE_API_BASE_URL || 'http://localhost:8080'

const api = axios.create({
  baseURL: API_BASE_URL,
  headers: { 'Content-Type': 'application/json' },
})

api.interceptors.request.use((config) => {
  const token = localStorage.getItem('cinebook_token')
  if (token) {
    config.headers.Authorization = `Bearer ${token}`
  }
  return config
})

let onUnauthorized = null
export function setUnauthorizedHandler(fn) {
  onUnauthorized = fn
}

api.interceptors.response.use(
  (response) => response,
  (error) => {
    if (error.response?.status === 401 && onUnauthorized) {
      onUnauthorized()
    }
    return Promise.reject(error)
  }
)

export function extractErrorMessage(error) {
  const data = error?.response?.data
  if (!data) return error?.message || 'Something went wrong. Please try again.'
  if (typeof data === 'string') return data
  if (data.message) return data.message
  if (data.error) return data.error
  if (Array.isArray(data.errors)) return data.errors.join(', ')
  return 'Something went wrong. Please try again.'
}

export default api
