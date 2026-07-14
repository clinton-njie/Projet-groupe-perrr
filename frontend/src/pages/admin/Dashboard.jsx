import { Link } from 'react-router-dom'
import { Badge } from '../../components/ui.jsx'
import { AsyncState } from '../../components/states.jsx'
import { BarChart, Donut } from '../../components/BarChart.jsx'
import { useApi } from '../../hooks/useApi.js'
import { adminApi } from '../../api/admin.js'
import { biensApi } from '../../api/biens.js'
import { SIGNALEMENT_STATUT_LABEL, SIGNALEMENT_STATUT_BADGE, CATEGORIE_LABEL, ROLE_LABEL } from '../../lib/constants.js'

const countBy = (arr, key) => {
  const m = {}
  arr.forEach((x) => { const k = typeof key === 'function' ? key(x) : x[key]; if (k) m[k] = (m[k] || 0) + 1 })
  return m
}

export default function AdminDashboard() {
  const users = useApi(() => adminApi.users().catch(() => []), [])
  const roleReqs = useApi(() => adminApi.roleRequests().catch(() => []), [])
  const signalements = useApi(() => adminApi.signalements().catch(() => []), [])
  const biens = useApi(() => biensApi.list().catch(() => []), [])

  const u = (Array.isArray(users.data) ? users.data : []).filter((x) => !x.isDeleted)
  const rr = Array.isArray(roleReqs.data) ? roleReqs.data : []
  const sg = Array.isArray(signalements.data) ? signalements.data : []
  const bn = Array.isArray(biens.data) ? biens.data : []

  const stats = [
    { k: 'Utilisateurs', v: u.length },
    { k: 'Biens', v: bn.length },
    { k: 'Demandes de rôle', v: rr.filter((r) => r.statut === 'EN_ATTENTE').length },
    { k: 'Signalements', v: sg.filter((s) => s.statut === 'EN_ATTENTE').length },
  ]

  const parVille = Object.entries(countBy(bn, 'ville')).map(([label, value]) => ({ label, value })).sort((a, b) => b.value - a.value).slice(0, 8)
  const CAT_COLORS = { APPARTEMENT: '#3B82F6', VILLA: '#102C5C', CITE: '#DEA23C', TERRAIN: '#2E7D5B' }
  const parCat = Object.entries(countBy(bn, 'categorie')).map(([k, value]) => ({ label: CATEGORIE_LABEL[k] || k, value, color: CAT_COLORS[k] || '#847C6E' }))

  return (
    <div className="view active">
      <div className="stat-grid">
        {stats.map((s) => <div className="stat" key={s.k}><div className="k">{s.k}</div><div className="v serif">{s.v}</div></div>)}
      </div>

      {/* Statistiques visuelles (calculées côté client) */}
      <div className="dash-2" style={{ marginBottom: 24 }}>
        <div className="panel-a" style={{ padding: 24 }}>
          <div className="ph" style={{ padding: 0, border: 'none', marginBottom: 18 }}><h3 className="serif" style={{ fontSize: 18 }}>Biens par ville</h3></div>
          <BarChart data={parVille} />
        </div>
        <div className="panel-a" style={{ padding: 24 }}>
          <div className="ph" style={{ padding: 0, border: 'none', marginBottom: 18 }}><h3 className="serif" style={{ fontSize: 18 }}>Biens par catégorie</h3></div>
          <Donut data={parCat} />
        </div>
      </div>

      <div className="dash-2">
        <div className="panel-a">
          <div className="ph"><h3 className="serif">Derniers signalements</h3><Link to="/admin/signalements">Tout voir</Link></div>
          <AsyncState loading={signalements.loading} error={signalements.error} onRetry={signalements.reload} empty={!sg.length} emptyTitle="Aucun signalement">
            <table className="tbl">
              <thead><tr><th>Type</th><th>Objet</th><th>Statut</th></tr></thead>
              <tbody>
                {sg.slice(0, 5).map((s) => (
                  <tr key={s.id}>
                    <td>{s.type}</td>
                    <td>{s.objetSignalement?.libelle || s.objet || '—'}</td>
                    <td><Badge cls={SIGNALEMENT_STATUT_BADGE[s.statut] || 'b-attente'}>{SIGNALEMENT_STATUT_LABEL[s.statut] || s.statut}</Badge></td>
                  </tr>
                ))}
              </tbody>
            </table>
          </AsyncState>
        </div>

        <div className="panel-a">
          <div className="ph"><h3 className="serif">Demandes de rôle</h3><Link to="/admin/demandes-role">Traiter</Link></div>
          <AsyncState loading={roleReqs.loading} error={roleReqs.error} onRetry={roleReqs.reload} empty={!rr.length} emptyTitle="Aucune demande">
            {rr.slice(0, 5).map((r) => (
              <div className="req" key={r.id}>
                <div><div className="rn">{r.nom || r.email || `Utilisateur #${r.authUserId}`}</div><div className="rt">{r.currentRole || 'CLIENT'} → {r.requestedRole || 'AGENT'}</div></div>
              </div>
            ))}
          </AsyncState>
        </div>
      </div>
    </div>
  )
}
