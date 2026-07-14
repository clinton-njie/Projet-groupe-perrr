import { Badge } from '../../components/ui.jsx'
import { AsyncState } from '../../components/states.jsx'
import { useToast } from '../../components/toast-context.jsx'
import { useApi } from '../../hooks/useApi.js'
import { useAuth } from '../../context/AuthContext.jsx'
import { visitesApi } from '../../api/visites.js'
import { apiError } from '../../api/http.js'
import { VISITE_STATUT_LABEL, VISITE_STATUT_BADGE } from '../../lib/constants.js'

const fmt = (s) => (s ? new Date(s).toLocaleString('fr-FR', { dateStyle: 'medium', timeStyle: 'short' }) : '—')

export default function AgentVisites() {
  const { user } = useAuth()
  const notify = useToast()
  const { data, loading, error, reload } = useApi(() => visitesApi.byAgent(user.email), [user.email])
  const list = Array.isArray(data) ? data : []

  const act = async (fn, id, msg) => {
    try { await fn(id); notify(msg); reload() } catch (e) { notify(apiError(e, 'Action impossible')) }
  }

  return (
    <div className="view active">
      <div className="sub-banner" style={{ background: 'var(--paper)', border: '1px solid var(--line)', color: 'var(--ink)' }}>
        <div className="l"><span className="txt" style={{ color: 'var(--ink)' }}>
          ⓘ La publication de vos <b>plages de disponibilité</b> n'est pas encore activée côté serveur. En attendant, les clients proposent un horaire et vous <b>confirmez</b> chaque visite ci-dessous.
        </span></div>
      </div>
      <div className="panel-a">
        <div className="ph"><h3 className="serif">Visites sur mes biens</h3></div>
        <AsyncState loading={loading} error={error} onRetry={reload} empty={!list.length} emptyTitle="Aucune visite">
          <table className="tbl">
            <thead><tr><th>Client</th><th>Bien</th><th>Début</th><th>Statut</th><th>Actions</th></tr></thead>
            <tbody>
              {list.map((v) => (
                <tr key={v.id}>
                  <td>{v.clientEmail}</td>
                  <td>{v.nomBien || `Bien #${v.bienId}`}</td>
                  <td>{fmt(v.dateHeureDebut)}</td>
                  <td><Badge cls={VISITE_STATUT_BADGE[v.statut]}>{VISITE_STATUT_LABEL[v.statut]}</Badge></td>
                  <td>
                    <span className="act-ic" style={{ gap: 14 }}>
                      {v.statut === 'PAYEE' && <button className="linklike" style={{ color: 'var(--gold-deep)' }} onClick={() => act(visitesApi.confirmer, v.id, 'Visite confirmée')}>Confirmer</button>}
                      {(v.statut === 'PAYEE' || v.statut === 'EN_COURS') && <button className="linklike" style={{ color: '#2E7D5B' }} onClick={() => act(visitesApi.terminer, v.id, 'Visite terminée')}>Terminer</button>}
                      {(v.statut === 'PAYEE' || v.statut === 'EN_COURS') && <button className="linklike" style={{ color: '#B23A3A' }} onClick={() => act(visitesApi.nonPresente, v.id, 'Client marqué non présenté')}>Absent</button>}
                    </span>
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        </AsyncState>
      </div>
    </div>
  )
}
