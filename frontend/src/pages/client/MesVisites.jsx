import { useState } from 'react'
import { Link } from 'react-router-dom'
import AccountShell from '../../components/AccountShell.jsx'
import { Badge } from '../../components/ui.jsx'
import { AsyncState } from '../../components/states.jsx'
import { NotationModal } from '../../components/modals.jsx'
import { useToast } from '../../components/toast-context.jsx'
import { useApi } from '../../hooks/useApi.js'
import { useAuth } from '../../context/AuthContext.jsx'
import { visitesApi } from '../../api/visites.js'
import { avisApi } from '../../api/avis.js'
import { apiError } from '../../api/http.js'
import { VISITE_STATUT_LABEL, VISITE_STATUT_BADGE } from '../../lib/constants.js'

const fmt = (s) => (s ? new Date(s).toLocaleString('fr-FR', { dateStyle: 'medium', timeStyle: 'short' }) : '—')

export default function MesVisites() {
  const { user, authUserId } = useAuth()
  const notify = useToast()
  const [noter, setNoter] = useState(null)

  const visites = useApi(() => visitesApi.byUser(user.email), [user.email])
  const opps = useApi(() => (authUserId ? avisApi.opportunites(authUserId) : Promise.resolve([])), [authUserId])
  const list = Array.isArray(visites.data) ? visites.data : []
  const opportunites = Array.isArray(opps.data) ? opps.data : []

  const annuler = async (id) => {
    try { await visitesApi.annuler(id); notify('Visite annulée'); visites.reload() }
    catch (e) { notify(apiError(e, 'Annulation impossible')) }
  }

  return (
    <AccountShell title="Mes visites">
      {opportunites.length > 0 && (
        <div className="sub-banner" style={{ background: 'var(--gold)', color: 'var(--navy-deep)' }}>
          <div className="l"><span className="txt" style={{ color: 'var(--navy-deep)' }}><b>{opportunites.length}</b> transaction(s) à noter.</span></div>
          <a onClick={() => setNoter(opportunites[0])} style={{ color: 'var(--navy-deep)', borderColor: 'var(--navy-deep)' }}>Noter maintenant</a>
        </div>
      )}

      <div className="panel-a">
        <div className="ph"><h3 className="serif">Visites planifiées & passées</h3></div>
        <AsyncState loading={visites.loading} error={visites.error} onRetry={visites.reload}
          empty={!list.length} emptyTitle="Aucune visite" emptyHint="Réservez une visite depuis la fiche d'un bien.">
          <table className="tbl">
            <thead><tr><th>Bien</th><th>Début</th><th>Statut</th><th></th></tr></thead>
            <tbody>
              {list.map((v) => (
                <tr key={v.id}>
                  <td><Link to={`/biens/${v.bienId}`} className="bn serif" style={{ color: 'var(--navy)' }}>{v.nomBien || `Bien #${v.bienId}`}</Link><div className="bl">{v.agentEmail}</div></td>
                  <td>{fmt(v.dateHeureDebut)}</td>
                  <td><Badge cls={VISITE_STATUT_BADGE[v.statut] || 'b-attente'}>{VISITE_STATUT_LABEL[v.statut] || v.statut}</Badge></td>
                  <td>
                    {/* Annulation possible uniquement tant que la visite n'est pas payée (aucun remboursement). */}
                    {v.statut === 'EN_ATTENTE' && (
                      <button className="linklike muted" onClick={() => annuler(v.id)}>Annuler</button>
                    )}
                    {v.statut === 'PAYEE' && <span className="muted" style={{ fontSize: 12 }}>Payée · non remboursable</span>}
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        </AsyncState>
      </div>
      <p className="muted" style={{ fontSize: 12.5, marginTop: 14 }}>Le paiement d'une visite n'est pas remboursable. Seule une visite non encore payée peut être annulée.</p>

      <NotationModal open={!!noter} onClose={() => setNoter(null)} opportunite={noter} onDone={() => opps.reload()} />
    </AccountShell>
  )
}
