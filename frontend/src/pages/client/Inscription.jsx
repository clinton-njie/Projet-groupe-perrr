import { useState } from 'react'
import { Link, useNavigate } from 'react-router-dom'
import { useAuth } from '../../context/AuthContext.jsx'
import { apiError } from '../../api/http.js'
import authImg from '../../assets/auth-2.jpeg'

export default function Inscription() {
  const nav = useNavigate()
  const { register } = useAuth()
  const [form, setForm] = useState({ firstname: '', lastname: '', email: '', password: '' })
  const [confirm, setConfirm] = useState('')
  const [error, setError] = useState('')
  const [busy, setBusy] = useState(false)
  const set = (k) => (e) => setForm((f) => ({ ...f, [k]: e.target.value }))

  const submit = async (e) => {
    e.preventDefault()
    if (form.password !== confirm) { setError('Les deux mots de passe ne correspondent pas.'); return }
    setError(''); setBusy(true)
    try {
      await register(form) // crée le compte (CLIENT) puis connecte
      nav('/profil', { replace: true })
    } catch (err) {
      setError(apiError(err, "L'inscription a échoué (e-mail déjà utilisé ?)."))
    } finally { setBusy(false) }
  }

  return (
    <div className="page active">
      <div className="auth">
        <div className="auth-visual" style={{ backgroundImage: `url(${authImg})`, backgroundSize: 'cover', backgroundPosition: 'center' }}>
          <div className="vtext">
            <span className="eyebrow">Rejoignez ImmoAPP</span>
            <h2 className="serif">Votre prochaine adresse commence ici.</h2>
          </div>
        </div>
        <div className="auth-form-wrap">
          <form className="auth-form" onSubmit={submit}>
            <div className="brand-c" onClick={() => nav('/')}>Immo<span>APP</span></div>
            <h1 className="serif">Créer un compte</h1>
            <p className="sub">Inscription gratuite en tant que client.</p>
            {error && <div className="form-error">{error}</div>}
            <div className="field two">
              <div><label>Prénom</label><input value={form.firstname} onChange={set('firstname')} placeholder="Christale" required /></div>
              <div><label>Nom</label><input value={form.lastname} onChange={set('lastname')} placeholder="Teufack" required /></div>
            </div>
            <div className="field"><label>Adresse e-mail</label><input type="email" value={form.email} onChange={set('email')} placeholder="vous@exemple.com" required /></div>
            <div className="field"><label>Mot de passe</label><input type="password" value={form.password} onChange={set('password')} placeholder="6 caractères minimum" minLength={6} required /></div>
            <div className="field"><label>Confirmer le mot de passe</label><input type="password" value={confirm} onChange={(e) => setConfirm(e.target.value)} placeholder="Retapez le mot de passe" minLength={6} required /></div>
            <button className="btn-block" type="submit" disabled={busy}>{busy ? 'Création…' : "S'inscrire"}</button>
            <p className="auth-alt">Déjà inscrit ? <Link to="/connexion">Se connecter</Link></p>
          </form>
        </div>
      </div>
    </div>
  )
}
