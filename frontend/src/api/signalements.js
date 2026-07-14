import { http } from './http.js'

// service-signalements (/signalements). Objets = motifs gérés par l'admin.
export const signalementsApi = {
  objets: (type = 'AGENT') => http.get('/signalements/objets', { params: { type } }).then((r) => r.data),
  create: (payload) => http.post('/signalements', payload).then((r) => r.data),
  get: (id) => http.get(`/signalements/${id}`).then((r) => r.data),
  // Admin : gestion des objets de signalement
  createObjet: (libelle, type) => http.post('/signalements/objets', { libelle, type }).then((r) => r.data),
  updateStatut: (id, statut) => http.put(`/signalements/${id}/statut`, { statut }).then((r) => r.data),
}
