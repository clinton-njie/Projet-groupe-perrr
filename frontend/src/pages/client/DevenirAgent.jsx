import { useState } from 'react'
import { Link } from 'react-router-dom'
import { useToast } from '../../components/toast-context.jsx'
import { useAuth } from '../../context/AuthContext.jsx'
import { usersApi } from '../../api/users.js'
import { apiError } from '../../api/http.js'

export default function DevenirAgent() {
  const notify = useToast()
  const { authUserId, isAgent, isAdmin } = useAuth()
  const [justification, setJustification] = useState('')
  const [sent, setSent] = useState(false)
  const [busy, setBusy] = useState(false)
  const [error, setError] = useState('')

  const submit = async (e) => {
    e.preventDefault()
    setBusy(true); setError('')
    try {
      await usersApi.requestRoleChange(authUserId, 'ROLE_AGENT', justification)
      setSent(true)
      notify('Demande envoyée — en attente de validation par un administrateur.')
    } catch (err) { setError(apiError(err, "L'envoi de la demande a échoué.")) }
    finally { setBusy(false) }
  }

  return (
    <div className="page active client-inner">
      <div className="inner">
        <div className="wrap">
          <div className="crumb"><Link to="/">Accueil</Link> / <b>Devenir agent</b></div>
          <div className="detail-layout" style={{ gridTemplateColumns: '1fr 420px', alignItems: 'start' }}>
            <div>
              <span className="eyebrow">Rejoindre le réseau</span>
              <h1 className="page-title serif" style={{ marginTop: 12 }}>Devenez agent ImmoAPP</h1>
              <p className="d-desc" style={{ marginTop: 20 }}>
                En devenant agent, vous pouvez publier vos propres biens, gérer vos visites et réservations,
                échanger avec les clients et suivre vos performances. Votre demande est validée par un administrateur.
              </p>
              <div className="d-sec-title">Avantages</div>
              <ul className="d-feats">
                <li>Publication de biens illimitée</li><li>Tableau de bord & statistiques</li>
                <li>Messagerie clients intégrée</li><li>Gestion des visites payantes</li>
                <li>Badge agent vérifié</li><li>Paiements sécurisés Mobile Money</li>
              </ul>
              <div className="becomes" style={{ marginTop: 34 }}>
                <div><div className="bt serif">Un abonnement est requis</div>
                  <p>La publication de biens nécessite un abonnement actif (mensuel 10 000 FCFA ou annuel 100 000 FCFA), souscrit après validation.</p></div>
              </div>
            </div>

            <aside>
              <div className="form-card" style={{ padding: 30 }}>
                {isAdmin ? (
                  <div className="center" style={{ padding: '20px 0' }}>
                    <div style={{ fontSize: 40, color: 'var(--gold)' }}>★</div>
                    <h3 className="serif" style={{ margin: '10px 0 6px' }}>Vous êtes administrateur</h3>
                    <p className="muted" style={{ fontSize: 14 }}>Le rôle d'agent ne s'applique pas à un compte administrateur.</p>
                    <Link className="btn-outline" to="/admin" style={{ display: 'inline-block', marginTop: 12, padding: '13px 26px', width: 'auto' }}>Accéder à l'espace admin</Link>
                  </div>
                ) : isAgent ? (
                  <div className="center" style={{ padding: '20px 0' }}>
                    <div style={{ fontSize: 40, color: 'var(--gold)' }}>✓</div>
                    <h3 className="serif" style={{ margin: '10px 0 6px' }}>Vous êtes déjà agent</h3>
                    <Link className="btn-outline" to="/agent" style={{ display: 'inline-block', marginTop: 12, padding: '13px 26px', width: 'auto' }}>Accéder à mon espace</Link>
                  </div>
                ) : sent ? (
                  <div className="center" style={{ padding: '20px 0' }}>
                    <div style={{ fontSize: 40, color: 'var(--gold)' }}>✓</div>
                    <h3 className="serif" style={{ margin: '10px 0 6px' }}>Demande envoyée</h3>
                    <p className="muted" style={{ fontSize: 14 }}>Vous serez notifié dès qu'un administrateur aura examiné votre demande.</p>
                    <Link className="btn-outline" to="/profil" style={{ display: 'inline-block', marginTop: 20, padding: '13px 26px', width: 'auto' }}>Retour au profil</Link>
                  </div>
                ) : (
                  <form onSubmit={submit}>
                    <h3 className="serif" style={{ marginBottom: 6 }}>Demande de changement de rôle</h3>
                    <p className="muted" style={{ fontSize: 13, marginBottom: 20 }}>Client → Agent</p>
                    {error && <div className="form-error">{error}</div>}
                    <div className="field"><label>Rôle demandé</label><input value="Agent" disabled /></div>
                    <div className="field"><label>Justification</label>
                      <textarea rows={5} required value={justification} onChange={(e) => setJustification(e.target.value)} placeholder="Décrivez votre activité, votre portefeuille de biens, votre agence…" /></div>
                    <button className="btn-block" type="submit" disabled={busy}>{busy ? 'Envoi…' : 'Envoyer la demande'}</button>
                  </form>
                )}
              </div>
            </aside>
          </div>
        </div>
      </div>
    </div>
  )
}
