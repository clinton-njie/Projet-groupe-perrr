import { httpMessaging } from './http.js'

// service-messagerie (/api/messages) — REST sans temps réel (bouton Actualiser).
// Sur le VPS, routé par la gateway ; instance dédiée au cas où le port 8089
// serait exposé séparément (VITE_MESSAGING_URL).
export const messagesApi = {
  inbox: () => httpMessaging.get('/api/messages/inbox').then((r) => r.data),
  conversation: (id) => httpMessaging.get(`/api/messages/conversation/${id}`).then((r) => r.data),
  // 1re fois : { receiverId, bienId, contenu }. Ensuite : { conversationId, contenu }.
  send: (payload) => httpMessaging.post('/api/messages', payload).then((r) => r.data),
  markRead: (id) => httpMessaging.put(`/api/messages/${id}/read`).then((r) => r.data),
  deleteMessage: (id) => httpMessaging.delete(`/api/messages/${id}`).then((r) => r.data),
  deleteConversation: (id) => httpMessaging.delete(`/api/messages/conversations/${id}`).then((r) => r.data),
}
