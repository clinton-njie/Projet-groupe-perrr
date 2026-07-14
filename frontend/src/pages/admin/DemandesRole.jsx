import { Loader, ErrorState, EmptyState } from '../../components/states.jsx'
import { Badge } from '../../components/ui.jsx'
import { useToast } from '../../components/toast-context.jsx'
import { useApi } from '../../hooks/useApi.js'
import { adminApi } from '../../api/admin.js'
import { apiError } from '../../api/http.js'

const RR_LABEL = { EN_ATTENTE: 'En attente', APPROUVEE: 'Approuvée', REFUSEE: 'Refusée' }
const RR_BADGE = { EN_ATTENTE: 'b-attente', APPROUVEE: 'b-paye', REFUSEE: 'b-inactif' }

export default function DemandesRole() {
  const notify = useToast()
  const { data, loading, error, reload } = useApi(async () => {
    try { return await adminApi.roleRequests() }
    catch (e) { if (e?.response?.status === 404) return { __unavailable: true }; throw e }
  }, [])

  const unavailable = data && data.__unavailable
  const list = Array.isArray(data) ? [...data].sort((a, b) => (a.statut === 'EN_ATTENTE' ? -1 : 1)) : []

  const traiter = async (id, decision) => {
    let refusalReason = null
    if (decision === 'REFUSER') refusalReason = prompt('Motif du refus (facultatif) :') || ''
    try {
      await adminApi.processRoleRequest(id, decision, refusalReason)
      notify(decision === 'ACCEPTER' ? 'Demande acceptée — utilisateur promu agent.' : 'Demande refusée.')
      reload()
    } catch (e) { notify(apiError(e, 'Traitement impossible')) }
  }

  return (
    <div className="view active">
      <div className="panel-a">
        <div className="ph"><h3 className="serif">Demandes de changement de rôle</h3></div>
        {loading ? <Loader /> : unavailable ? (
          <EmptyState title="Endpoint indisponible sur le back déployé"
            hint="Le service-admin en ligne n'expose pas GET /admin/role-requests." />
        ) : error ? <ErrorState error={error} onRetry={reload} /> : !list.length ? (
          <EmptyState title="Aucune demande" />
        ) : (
          <table className="tbl">
            <thead><tr><th>Demandeur</th><th>Rôle demandé</th><th>Justification</th><th>Statut</th><th>Action</th></tr></thead>
            <tbody>
              {list.map((r) => (
                <tr key={r.id}>
                  <td><div className="bn serif" style={{ fontSize: 15 }}>Utilisateur #{r.authUserId}</div><div className="bl">{(r.currentRole || 'ROLE_CLIENT').replace('ROLE_', '')}</div></td>
                  <td>{(r.requestedRole || 'ROLE_AGENT').replace('ROLE_', '')}</td>
                  <td><div className="justif" title={r.justification}>{r.justification}</div></td>
                  <td><Badge cls={RR_BADGE[r.statut] || 'b-attente'}>{RR_LABEL[r.statut] || r.statut}</Badge></td>
                  <td>
                    {r.statut === 'EN_ATTENTE' ? (
                      <span className="act-ic" style={{ gap: 14 }}>
                        <button className="linklike" style={{ color: '#2E7D5B' }} onClick={() => traiter(r.id, 'ACCEPTER')}>Accepter</button>
                        <button className="linklike" style={{ color: '#B23A3A' }} onClick={() => traiter(r.id, 'REFUSER')}>Refuser</button>
                      </span>
                    ) : <span className="muted" style={{ fontSize: 12 }}>Traitée</span>}
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        )}
      </div>
    </div>
  )
}
