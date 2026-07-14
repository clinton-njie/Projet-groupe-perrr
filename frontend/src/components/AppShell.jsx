import { useState } from 'react'
import { NavLink, useNavigate, Outlet, useLocation } from 'react-router-dom'
import { Avatar } from './ui.jsx'
import { isRealImg } from './ui.jsx'
import NotificationsMenu from './NotificationsMenu.jsx'
import { ToastProvider } from './toast-context.jsx'
import { useAuth } from '../context/AuthContext.jsx'
import { useTheme } from '../hooks/useTheme.js'

// Shell agent/admin : sidebar fixe + barre supérieure. `dark` = sidebar admin sombre.
export default function AppShell({ id, dark, brandBadge, roleLabel, nav, headerAction }) {
  const [sideOpen, setSideOpen] = useState(false)
  const navigate = useNavigate()
  const loc = useLocation()
  const { user, profile, logout } = useAuth()
  const { isDark, toggle: toggleTheme } = useTheme()

  const current = nav.find((n) => n.to === loc.pathname)
  const title = current ? current.label : ''
  const nom = profile ? `${profile.firstname || ''} ${profile.lastname || ''}`.trim() : (user?.email || '')
  const seed = user?.email || 'me'
  const photo = profile?.photoProfil

  const avatarNode = (size) =>
    isRealImg(photo)
      ? <div className="av" style={{ width: size, height: size, borderRadius: '50%', border: '1.5px solid var(--gold)', backgroundImage: `url(${photo})`, backgroundSize: 'cover', backgroundPosition: 'center' }} />
      : <Avatar seed={seed} size={size} style={{ border: '1.5px solid var(--gold)' }} />

  const doLogout = async () => { await logout(); navigate('/') }

  return (
    <ToastProvider>
      <div className={dark ? 'app-admin' : 'app-agent'}>
        <div id={id} className="app" style={{ display: 'flex', minHeight: '100svh' }}>
          <aside className="side" style={sideOpen ? { transform: 'none' } : undefined}>
            <div className="brand-a" onClick={() => navigate('/')}>Immo<span>APP</span></div>
            <div className="agent-badge">
              {avatarNode(44)}
              <div>
                <div className="an">{nom || 'Utilisateur'}</div>
                <div className="ar">{roleLabel}</div>
              </div>
              {brandBadge && <span className="sa-tag">{brandBadge}</span>}
            </div>
            <ul className="snav">
              {nav.map((n) => (
                <li key={n.to}>
                  <NavLink to={n.to} end className={({ isActive }) => (isActive ? 'on' : '')} onClick={() => setSideOpen(false)}>
                    <span className="ic">{n.ic}</span>
                    <span>{n.label}</span>
                    {n.bubble ? <span className="bubble">{n.bubble}</span> : null}
                  </NavLink>
                </li>
              ))}
            </ul>
            <div className="logout" onClick={doLogout}>↩ Déconnexion</div>
          </aside>

          <div className="app-main">
            <div className="app-top">
              <div style={{ display: 'flex', alignItems: 'center', gap: 14 }}>
                <button className="linklike" style={{ fontSize: 22 }} onClick={() => setSideOpen((o) => !o)}>☰</button>
                <h1 className="serif">{title}</h1>
              </div>
              <div className="r">
                {headerAction}
                <button className="theme-toggle" onClick={toggleTheme} title={isDark ? 'Mode clair' : 'Mode sombre'}>{isDark ? '☀' : '☾'}</button>
                <NotificationsMenu />
                {avatarNode(38)}
              </div>
            </div>
            <div className="app-body"><Outlet /></div>
          </div>
        </div>
      </div>
    </ToastProvider>
  )
}
