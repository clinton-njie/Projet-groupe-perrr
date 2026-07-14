import { http } from './http.js'

// service-utilisateur (/api/users). Pas d'id dans le JWT → résolution par email.
export const usersApi = {
  idByEmail: (email) => http.get(`/api/users/by-email/${encodeURIComponent(email)}`).then((r) => r.data),
  getProfile: (id) => http.get(`/api/users/${id}`).then((r) => r.data),
  updateProfile: (id, payload) => http.put(`/api/users/${id}`, payload).then((r) => r.data),
  // Demande de passage CLIENT → AGENT (validée par un admin)
  requestRoleChange: (id, requestedRole, justification) =>
    http.post(`/api/users/${id}/request-role-change`, { requestedRole, justification }).then((r) => r.data),
  deletionStatus: (id) => http.get(`/api/users/${id}/deletion-status`).then((r) => r.data),
}
