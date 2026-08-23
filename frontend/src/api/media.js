import api from '../lib/api'

// media-service — /api/media/**
export const uploadImage = (file) => {
  const formData = new FormData()
  formData.append('file', file)
  // let the browser set multipart/form-data with the correct boundary itself
  return api
    .post('/api/media/images', formData, { headers: { 'Content-Type': undefined } })
    .then((r) => r.data)
}
