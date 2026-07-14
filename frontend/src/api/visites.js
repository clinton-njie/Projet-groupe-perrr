import { http } from './http.js'

// service-visite. Création = paiement Mobile Money (escrow). Annulation verrouillée < 24h.
export const visitesApi = {
  create: (payload) => http.post('/api/visites', payload).then((r) => r.data),
  get: (id) => http.get(`/api/visites/${id}`).then((r) => r.data),
  byUser: (email) => http.get(`/api/visites/user/${encodeURIComponent(email)}`).then((r) => r.data),
  byAgent: (email) => http.get(`/api/visites/agent/${encodeURIComponent(email)}`).then((r) => r.data),
  availableSlots: (bienId, date) => http.get('/visits/available-slots', { params: { bienId, date } }).then((r) => r.data),
  confirmer: (id) => http.put(`/api/visites/${id}/confirmer`).then((r) => r.data),
  terminer: (id) => http.put(`/api/visites/${id}/terminer`).then((r) => r.data),
  annuler: (id) => http.put(`/api/visites/${id}/annuler`).then((r) => r.data),
  annulerAgent: (id) => http.put(`/api/visites/${id}/annuler-agent`).then((r) => r.data),
  nonPresente: (id) => http.put(`/api/visites/${id}/non-presente`).then((r) => r.data),
}
