import { http } from './http.js'

// service-notification (/api/notifications).
// GET /api/notifications/me → notifications de l'utilisateur courant (JWT).
export const NOTIFICATIONS_LIST_AVAILABLE = true

export const notificationsApi = {
  listAvailable: NOTIFICATIONS_LIST_AVAILABLE,
  // NotificationResponse : { id, destinataire, message, createdAt }
  list: () => http.get('/api/notifications/me').then((r) => r.data),
  envoyer: (payload) => http.post('/api/notifications', payload).then((r) => r.data),
}
