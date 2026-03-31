import axios from 'axios'

const API_BASE = import.meta.env.VITE_API_URL || '/api'

const api = axios.create({
  baseURL: API_BASE,
  headers: { 'Content-Type': 'application/json' },
})

api.interceptors.request.use((config) => {
  const token = localStorage.getItem('token')
  if (token) config.headers.Authorization = `Bearer ${token}`
  return config
})

api.interceptors.response.use(
  (r) => r,
  (err) => {
    if (err.response?.status === 401) {
      localStorage.removeItem('token')
      localStorage.removeItem('userId')
      localStorage.removeItem('role')
      window.location.href = '/login'
    }
    return Promise.reject(err)
  }
)

export default api

export const auth = {
  register: (data) => api.post('/auth/register', data),
  login: (data) => api.post('/auth/login', data),
}

export const businessApi = {
  create: (data) => api.post('/business', data),
  getById: (id) => api.get(`/business/${id}`),
  getMe: () => api.get('/business/me'),
  update: (id, data) => api.put(`/business/${id}`, data),
  delete: (id) => api.delete(`/business/${id}`),
}

export const servicesApi = {
  create: (data) => api.post('/services', data),
  getById: (id) => api.get(`/services/${id}`),
  getByBusinessId: (businessId, params) => api.get(`/services/business/${businessId}`, { params }),
  update: (id, data) => api.put(`/services/${id}`, data),
  delete: (id) => api.delete(`/services/${id}`),
}

export const slotsApi = {
  create: (data) => api.post('/slots', data),
  generateBulk: (data) => api.post('/slots/bulk', data),
  getByServiceId: (serviceId, params) => api.get(`/slots/service/${serviceId}`, { params }),
  update: (id, data) => api.put(`/slots/${id}`, data),
  delete: (id) => api.delete(`/slots/${id}`),
}

export const customersApi = {
  create: (data) => api.post('/customers', data),
  getById: (id) => api.get(`/customers/${id}`),
  getMe: () => api.get('/customers/me'),
  update: (id, data) => api.put(`/customers/${id}`, data),
  delete: (id) => api.delete(`/customers/${id}`),
}

export const bookingsApi = {
  create: (data) => api.post('/bookings', data),
  getByCustomerId: (customerId, params) => api.get(`/bookings/customer/${customerId}`, { params }),
  cancel: (id) => api.put(`/bookings/${id}/cancel`),
}

export const paymentsApi = {
  initiate: (data) => api.post('/payments/initiate', data),
  verify: (reference) => api.get('/payments/verify', { params: { reference } }),
  success: (id) => api.put(`/payments/${id}/success`),
  fail: (id) => api.put(`/payments/${id}/fail`),
}
