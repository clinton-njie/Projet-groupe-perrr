import AppShell from './AppShell.jsx'
import { useAuth } from '../context/AuthContext.jsx'

export default function AdminLayout() {
  const { isSuperAdmin } = useAuth()
  const nav = [
    { to: '/admin', label: 'Tableau de bord', ic: '▦' },
    { to: '/admin/demandes-role', label: 'Demandes de rôle', ic: '⇅' },
    { to: '/admin/utilisateurs', label: 'Utilisateurs', ic: '☺' },
    { to: '/admin/signalements', label: 'Signalements', ic: '⚑' },
    { to: '/admin/parametres', label: 'Paramètres', ic: '⚙' },
    { to: '/admin/profil', label: 'Mon profil', ic: '☺' },
    ...(isSuperAdmin ? [
      { to: '/admin/administrateurs', label: 'Administrateurs', ic: '✦' },
      { to: '/admin/creer-admin', label: 'Créer un admin', ic: '＋' },
    ] : []),
  ]
  return <AppShell id="app-admin" dark brandBadge={isSuperAdmin ? 'Super' : 'Admin'} roleLabel={isSuperAdmin ? 'Super-administrateur' : 'Administrateur'} nav={nav} />
}
