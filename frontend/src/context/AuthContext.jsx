import { createContext, useContext, useState, useEffect, useCallback } from 'react'
import { authApi } from '../api/auth.js'
import { usersApi } from '../api/users.js'
import { setTokens, clearTokens, getAccess, getRefresh } from '../api/tokens.js'
import { setAuthFailureHandler } from '../api/http.js'

const AuthCtx = createContext(null)
export const useAuth = () => useContext(AuthCtx)

export function AuthProvider({ children }) {
  const [user, setUser] = useState(null)      // { email, roles[], authUserId, profile }
  const [loading, setLoading] = useState(true) // réhydratation initiale

  // Récupère email+roles (/me), l'id (by-email) et le profil.
  const hydrate = useCallback(async () => {
    const me = await authApi.me() // { email, roles[] }
    const roles = me.roles || (me.role ? [me.role] : [])
    let authUserId = null
    let profile = null
    try {
      authUserId = await usersApi.idByEmail(me.email)
      if (authUserId != null) profile = await usersApi.getProfile(authUserId)
    } catch { /* profil non bloquant */ }
    const u = { email: me.email, roles, authUserId, profile }
    setUser(u)
    return u
  }, [])

  // Session expirée (refresh échoué) → déconnexion silencieuse.
  useEffect(() => {
    setAuthFailureHandler(() => { clearTokens(); setUser(null) })
  }, [])

  // Au chargement : réhydrate si un token est présent.
  useEffect(() => {
    let alive = true
    ;(async () => {
      if (!getAccess()) { setLoading(false); return }
      try { await hydrate() } catch { clearTokens() }
      finally { if (alive) setLoading(false) }
    })()
    return () => { alive = false }
  }, [hydrate])

  const login = useCallback(async (email, password) => {
    const data = await authApi.login(email, password) // { accessToken, refreshToken, email, role }
    setTokens({ accessToken: data.accessToken, refreshToken: data.refreshToken })
    return hydrate()
  }, [hydrate])

  const register = useCallback(async (payload) => {
    await authApi.register(payload) // crée le compte (CLIENT) sans token
    return login(payload.email, payload.password)
  }, [login])

  const logout = useCallback(async () => {
    try { await authApi.logout(getRefresh()) } catch { /* best-effort */ }
    clearTokens()
    setUser(null)
  }, [])

  const refreshProfile = useCallback(async () => {
    if (!user?.authUserId) return null
    const profile = await usersApi.getProfile(user.authUserId)
    setUser((u) => ({ ...u, profile }))
    return profile
  }, [user?.authUserId])

  const roles = user?.roles || []
  const hasRole = (r) => roles.includes(r)
  const value = {
    user, loading,
    isAuthenticated: !!user,
    roles,
    authUserId: user?.authUserId ?? null,
    profile: user?.profile ?? null,
    hasRole,
    isClient: hasRole('ROLE_CLIENT'),
    isAgent: hasRole('ROLE_AGENT'),
    isAdmin: hasRole('ROLE_ADMIN') || hasRole('ROLE_SUPER_ADMIN'),
    isSuperAdmin: hasRole('ROLE_SUPER_ADMIN'),
    login, register, logout, refreshProfile, setUser,
  }
  return <AuthCtx.Provider value={value}>{children}</AuthCtx.Provider>
}
