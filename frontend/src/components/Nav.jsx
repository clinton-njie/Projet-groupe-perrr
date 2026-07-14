import { useState, useEffect } from 'react'
import { Link, useNavigate, useLocation } from 'react-router-dom'
import { useAuth } from '../context/AuthContext.jsx'
import { InstallButton } from './states.jsx'
import { useTheme } from '../hooks/useTheme.js'

export default function Nav() {
  const [solid, setSolid] = useState(false)
  const nav = useNavigate()
  const loc = useLocation()
  const { user, isAgent, isAdmin, logout } = useAuth()
  const { isDark, toggle: toggleTheme } = useTheme()
  const isHome = loc.pathname === '/'

  useEffect(() => {
    const onScroll = () => setSolid(window.scrollY > 40)
    onScroll()
    window.addEventListener('scroll', onScroll)
    return () => window.removeEventListener('scroll', onScroll)
  }, [])

  const forceSolid = !isHome
  const cls = `nav ${solid || forceSolid ? 'solid' : ''} ${forceSolid ? 'force-solid' : ''}`
  const espace = isAdmin ? '/admin' : isAgent ? '/agent' : '/profil'

  const links = [
    ['Acheter / Louer', '/biens'],
    ['Nos agents', '/agents'],
    ...(user ? [['Mes visites', '/mes-visites'], ['Messagerie', '/messagerie']] : []),
    ...(!isAgent && !isAdmin ? [['Devenir agent', '/devenir-agent']] : []),
  ]

  const doLogout = async () => { await logout(); nav('/') }

  return (
    <nav className={cls}>
      <div className="brand" onClick={() => nav('/')}>Immo<span>APP</span></div>
      <ul className="nav-links" style={{ display: 'flex' }}>
        {links.map(([t, to]) => (
          <li key={to}><Link className={loc.pathname === to ? 'on' : ''} to={to}>{t}</Link></li>
        ))}
      </ul>
      <div className="nav-right">
        <button className="theme-toggle" onClick={toggleTheme} title={isDark ? 'Mode clair' : 'Mode sombre'} aria-label="Basculer le thème">{isDark ? '☀' : '☾'}</button>
        <span className="install-slot"><InstallButton className="btn-nav" /></span>
        {user ? (
          <>
            <span className="nav-login" onClick={doLogout}>Déconnexion</span>
            <button className="btn-nav" onClick={() => nav(espace)}>Mon espace</button>
          </>
        ) : (
          <>
            <span className="nav-login" onClick={() => nav('/connexion')}>Connexion</span>
            <button className="btn-nav" onClick={() => nav('/inscription')}>Inscription</button>
          </>
        )}
      </div>
    </nav>
  )
}
