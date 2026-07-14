// Stockage des jetons JWT (localStorage). Les tokens ne sont JAMAIS journalisés.
const ACCESS = 'immoapp_access'
const REFRESH = 'immoapp_refresh'

export const getAccess = () => localStorage.getItem(ACCESS) || null
export const getRefresh = () => localStorage.getItem(REFRESH) || null

export function setTokens({ accessToken, refreshToken }) {
  if (accessToken) localStorage.setItem(ACCESS, accessToken)
  if (refreshToken) localStorage.setItem(REFRESH, refreshToken)
}

export function clearTokens() {
  localStorage.removeItem(ACCESS)
  localStorage.removeItem(REFRESH)
}

export const isAuthenticated = () => !!getAccess()
