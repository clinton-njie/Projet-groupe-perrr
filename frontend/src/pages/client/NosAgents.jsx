import { Link } from 'react-router-dom'
import { Avatar, isRealImg } from '../../components/ui.jsx'
import { AsyncState } from '../../components/states.jsx'
import { useApi } from '../../hooks/useApi.js'
import { biensApi } from '../../api/biens.js'
import { usersApi } from '../../api/users.js'

export default function NosAgents() {
  const { data, loading, error, reload } = useApi(async () => {
    const biens = await biensApi.list()
    const count = {}
    ;(Array.isArray(biens) ? biens : []).forEach((b) => { if (b.agentEmail) count[b.agentEmail] = (count[b.agentEmail] || 0) + 1 })
    const agents = await Promise.all(Object.keys(count).map(async (email) => {
      try { const id = await usersApi.idByEmail(email); const p = await usersApi.getProfile(id); return { email, id, profile: p, count: count[email] } }
      catch { return { email, profile: null, count: count[email] } }
    }))
    return agents.sort((a, b) => b.count - a.count)
  }, [])

  const agents = Array.isArray(data) ? data : []

  return (
    <div className="page active client-inner">
      <div className="inner">
        <div className="wrap">
          <div className="crumb"><Link to="/">Accueil</Link> / <b>Nos agents</b></div>
          <span className="eyebrow">Le réseau ImmoAPP</span>
          <h1 className="page-title serif" style={{ marginTop: 12, marginBottom: 30 }}>Nos agents</h1>

          <AsyncState loading={loading} error={error} onRetry={reload} empty={!agents.length} emptyTitle="Aucun agent pour l'instant">
            <div className="mini-grid">
              {agents.map((a) => {
                const nom = a.profile ? `${a.profile.firstname || ''} ${a.profile.lastname || ''}`.trim() || a.email : a.email
                return (
                  <div className="agent-card" key={a.email}>
                    {isRealImg(a.profile?.photoProfil)
                      ? <div className="agent-card-av" style={{ backgroundImage: `url(${a.profile.photoProfil})` }} />
                      : <div className="agent-card-av"><Avatar seed={a.email} size={92} /></div>}
                    <div className="agent-card-name serif">{nom}</div>
                    <div className="agent-card-meta">{a.profile?.ville || 'Cameroun'} · {a.count} bien{a.count > 1 ? 's' : ''}</div>
                    <Link className="btn-outline btn-sm" to={`/agents/${encodeURIComponent(a.email)}`} style={{ marginTop: 12 }}>Consulter ses biens</Link>
                  </div>
                )
              })}
            </div>
          </AsyncState>
        </div>
      </div>
    </div>
  )
}
