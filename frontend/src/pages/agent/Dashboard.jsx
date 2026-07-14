import { Link } from 'react-router-dom'
import { Badge } from '../../components/ui.jsx'
import { AsyncState } from '../../components/states.jsx'
import { BarChart, Donut } from '../../components/BarChart.jsx'
import { useApi } from '../../hooks/useApi.js'
import { useAuth } from '../../context/AuthContext.jsx'
import { biensApi } from '../../api/biens.js'
import { visitesApi } from '../../api/visites.js'
import { abonnementsApi } from '../../api/abonnements.js'
import { STATUT_LABEL, STATUT_BADGE, VISITE_STATUT_LABEL, VISITE_STATUT_BADGE, CATEGORIE_LABEL, fmtPrix } from '../../lib/constants.js'

const fmt = (s) => (s ? new Date(s).toLocaleDateString('fr-FR', { dateStyle: 'medium' }) : '—')
const countBy = (arr, key) => {
  const m = {}
  arr.forEach((x) => { const k = typeof key === 'function' ? key(x) : x[key]; if (k) m[k] = (m[k] || 0) + 1 })
  return m
}

export default function AgentDashboard() {
  const { user } = useAuth()
  const biens = useApi(() => biensApi.mesBiens(), [])
  const visites = useApi(() => visitesApi.byAgent(user.email), [user.email])
  const abo = useApi(() => abonnementsApi.monStatut(), [])

  const mesBiens = Array.isArray(biens.data) ? biens.data : []
  const mesVisites = Array.isArray(visites.data) ? visites.data : []
  const statut = abo.data

  const parVille = Object.entries(countBy(mesBiens, 'ville')).map(([label, value]) => ({ label, value })).sort((a, b) => b.value - a.value)
  const STAT_COLORS = { DISPONIBLE: '#2E7D5B', RESERVE: '#DEA23C', LOUE: '#102C5C' }
  const parStatut = Object.entries(countBy(mesBiens, 'statut')).map(([k, value]) => ({ label: STATUT_LABEL[k] || k, value, color: STAT_COLORS[k] || '#847C6E' }))
  const parCat = Object.entries(countBy(mesBiens, 'categorie')).map(([k, value]) => ({ label: CATEGORIE_LABEL[k] || k, value })).sort((a, b) => b.value - a.value)

  const stats = [
    { k: 'Biens publiés', v: mesBiens.length },
    { k: 'Visites', v: mesVisites.length },
    { k: 'Disponibles', v: mesBiens.filter((b) => b.statut === 'DISPONIBLE').length },
    { k: 'Abonnement', v: statut?.actif ? 'Actif' : '—' },
  ]

  return (
    <div className="view active">
      <div className="sub-banner">
        <div className="l">
          <span className="badge-sm">Abonnement</span>
          <span className="txt">
            {abo.loading ? 'Vérification…' : statut?.actif
              ? <>Actif — <b>{statut.type}</b>, {statut.joursRestants ?? '—'} jours restants.</>
              : <>Aucun abonnement actif — <b>publication indisponible</b>.</>}
          </span>
        </div>
        <Link to="/agent/abonnement">Gérer l'abonnement</Link>
      </div>

      <div className="stat-grid">
        {stats.map((s) => <div className="stat" key={s.k}><div className="k">{s.k}</div><div className="v serif">{s.v}</div></div>)}
      </div>

      {/* Graphiques (calculés côté client) */}
      {mesBiens.length > 0 && (
        <div className="dash-2" style={{ marginBottom: 24 }}>
          <div className="panel-a" style={{ padding: 24 }}>
            <div className="ph" style={{ padding: 0, border: 'none', marginBottom: 18 }}><h3 className="serif" style={{ fontSize: 18 }}>Mes biens par ville</h3></div>
            <BarChart data={parVille} />
          </div>
          <div className="panel-a" style={{ padding: 24 }}>
            <div className="ph" style={{ padding: 0, border: 'none', marginBottom: 18 }}><h3 className="serif" style={{ fontSize: 18 }}>Répartition par statut</h3></div>
            <Donut data={parStatut} />
            <div style={{ marginTop: 18 }}><BarChart data={parCat} accent="var(--navy)" /></div>
          </div>
        </div>
      )}

      <div className="dash-2">
        <div className="panel-a">
          <div className="ph"><h3 className="serif">Mes biens récents</h3><Link to="/agent/biens">Tout voir</Link></div>
          <AsyncState loading={biens.loading} error={biens.error} onRetry={biens.reload} empty={!mesBiens.length} emptyTitle="Aucun bien" emptyHint="Publiez votre premier bien.">
            <table className="tbl">
              <thead><tr><th>Bien</th><th>Prix</th><th>Statut</th></tr></thead>
              <tbody>
                {mesBiens.slice(0, 5).map((b) => (
                  <tr key={b.id}>
                    <td><Link to={`/biens/${b.id}`} className="bn serif" style={{ color: 'var(--navy)' }}>{b.nom}</Link><div className="bl">{b.quartier}, {b.ville}</div></td>
                    <td>{fmtPrix(b.prixMensuel)} FCFA</td>
                    <td><Badge cls={STATUT_BADGE[b.statut]}>{STATUT_LABEL[b.statut]}</Badge></td>
                  </tr>
                ))}
              </tbody>
            </table>
          </AsyncState>
        </div>

        <div className="panel-a">
          <div className="ph"><h3 className="serif">Visites</h3><Link to="/agent/visites">Tout voir</Link></div>
          <AsyncState loading={visites.loading} error={visites.error} onRetry={visites.reload} empty={!mesVisites.length} emptyTitle="Aucune visite">
            {mesVisites.slice(0, 5).map((v) => (
              <div className="req" key={v.id}>
                <div><div className="rn">{v.nomBien || `Bien #${v.bienId}`}</div><div className="rt">{v.clientEmail}</div></div>
                <div className="rtime">{fmt(v.dateHeureDebut)}<br /><Badge cls={VISITE_STATUT_BADGE[v.statut]}>{VISITE_STATUT_LABEL[v.statut]}</Badge></div>
              </div>
            ))}
          </AsyncState>
        </div>
      </div>
    </div>
  )
}
