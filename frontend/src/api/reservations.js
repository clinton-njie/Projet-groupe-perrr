import { http } from './http.js'

// reservation-service. Montant = prix mensuel × durée (calculé côté back).
export const reservationsApi = {
  create: (payload) => http.post('/api/reservations', payload).then((r) => r.data),
  get: (id) => http.get(`/api/reservations/${id}`).then((r) => r.data),
  byUser: (userId) => http.get(`/api/reservations/user/${userId}`).then((r) => r.data),
  // Réservations reçues par l'agent courant sur ses biens (JWT agent)
  agent: () => http.get('/api/reservations/agent').then((r) => r.data),
  annuler: (id) => http.put(`/api/reservations/${id}/annuler`).then((r) => r.data),
  confirmer: (id) => http.put(`/api/reservations/${id}/confirmer`).then((r) => r.data),
}
