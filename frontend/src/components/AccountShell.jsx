import { Link, useNavigate, useLocation } from 'react-router-dom'
import { Avatar, isRealImg } from './ui.jsx'
import { useAuth } from '../context/AuthContext.jsx'
import { ROLE_LABEL } from '../lib/constants.js'

const MENU = [
  ['Informations', '/profil'],
  ['Mes visites', '/mes-visites'],
  ['Mes réservations', '/mes-reservations'],
  ['Mes favoris', '/mes-favoris'],
  ['Messagerie', '/messagerie'],
]

export default function AccountShell({ title, children }) {
  const nav = useNavigate()
  const loc = useLocation()
  const { user, profile, roles, logout } = useAuth()
  const nom = profile ? `${profile.firstname || ''} ${profile.lastname || ''}`.trim() : user?.email
  const role = roles?.[0] || 'ROLE_CLIENT'
  const photo = profile?.photoProfil
  const doLogout = async () => { await logout(); nav('/') }

  return (
    <div className="page active client-inner">
      <div className="inner">
        <div className="wrap">
          <div className="crumb"><Link to="/">Accueil</Link> / <b>{title}</b></div>
          <h1 className="page-title serif">{title}</h1>
          <div className="profil-layout">
            <aside className="profil-side">
              <div className="profil-card">
                {isRealImg(photo)
                  ? <div className="av" style={{ width: 96, height: 96, borderRadius: '50%', margin: '0 auto 16px', border: '2px solid var(--gold)', backgroundImage: `url(${photo})`, backgroundSize: 'cover', backgroundPosition: 'center' }} />
                  : <div className="av" style={{ width: 96, height: 96, margin: '0 auto 16px', border: '2px solid var(--gold)' }}><Avatar seed={user?.email || 'me'} size={96} /></div>}
                <div className="pn serif">{nom || 'Mon compte'}</div>
                <div className="role">{ROLE_LABEL[role]}</div>
                <div className="pe">{user?.email}</div>
              </div>
              <ul className="side-menu">
                {MENU.map(([t, to]) => (
                  <li key={to}><Link className={loc.pathname === to ? 'on' : ''} to={to}>{t} <span>›</span></Link></li>
                ))}
                <li><a className="danger" onClick={doLogout}>Déconnexion <span>↩</span></a></li>
              </ul>
            </aside>
            <div>{children}</div>
          </div>
        </div>
      </div>
    </div>
  )
}
