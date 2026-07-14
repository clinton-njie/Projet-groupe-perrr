import { http } from './http.js'

// auth-service (/api/auth). login → {accessToken, refreshToken, email, role}
export const authApi = {
  register: (data) => http.post('/api/auth/register', data, { skipAuth: true }).then((r) => r.data),
  adminRegister: (data) => http.post('/api/auth/admin/register', data).then((r) => r.data),
  login: (email, password) => http.post('/api/auth/login', { email, password }, { skipAuth: true }).then((r) => r.data),
  me: () => http.get('/api/auth/me').then((r) => r.data),
  logout: (refreshToken) => http.post('/api/auth/logout', { refreshToken }).then((r) => r.data).catch(() => null),
  updateUserRole: (id, newRole) => http.put(`/api/auth/users/${id}/role`, { newRole }).then((r) => r.data),
}
