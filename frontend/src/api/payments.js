import api from '../lib/api'

// payment-service — /api/payments/**
export const listPayments = () => api.get('/api/payments').then((r) => r.data)
export const getPayment = (id) => api.get(`/api/payments/${id}`).then((r) => r.data)
export const getPaymentByBookingReference = (reference) =>
  api.get(`/api/payments/booking-reference/${reference}`).then((r) => r.data)
