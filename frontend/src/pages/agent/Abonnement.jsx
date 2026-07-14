import { useState } from 'react'
import { Badge, Modal } from '../../components/ui.jsx'
import { AsyncState } from '../../components/states.jsx'
import { useToast } from '../../components/toast-context.jsx'
import { useApi } from '../../hooks/useApi.js'
import { abonnementsApi } from '../../api/abonnements.js'
import { apiError } from '../../api/http.js'
import { PLANS, ABO_STATUT_LABEL, ABO_STATUT_BADGE, fmtPrix } from '../../lib/constants.js'

const fmt = (s) => (s ? new Date(s).toLocaleDateString('fr-FR', { dateStyle: 'medium' }) : '—')
const sleep = (ms) => new Promise((r) => setTimeout(r, ms))

export default function AgentAbonnement() {
  const notify = useToast()
  const statut = useApi(() => abonnementsApi.monStatut(), [])
  const histo = useApi(() => abonnementsApi.monHistorique().catch(() => []), [])
  const st = statut.data
  // mon-historique renvoie { userId, nombre, abonnements: [...] }
  const historique = histo.data?.abonnements ?? (Array.isArray(histo.data) ? histo.data : [])

  const [plan, setPlan] = useState(null)
  const [operateur, setOperateur] = useState('ORANGE')
  const [tel, setTel] = useState('')
  const [paying, setPaying] = useState(false)
  const [error, setError] = useState('')

  // Attend l'activation (Kafka asynchrone) puis rafraîchit.
  const attendreActivation = async () => {
    let active = false
    for (let i = 0; i < 8 && !active; i++) {
      await sleep(1200)
      const s = await abonnementsApi.monStatut().catch(() => null)
      if (s?.actif || s?.statut === 'ACTIF') active = true
    }
    statut.reload(); histo.reload()
    return active
  }

  // Confirme le paiement d'un abonnement déjà EN_ATTENTE_PAIEMENT (créé avant, resté bloqué).
  const confirmerEnAttente = async () => {
    setPaying(true); setError('')
    try {
      const h = await abonnementsApi.monHistorique().catch(() => null)
      const abos = h?.abonnements || (Array.isArray(h) ? h : [])
      const pending = abos.filter((a) => a.statut === 'EN_ATTENTE_PAIEMENT').slice(-1)[0]
      if (!pending) { notify('Aucun abonnement en attente à confirmer.'); return }
      await abonnementsApi.simulerPaiement({ requestId: pending.requestId, statut: 'SUCCESS', modePaiement: pending.modePaiement || 'MOBILE_MONEY', reference: `MM-${pending.id}` })
      const active = await attendreActivation()
      notify(active ? 'Abonnement activé ✅' : 'Paiement pris en compte — activation en cours…')
    } catch (err) { notify(apiError(err, 'Confirmation impossible')) } finally { setPaying(false) }
  }

  const payer = async (e) => {
    e.preventDefault()
    setPaying(true); setError('')
    try {
      // 1) souscription → EN_ATTENTE_PAIEMENT
      const abo = await abonnementsApi.souscrire(plan.type, 'MOBILE_MONEY')
      // 2) confirmation du paiement (simulée côté back — pas de vraie passerelle)
      await abonnementsApi.simulerPaiement({ requestId: abo.requestId, statut: 'SUCCESS', modePaiement: 'MOBILE_MONEY', reference: `MM-${operateur}-${abo.id}` })
      // 3) scrutation jusqu'à activation
      const active = await attendreActivation()
      setPlan(null); setTel('')
      notify(active ? 'Abonnement activé ✅' : "Paiement pris en compte — activation en cours…")
    } catch (err) {
      setError(apiError(err, 'Le paiement a échoué.'))
    } finally { setPaying(false) }
  }

  return (
    <div className="view active">
      <div className="sub-banner">
        <div className="l">
          <span className="badge-sm">Abonnement</span>
          <span className="txt">
            {statut.loading ? 'Vérification…' : st?.actif
              ? <>Statut : <b>Actif</b> — {st.type}, échéance {fmt(st.dateFin)} ({st.joursRestants ?? '—'} j restants).</>
              : st?.statut
                ? <>Statut : <b>{ABO_STATUT_LABEL[st.statut] || st.statut}</b>.</>
                : <>Aucun abonnement — la publication de biens est indisponible.</>}
          </span>
        </div>
        {st?.statut === 'EN_ATTENTE_PAIEMENT' && (
          <a onClick={() => !paying && confirmerEnAttente()} style={{ cursor: paying ? 'default' : 'pointer' }}>
            {paying ? 'Confirmation…' : 'Confirmer le paiement'}
          </a>
        )}
      </div>

      <h3 className="serif" style={{ fontSize: 24, color: 'var(--navy)', margin: '10px 0 4px' }}>Choisir une formule</h3>
      <p className="muted" style={{ fontSize: 14 }}>Un abonnement actif est nécessaire pour publier des biens. Paiement via Mobile Money.</p>

      <div className="plans">
        {PLANS.map((p) => (
          <div className={`plan ${p.feat ? 'feat' : ''}`} key={p.type}>
            {p.feat && <span className="flag">Recommandé</span>}
            <div className="pt">{p.type}</div>
            <div className="pp serif">{fmtPrix(p.prix)} <small>FCFA</small></div>
            <div className="pv">par {p.periode}</div>
            <ul>{p.avantages.map((a) => <li key={a}>{a}</li>)}</ul>
            <button className={p.feat ? 'btn-block' : 'btn-outline'} style={{ marginTop: 0 }} onClick={() => { setError(''); setPlan(p) }}>Souscrire {p.type.toLowerCase()}</button>
          </div>
        ))}
      </div>

      <div className="panel-a" style={{ marginTop: 34 }}>
        <div className="ph"><h3 className="serif">Historique</h3></div>
        <AsyncState loading={histo.loading} error={histo.error} onRetry={histo.reload} empty={!historique.length} emptyTitle="Aucun paiement">
          <table className="tbl">
            <thead><tr><th>Date</th><th>Formule</th><th>Montant</th><th>Statut</th></tr></thead>
            <tbody>
              {historique.map((h, i) => (
                <tr key={h.id || i}>
                  <td>{fmt(h.dateDebut || h.createdAt || h.date)}</td>
                  <td>{h.type}</td>
                  <td>{fmtPrix(h.prix || h.montant)} FCFA</td>
                  <td><Badge cls={ABO_STATUT_BADGE[h.statut] || 'b-attente'}>{ABO_STATUT_LABEL[h.statut] || h.statut}</Badge></td>
                </tr>
              ))}
            </tbody>
          </table>
        </AsyncState>
      </div>

      {/* Paiement Mobile Money */}
      <Modal open={!!plan} onClose={() => !paying && setPlan(null)} title={`Abonnement ${plan?.type || ''}`} subtitle="Paiement via Mobile Money">
        {error && <div className="form-error">{error}</div>}
        <form onSubmit={payer}>
          <div className="row" style={{ borderBottom: '1px solid var(--line)' }}><span>Montant</span><b>{fmtPrix(plan?.prix)} FCFA</b></div>
          <p style={{ fontSize: 11, letterSpacing: '.14em', textTransform: 'uppercase', color: 'var(--muted)', margin: '18px 0 10px' }}>Opérateur</p>
          <div className="ops">
            <button type="button" className={`op orange ${operateur === 'ORANGE' ? 'on' : ''}`} onClick={() => setOperateur('ORANGE')}>Orange Money</button>
            <button type="button" className={`op mtn ${operateur === 'MTN' ? 'on' : ''}`} onClick={() => setOperateur('MTN')}>MTN MoMo</button>
          </div>
          <div className="field" style={{ marginTop: 16 }}><label>Numéro de téléphone</label><input value={tel} onChange={(e) => setTel(e.target.value)} placeholder="6XX XX XX XX" inputMode="tel" required /></div>
          <button className="btn-block" disabled={paying}>{paying ? 'Paiement en cours…' : `Payer ${fmtPrix(plan?.prix)} FCFA`}</button>
          <p className="center muted" style={{ fontSize: 12, marginTop: 12 }}>Paiement simulé (pas de passerelle Mobile Money réelle sur l'environnement de démo).</p>
        </form>
      </Modal>
    </div>
  )
}
