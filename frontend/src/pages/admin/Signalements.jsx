import { useState } from 'react'
import { Badge, Modal } from '../../components/ui.jsx'
import { AsyncState } from '../../components/states.jsx'
import { useToast } from '../../components/toast-context.jsx'
import { useApi } from '../../hooks/useApi.js'
import { adminApi } from '../../api/admin.js'
import { apiError } from '../../api/http.js'
import { SIGNALEMENT_STATUT_LABEL, SIGNALEMENT_STATUT_BADGE } from '../../lib/constants.js'

const objetOf = (s) => s.objetSignalement?.libelle || s.objet || s.libelle || s.motif || '—'

export default function AdminSignalements() {
  const notify = useToast()
  const [sel, setSel] = useState(null)
  const { data, loading, error, reload } = useApi(() => adminApi.signalements(), [])
  const list = Array.isArray(data) ? data : []

  const traiter = async (id, statut, msg) => {
    try { await adminApi.updateSignalementStatut(id, statut); setSel(null); notify(msg); reload() }
    catch (e) { notify(apiError(e, 'Traitement impossible')) }
  }

  return (
    <div className="view active">
      <div className="panel-a">
        <div className="ph"><h3 className="serif">Signalements</h3></div>
        <AsyncState loading={loading} error={error} onRetry={reload} empty={!list.length} emptyTitle="Aucun signalement">
          <table className="tbl">
            <thead><tr><th>Type</th><th>Objet</th><th>Description</th><th>Statut</th><th></th></tr></thead>
            <tbody>
              {list.map((s) => (
                <tr key={s.id}>
                  <td><Badge cls={s.type === 'AGENT' ? 'b-role-agent' : 'b-attente'}>{s.type}</Badge></td>
                  <td>{objetOf(s)}</td>
                  <td><div className="justif" title={s.description}>{s.description}</div></td>
                  <td><Badge cls={SIGNALEMENT_STATUT_BADGE[s.statut] || 'b-attente'}>{SIGNALEMENT_STATUT_LABEL[s.statut] || s.statut}</Badge></td>
                  <td>{s.statut === 'EN_ATTENTE' && <button className="linklike" style={{ color: 'var(--gold-deep)' }} onClick={() => setSel(s)}>Examiner</button>}</td>
                </tr>
              ))}
            </tbody>
          </table>
        </AsyncState>
      </div>

      <Modal open={!!sel} onClose={() => setSel(null)} title="Traiter le signalement" subtitle={sel ? `${sel.type} · ${objetOf(sel)}` : ''}>
        {sel && (
          <>
            {sel.agentId != null && <div className="row"><span>Agent visé</span><b>#{sel.agentId}</b></div>}
            <p style={{ fontSize: 14, color: 'var(--ink)', margin: '18px 0', lineHeight: 1.6 }}>“{sel.description}”</p>
            <button className="btn-block" style={{ background: '#B23A3A', borderColor: '#B23A3A' }}
              onClick={() => traiter(sel.id, 'TERMINE_AVEC_SANCTION', 'Sanction appliquée.')}>Sanctionner l'agent</button>
            <button className="btn-outline" onClick={() => traiter(sel.id, 'TERMINE_SANS_SANCTION', 'Classé sans suite.')}>Classer sans suite</button>
          </>
        )}
      </Modal>
    </div>
  )
}
