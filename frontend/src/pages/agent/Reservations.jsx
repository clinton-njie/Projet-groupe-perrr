import { Badge } from '../../components/ui.jsx'
import { AsyncState } from '../../components/states.jsx'
import { useApi } from '../../hooks/useApi.js'
import { reservationsApi } from '../../api/reservations.js'
import { RESA_STATUT_LABEL, RESA_STATUT_BADGE, fmtPrix } from '../../lib/constants.js'

// GET /api/reservations/agent → réservations reçues par l'agent courant.
export default function AgentReservations() {
  const { data, loading, error, reload } = useApi(() => reservationsApi.agent(), [])
  const list = Array.isArray(data) ? data : []

  return (
    <div className="view active">
      <div className="panel-a">
        <div className="ph"><h3 className="serif">Réservations reçues</h3></div>
        <AsyncState loading={loading} error={error} onRetry={reload} empty={!list.length}
          emptyTitle="Aucune réservation" emptyHint="Les réservations sur vos biens apparaîtront ici.">
          <table className="tbl">
            <thead><tr><th>Client</th><th>Bien</th><th>Période</th><th>Montant</th><th>Statut</th></tr></thead>
            <tbody>
              {list.map((r) => (
                <tr key={r.id}>
                  <td>{r.nomUtilisateur || `Utilisateur #${r.userId}`}</td>
                  <td>{r.nomBien || `Bien #${r.propertyId}`}</td>
                  <td>{r.startDate} → {r.endDate}</td>
                  <td>{fmtPrix(r.amount)} FCFA</td>
                  <td><Badge cls={RESA_STATUT_BADGE[r.status] || 'b-attente'}>{RESA_STATUT_LABEL[r.status] || r.status}</Badge></td>
                </tr>
              ))}
            </tbody>
          </table>
        </AsyncState>
      </div>
      <p className="muted" style={{ fontSize: 12.5, marginTop: 14 }}>La réservation est confirmée automatiquement dès que le paiement Mobile Money est validé.</p>
    </div>
  )
}
