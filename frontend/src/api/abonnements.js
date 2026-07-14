import { http } from './http.js'

// abonnement-service. Paiement asynchrone → EN_ATTENTE_PAIEMENT puis ACTIF.
export const abonnementsApi = {
  souscrire: (type, modePaiement = 'MOBILE_MONEY') => http.post('/api/abonnements', { type, modePaiement }).then((r) => r.data),
  renouveler: (type, modePaiement = 'MOBILE_MONEY') => http.post('/api/abonnements/renouveler', { type, modePaiement }).then((r) => r.data),
  monStatut: () => http.get('/api/abonnements/mon-statut').then((r) => r.data),
  monActif: () => http.get('/api/abonnements/mon-abonnement-actif').then((r) => r.data),
  monHistorique: () => http.get('/api/abonnements/mon-historique').then((r) => r.data),
  reessayerPaiement: (id) => http.post(`/api/abonnements/${id}/reessayer-paiement`).then((r) => r.data),
  // Confirme le paiement (simulation : pas de vraie passerelle Mobile Money côté back).
  // Publie un PaiementTraiteEvent → le consumer active l'abonnement.
  simulerPaiement: ({ requestId, statut = 'SUCCESS', modePaiement = 'MOBILE_MONEY', reference }) =>
    http.post('/api/interne/abonnements/simuler-paiement', { requestId, statut, modePaiement, reference }).then((r) => r.data),
  byRequest: (requestId) => http.get(`/api/abonnements/request/${requestId}`).then((r) => r.data),
  adminList: () => http.get('/api/admin/abonnements').then((r) => r.data),
  adminStats: () => http.get('/api/admin/abonnements/statistiques').then((r) => r.data),
}
