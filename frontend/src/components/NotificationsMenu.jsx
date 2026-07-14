import { useState } from 'react'
import { notificationsApi } from '../api/notifications.js'
import { apiError } from '../api/http.js'

const fmt = (s) => (s ? new Date(s).toLocaleString('fr-FR', { dateStyle: 'short', timeStyle: 'short' }) : '')

// Panneau notifications — GET /api/notifications/me (utilisateur courant, JWT).
export default function NotificationsMenu() {
  const [open, setOpen] = useState(false)
  const [items, setItems] = useState([])
  const [loading, setLoading] = useState(false)
  const [error, setError] = useState('')
  const [loaded, setLoaded] = useState(false)

  const load = async () => {
    setLoading(true); setError('')
    try { const d = await notificationsApi.list(); setItems(Array.isArray(d) ? d : []) }
    catch (e) { setError(apiError(e, 'Impossible de charger les notifications.')) }
    finally { setLoading(false); setLoaded(true) }
  }

  const toggle = () => {
    const next = !open
    setOpen(next)
    if (next && !loaded) load()
  }

  return (
    <div style={{ position: 'relative' }}>
      <span className="bell" onClick={toggle} title="Notifications">🔔</span>
      <div className={`notif-menu ${open ? 'show' : ''}`} style={{ right: 0, top: '140%' }}>
        <div className="nh"><span>Notifications</span><a onClick={load}>Actualiser</a></div>
        {loading && <div className="notif-item"><span className="dot read" /><div className="nt">Chargement…</div></div>}
        {!loading && error && <div className="notif-item"><span className="dot" /><div><div className="nt">Erreur</div><div className="nd">{error}</div></div></div>}
        {!loading && !error && items.length === 0 && (
          <div className="notif-item"><span className="dot read" /><div><div className="nt">Aucune notification</div><div className="nd">Vous êtes à jour.</div></div></div>
        )}
        {!loading && !error && items.map((n) => (
          <div className="notif-item" key={n.id}>
            <span className="dot" />
            <div><div className="nt">{n.message}</div><div className="nd">{fmt(n.createdAt)}</div></div>
          </div>
        ))}
      </div>
    </div>
  )
}
