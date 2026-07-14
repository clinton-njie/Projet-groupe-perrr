import { Link } from 'react-router-dom'
import AccountShell from '../../components/AccountShell.jsx'
import { Badge } from '../../components/ui.jsx'
import { AsyncState } from '../../components/states.jsx'
import { useToast } from '../../components/toast-context.jsx'
import { useApi } from '../../hooks/useApi.js'
import { useAuth } from '../../context/AuthContext.jsx'
import { reservationsApi } from '../../api/reservations.js'
import { apiError } from '../../api/http.js'
import { RESA_STATUT_LABEL, RESA_STATUT_BADGE, fmtPrix } from '../../lib/constants.js'

export default function MesReservations() {
  const { authUserId } = useAuth()
  const notify = useToast()
  const { data, loading, error, reload } = useApi(() => (authUserId ? reservationsApi.byUser(authUserId) : Promise.resolve([])), [authUserId])
  const list = Array.isArray(data) ? data : []

  const annuler = async (id) => {
    try { await reservationsApi.annuler(id); notify('Réservation annulée'); reload() }
    catch (e) { notify(apiError(e, 'Annulation impossible')) }
  }

  return (
    <AccountShell title="Mes réservations">
      <div className="panel-a">
        <div className="ph"><h3 className="serif">Réservations de location</h3></div>
        <AsyncState loading={loading} error={error} onRetry={reload}
          empty={!list.length} emptyTitle="Aucune réservation" emptyHint="Réservez un bien depuis sa fiche.">
          <table className="tbl">
            <thead><tr><th>Bien</th><th>Période</th><th>Montant</th><th>Statut</th><th></th></tr></thead>
            <tbody>
              {list.map((r) => (
                <tr key={r.id}>
                  <td><Link to={`/biens/${r.propertyId}`} className="bn serif" style={{ color: 'var(--navy)' }}>{r.nomBien || `Bien #${r.propertyId}`}</Link></td>
                  <td>{r.startDate} → {r.endDate}</td>
                  <td>{fmtPrix(r.amount)} FCFA</td>
                  <td><Badge cls={RESA_STATUT_BADGE[r.status] || 'b-attente'}>{RESA_STATUT_LABEL[r.status] || r.status}</Badge></td>
                  <td>{r.status === 'EN_ATTENTE' && <button className="linklike muted" onClick={() => annuler(r.id)}>Annuler</button>}</td>
                </tr>
              ))}
            </tbody>
          </table>
        </AsyncState>
      </div>
      <p className="muted" style={{ fontSize: 12.5, marginTop: 14 }}>Montant = loyer mensuel × durée, calculé par le serveur. Réglé via Mobile Money — <b>paiement non remboursable</b>.</p>
    </AccountShell>
  )
}
