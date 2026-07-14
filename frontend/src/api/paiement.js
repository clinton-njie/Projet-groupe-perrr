import { http } from './http.js'

// service-paiement (/payments). En pratique le paiement est déclenché par les
// services métier (visite/réservation/abonnement). Exposé au cas où.
export const paiementApi = {
  initier: (payload) => http.post('/payments', payload).then((r) => r.data),
}
