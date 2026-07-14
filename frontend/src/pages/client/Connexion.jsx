import { useState } from 'react'
import { Link, useNavigate, useLocation } from 'react-router-dom'
import { useAuth } from '../../context/AuthContext.jsx'
import { apiError } from '../../api/http.js'
import authImg from '../../assets/auth-1.jpeg'

export default function Connexion() {
  const nav = useNavigate()
  const loc = useLocation()
  const { login } = useAuth()
  const [email, setEmail] = useState('')
  const [password, setPassword] = useState('')
  const [error, setError] = useState('')
  const [busy, setBusy] = useState(false)

  const submit = async (e) => {
    e.preventDefault()
    setError(''); setBusy(true)
    try {
      const u = await login(email, password)
      const dest = loc.state?.from
        || (u.roles.some((r) => r === 'ROLE_ADMIN' || r === 'ROLE_SUPER_ADMIN') ? '/admin'
          : u.roles.includes('ROLE_AGENT') ? '/agent' : '/profil')
      nav(dest, { replace: true })
    } catch (err) {
      setError(apiError(err, 'Identifiants incorrects.'))
    } finally { setBusy(false) }
  }

  return (
    <div className="page active">
      <div className="auth">
        <div className="auth-visual" style={{ backgroundImage: `url(${authImg})`, backgroundSize: 'cover', backgroundPosition: 'center' }}>
          <div className="vtext">
            <span className="eyebrow">ImmoAPP</span>
            <h2 className="serif">Le bon endroit, au bon moment.</h2>
          </div>
        </div>
        <div className="auth-form-wrap">
          <form className="auth-form" onSubmit={submit}>
            <div className="brand-c" onClick={() => nav('/')}>Immo<span>APP</span></div>
            <h1 className="serif">Connexion</h1>
            <p className="sub">Accédez à votre espace personnel.</p>
            {error && <div className="form-error">{error}</div>}
            <div className="field">
              <label>Adresse e-mail</label>
              <input type="email" value={email} onChange={(e) => setEmail(e.target.value)} placeholder="vous@exemple.com" required />
            </div>
            <div className="field">
              <label>Mot de passe</label>
              <input type="password" value={password} onChange={(e) => setPassword(e.target.value)} placeholder="••••••••" required />
            </div>
            <button className="btn-block" type="submit" disabled={busy}>{busy ? 'Connexion…' : 'Se connecter'}</button>
            <p className="auth-alt">Pas encore de compte ? <Link to="/inscription">Créer un compte</Link></p>
          </form>
        </div>
      </div>
    </div>
  )
}
