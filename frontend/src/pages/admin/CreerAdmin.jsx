import { useState } from 'react'
import { useNavigate } from 'react-router-dom'
import { useToast } from '../../components/toast-context.jsx'
import { adminApi } from '../../api/admin.js'
import { apiError } from '../../api/http.js'
import { setKnownRole } from '../../lib/roleRegistry.js'

export default function AdminCreerAdmin() {
  const nav = useNavigate()
  const notify = useToast()
  const [form, setForm] = useState({ firstname: '', lastname: '', email: '', password: '', role: 'ROLE_ADMIN' })
  const [confirm, setConfirm] = useState('')
  const [busy, setBusy] = useState(false)
  const [error, setError] = useState('')
  const set = (k) => (e) => setForm((f) => ({ ...f, [k]: e.target.value }))

  const submit = async (e) => {
    e.preventDefault()
    if (form.password !== confirm) { setError('Les deux mots de passe ne correspondent pas.'); return }
    setBusy(true); setError('')
    try {
      await adminApi.createAdmin(form)
      setKnownRole(form.email, form.role) // pour l'affichage dans la liste des admins
      notify('Compte administrateur créé')
      nav('/admin/administrateurs')
    } catch (err) { setError(apiError(err, "La création a échoué (e-mail déjà utilisé ?).")) }
    finally { setBusy(false) }
  }

  return (
    <div className="view active">
      <form className="form-card" style={{ maxWidth: 560 }} onSubmit={submit}>
        <div className="form-sec">Créer un compte administrateur</div>
        <p className="muted" style={{ fontSize: 13.5, marginBottom: 20 }}>Création directe d'un administrateur (auth-service).</p>
        {error && <div className="form-error">{error}</div>}
        <div className="fgrid">
          <div className="field"><label>Prénom</label><input value={form.firstname} onChange={set('firstname')} required /></div>
          <div className="field"><label>Nom</label><input value={form.lastname} onChange={set('lastname')} required /></div>
        </div>
        <div className="field"><label>Adresse e-mail</label><input type="email" value={form.email} onChange={set('email')} required /></div>
        <div className="fgrid">
          <div className="field"><label>Rôle</label>
            <select value={form.role} onChange={set('role')}><option value="ROLE_ADMIN">ROLE_ADMIN</option><option value="ROLE_SUPER_ADMIN">ROLE_SUPER_ADMIN</option></select>
          </div>
          <div className="field"><label>Mot de passe</label><input type="password" minLength={6} value={form.password} onChange={set('password')} placeholder="6 caractères min." required /></div>
          <div className="field"><label>Confirmer le mot de passe</label><input type="password" minLength={6} value={confirm} onChange={(e) => setConfirm(e.target.value)} placeholder="Retapez le mot de passe" required /></div>
        </div>
        <button className="btn-pub" type="submit" style={{ marginTop: 20 }} disabled={busy}>{busy ? 'Création…' : "Créer l'administrateur"}</button>
      </form>
    </div>
  )
}
