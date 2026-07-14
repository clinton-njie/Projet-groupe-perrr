import { Navigate, useLocation } from 'react-router-dom'
import { useAuth } from '../context/AuthContext.jsx'
import { Loader } from '../components/states.jsx'

// Garde de route. `roles` = liste des rôles autorisés (au moins un requis).
export default function ProtectedRoute({ roles, children }) {
  const { user, loading, hasRole } = useAuth()
  const loc = useLocation()

  if (loading) return <div style={{ minHeight: '60svh' }}><Loader label="Vérification de la session…" /></div>
  if (!user) return <Navigate to="/connexion" replace state={{ from: loc.pathname }} />

  if (roles && roles.length && !roles.some((r) => hasRole(r))) {
    // Connecté mais rôle insuffisant → renvoyé vers son espace.
    const home = hasRole('ROLE_ADMIN') || hasRole('ROLE_SUPER_ADMIN') ? '/admin'
      : hasRole('ROLE_AGENT') ? '/agent' : '/'
    return <Navigate to={home} replace />
  }
  return children
}

// Redirige un utilisateur déjà connecté hors des pages connexion/inscription.
export function GuestOnly({ children }) {
  const { user, loading } = useAuth()
  if (loading) return null
  if (user) return <Navigate to="/" replace />
  return children
}
