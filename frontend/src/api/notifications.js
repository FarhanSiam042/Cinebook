import api from '../lib/api'

// notification-service — /api/notifications/**
export const listNotifications = () => api.get('/api/notifications').then((r) => r.data)
export const getNotification = (id) => api.get(`/api/notifications/${id}`).then((r) => r.data)
export const getNotificationByBookingReference = (reference) =>
  api.get(`/api/notifications/booking-reference/${reference}`).then((r) => r.data)
