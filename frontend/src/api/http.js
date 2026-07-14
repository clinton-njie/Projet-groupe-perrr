import axios from 'axios'
import { getAccess, getRefresh, setTokens, clearTokens } from './tokens.js'

// En DEV : chemins relatifs → proxy Vite (même origine, pas de CORS).
// En PROD : base absolue VITE_API_URL (nécessite le CORS côté back).
export const API_BASE = import.meta.env.DEV ? '' : (import.meta.env.VITE_API_URL || '')
export const MESSAGING_BASE = import.meta.env.DEV ? '' : (import.meta.env.VITE_MESSAGING_URL || import.meta.env.VITE_API_URL || '')

// Handler appelé quand la session ne peut plus être rétablie (refresh échoué).
let onAuthFailure = () => {}
export const setAuthFailureHandler = (fn) => { onAuthFailure = fn }

// timeout : évite qu'un microservice bloqué fasse « pendre » l'UI indéfiniment.
export const http = axios.create({
  baseURL: API_BASE,
  headers: { 'Content-Type': 'application/json' },
  timeout: 20000,
})

// Instance messagerie (base potentiellement différente).
export const httpMessaging = axios.create({
  baseURL: MESSAGING_BASE,
  headers: { 'Content-Type': 'application/json' },
  timeout: 20000,
})

// ---- Intercepteur requête : ajoute le Bearer (sauf endpoints d'auth) ----
const attachAuth = (config) => {
  const token = getAccess()
  if (token && !config.skipAuth) {
    config.headers = config.headers || {}
    config.headers.Authorization = `Bearer ${token}`
  }
  return config
}
http.interceptors.request.use(attachAuth)
httpMessaging.interceptors.request.use(attachAuth)

// ---- Rafraîchissement du token (un seul en vol, requêtes mises en file) ----
let refreshing = null

async function refreshAccessToken() {
  const refreshToken = getRefresh()
  if (!refreshToken) throw new Error('no-refresh-token')
  // Appel direct (sans intercepteur de refresh) vers l'endpoint réel.
  const { data } = await axios.post(
    `${API_BASE}/api/auth/refresh-token`,
    { refreshToken },
    { headers: { 'Content-Type': 'application/json' } }
  )
  setTokens({ accessToken: data.accessToken, refreshToken: data.refreshToken })
  return data.accessToken
}

const isAuthEndpoint = (url = '') =>
  url.includes('/api/auth/login') || url.includes('/api/auth/register') || url.includes('/api/auth/refresh-token')

function makeResponseErrorHandler(instance) {
  return async (error) => {
    const { response, config } = error
    const status = response?.status
    // Le back renvoie 403 (et non 401) sur token expiré/invalide → on tente aussi
    // le refresh sur 403. Si le refresh réussit mais que ça reste 403 (droits
    // insuffisants réels), on ne déconnecte pas : la requête est simplement rejetée.
    // On ne tente rien s'il n'y a pas de refresh token (invité).
    const refreshable = status === 401 || status === 403
    if (!response || !refreshable || !config || config._retry || isAuthEndpoint(config.url) || !getRefresh()) {
      return Promise.reject(error)
    }
    config._retry = true
    try {
      if (!refreshing) refreshing = refreshAccessToken().finally(() => { refreshing = null })
      const newToken = await refreshing
      config.headers = config.headers || {}
      config.headers.Authorization = `Bearer ${newToken}`
      return instance(config)
    } catch (e) {
      // Refresh impossible → session réellement expirée → déconnexion.
      clearTokens()
      onAuthFailure()
      return Promise.reject(error)
    }
  }
}

http.interceptors.response.use((r) => r, makeResponseErrorHandler(http))
httpMessaging.interceptors.response.use((r) => r, makeResponseErrorHandler(httpMessaging))

// Message d'erreur lisible depuis une réponse back.
export function apiError(err, fallback = 'Une erreur est survenue.') {
  if (err?.code === 'ECONNABORTED') return 'Le service met trop de temps à répondre. Réessayez dans un instant.'
  const status = err?.response?.status
  if (!err?.response) return 'Connexion au serveur impossible. Vérifiez votre réseau et réessayez.'
  if (status >= 500) return 'Ce service est momentanément indisponible. Réessayez plus tard.'
  const d = err.response.data
  if (typeof d === 'string' && d && !d.trim().startsWith('<')) return d
  return d?.message || d?.error || err?.message || fallback
}
