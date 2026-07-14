import { useState, useEffect } from 'react'
import { useParams, useNavigate, useSearchParams } from 'react-router-dom'
import { Loader } from '../../components/states.jsx'
import authImg from '../../assets/auth-3.jpeg'
import { adminApi } from '../../api/admin.js'
import { apiError } from '../../api/http.js'

export default function AccepterInvitation() {
  const { token: paramToken } = useParams()
  const [sp] = useSearchParams()
  const token = paramToken || sp.get('token') // /invitation/:token OU /creer-compte-admin?token=
  const nav = useNavigate()
  const [checking, setChecking] = useState(true)
  const [valid, setValid] = useState(false)
  const [info, setInfo] = useState(null)
  const [form, setForm] = useState({ firstname: '', lastname: '', password: '' })
  const [confirm, setConfirm] = useState('')
  const [error, setError] = useState('')
  const [busy, setBusy] = useState(false)
  const [done, setDone] = useState(false)
  const set = (k) => (e) => setForm((f) => ({ ...f, [k]: e.target.value }))

  useEffect(() => {
    adminApi.validateInvite(token)
      .then((d) => { setValid(true); setInfo(d) })
      .catch(() => setValid(false))
      .finally(() => setChecking(false))
  }, [token])

  const submit = async (e) => {
    e.preventDefault()
    if (form.password !== confirm) { setError('Les deux mots de passe ne correspondent pas.'); return }
    setBusy(true); setError('')
    try { await adminApi.acceptInvite(token, form); setDone(true) }
    catch (err) { setError(apiError(err, "L'acceptation a échoué (invitation expirée ?).")) }
    finally { setBusy(false) }
  }

  return (
    <div className="page active">
      <div className="auth">
        <div className="auth-visual" style={{ backgroundImage: `url(${authImg})`, backgroundSize: 'cover', backgroundPosition: 'center' }}>
          <div className="vtext"><span className="eyebrow">ImmoAPP · Administration</span><h2 className="serif">Créez votre compte administrateur.</h2></div>
        </div>
        <div className="auth-form-wrap">
          <div className="auth-form">
            <div className="brand-c" onClick={() => nav('/')}>Immo<span>APP</span></div>
            {checking ? <Loader label="Vérification de l'invitation…" /> : !valid ? (
              <>
                <h1 className="serif">Invitation invalide</h1>
                <p className="sub">Ce lien d'invitation est invalide ou a expiré.</p>
                <button className="btn-block" onClick={() => nav('/')}>Retour à l'accueil</button>
              </>
            ) : done ? (
              <>
                <h1 className="serif">Compte créé</h1>
                <p className="sub">Votre compte administrateur est prêt. Vous pouvez vous connecter.</p>
                <button className="btn-block" onClick={() => nav('/connexion')}>Se connecter</button>
              </>
            ) : (
              <form onSubmit={submit}>
                <h1 className="serif">Finaliser mon compte</h1>
                <p className="sub">{info?.email ? `Invitation pour ${info.email}` : 'Renseignez vos informations.'}</p>
                {error && <div className="form-error">{error}</div>}
                <div className="field two">
                  <div><label>Prénom</label><input value={form.firstname} onChange={set('firstname')} required /></div>
                  <div><label>Nom</label><input value={form.lastname} onChange={set('lastname')} required /></div>
                </div>
                <div className="field"><label>Mot de passe</label><input type="password" minLength={6} value={form.password} onChange={set('password')} placeholder="6 caractères minimum" required /></div>
                <div className="field"><label>Confirmer le mot de passe</label><input type="password" minLength={6} value={confirm} onChange={(e) => setConfirm(e.target.value)} placeholder="Retapez le mot de passe" required /></div>
                <button className="btn-block" type="submit" disabled={busy}>{busy ? 'Création…' : 'Créer mon compte'}</button>
              </form>
            )}
          </div>
        </div>
      </div>
    </div>
  )
}
