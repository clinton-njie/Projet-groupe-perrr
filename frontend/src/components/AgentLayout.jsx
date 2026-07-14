import { useNavigate } from 'react-router-dom'
import AppShell from './AppShell.jsx'

export default function AgentLayout() {
  const navigate = useNavigate()
  const nav = [
    { to: '/agent', label: 'Tableau de bord', ic: '▦' },
    { to: '/agent/biens', label: 'Mes biens', ic: '⌂' },
    { to: '/agent/publier', label: 'Publier un bien', ic: '＋' },
    { to: '/agent/visites', label: 'Visites', ic: '◷' },
    { to: '/agent/reservations', label: 'Réservations', ic: '❑' },
    { to: '/agent/abonnement', label: 'Abonnement', ic: '★' },
    { to: '/agent/messagerie', label: 'Messagerie', ic: '✉' },
    { to: '/agent/profil', label: 'Mon profil', ic: '☺' },
  ]
  const action = <button className="btn-pub" onClick={() => navigate('/agent/publier')}>+ Publier</button>
  return <AppShell id="app-agent" roleLabel="Agent" nav={nav} headerAction={action} />
}
