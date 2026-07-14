import { http } from './http.js'

// bien-service (/api/biens). Upload Cloudinary → { secureUrl, publicId }.
export const biensApi = {
  list: () => http.get('/api/biens').then((r) => r.data),
  disponibles: () => http.get('/api/biens/disponibles').then((r) => r.data),
  get: (id) => http.get(`/api/biens/${id}`).then((r) => r.data),
  byCategorie: (cat) => http.get(`/api/biens/categorie/${cat}`).then((r) => r.data),
  mesBiens: () => http.get('/api/biens/mes-biens').then((r) => r.data),
  recherche: (params) => http.get('/api/biens/recherche', { params }).then((r) => r.data),

  prixVisite: (cat) => http.get(`/api/biens/categorie/${cat}/prix-visite`).then((r) => r.data),

  create: (payload) => http.post('/api/biens', payload).then((r) => r.data),
  update: (id, payload) => http.put(`/api/biens/${id}`, payload).then((r) => r.data),
  remove: (id) => http.delete(`/api/biens/${id}`).then((r) => r.data),
  updateStatut: (id, statut) => http.put(`/api/biens/${id}/statut`, { statut }).then((r) => r.data),

  // Upload d'images (multipart). Champ 'file' / 'files'. Réponse { secureUrl, publicId }.
  // Content-Type laissé au navigateur → il ajoute la boundary multipart correcte.
  uploadImage: (file) => {
    const fd = new FormData()
    fd.append('file', file)
    return http.post('/api/biens/images/upload', fd, { headers: { 'Content-Type': undefined } }).then((r) => r.data)
  },
  uploadImages: (files) => {
    const fd = new FormData()
    Array.from(files).forEach((f) => fd.append('files', f))
    return http.post('/api/biens/images/upload-multiple', fd, { headers: { 'Content-Type': undefined } }).then((r) => r.data)
  },

  // Admin : tarif de visite par catégorie
  adminSetPrixVisite: (cat, prixVisite) => http.put(`/api/biens/admin/prix-visite/${cat}`, { prixVisite }).then((r) => r.data),
}
