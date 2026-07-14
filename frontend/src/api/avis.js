import { http } from './http.js'

// ms-avis (/api/avis). Note transaction (6 critères 1-5) ou note plateforme.
export const avisApi = {
  agent: (agentId) => http.get(`/api/avis/agent/${agentId}`).then((r) => r.data),
  bien: (bienId) => http.get(`/api/avis/bien/${bienId}`).then((r) => r.data),
  plateforme: () => http.get('/api/avis/plateforme').then((r) => r.data),
  // Opportunités de notation ouvertes pour un client (après transaction terminée)
  opportunites: (clientId) => http.get(`/api/avis/opportunites/${clientId}`).then((r) => r.data),
  noterTransaction: (payload) => http.post('/api/avis/transaction', payload).then((r) => r.data),
  noterPlateforme: (notePlateforme, commentaire) =>
    http.post('/api/avis/plateforme', { notePlateforme, commentaire }).then((r) => r.data),
}
