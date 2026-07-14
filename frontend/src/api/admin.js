import { http } from './http.js'

// service-admin (/admin) + création d'admin via auth-service.
export const adminApi = {
  users: () => http.get('/admin/users').then((r) => r.data),
  deactivateUser: (authUserId) => http.put(`/admin/users/${authUserId}/deactivate`).then((r) => r.data),
  reactivateUser: (authUserId) => http.put(`/admin/users/${authUserId}/reactivate`).then((r) => r.data),

  roleRequests: () => http.get('/admin/role-requests').then((r) => r.data),
  processRoleRequest: (id, decision, refusalReason) =>
    http.put(`/admin/role-requests/${id}/process`, { decision, refusalReason }).then((r) => r.data),

  signalements: () => http.get('/admin/signalements').then((r) => r.data),
  signalement: (id) => http.get(`/admin/signalements/${id}`).then((r) => r.data),
  objetsSignalement: () => http.get('/admin/signalements/objets').then((r) => r.data),
  updateSignalementStatut: (id, statut) => http.put(`/admin/signalements/${id}/statut`, { statut }).then((r) => r.data),

  // Invitations d'admins (super-admin)
  invite: (email) => http.post('/admin/invites', { email }).then((r) => r.data),
  validateInvite: (token) => http.get(`/admin/invites/${token}/validate`).then((r) => r.data),
  acceptInvite: (token, { password, firstname, lastname }) =>
    http.post(`/admin/invites/${token}/accept`, { password, firstname, lastname }).then((r) => r.data),

  // Création directe d'un admin (auth-service)
  createAdmin: (data) => http.post('/api/auth/admin/register', data).then((r) => r.data),
}
