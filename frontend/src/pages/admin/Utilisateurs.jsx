import { useState } from 'react'
import { Badge } from '../../components/ui.jsx'
import { AsyncState } from '../../components/states.jsx'
import { useToast } from '../../components/toast-context.jsx'
import { useApi } from '../../hooks/useApi.js'
import { useAuth } from '../../context/AuthContext.jsx'
import { adminApi } from '../../api/admin.js'
import { biensApi } from '../../api/biens.js'
import { apiError } from '../../api/http.js'
import { buildRoleResolver } from '../../lib/roles.js'
import { ROLE_LABEL, ROLE_BADGE } from '../../lib/constants.js'

export default function AdminUtilisateurs() {
  const notify = useToast()
  const { user, roles } = useAuth()
  const [filtre, setFiltre] = useState('TOUS')

  const { data, loading, error, reload } = useApi(async () => {
    const [users, reqs, biens] = await Promise.all([
      adminApi.users(),
      adminApi.roleRequests().catch(() => []),
      biensApi.list().catch(() => []),
    ])
    const roleOf = buildRoleResolver({ roleRequests: reqs, biens, currentEmail: user?.email, currentRole: roles?.[0] })
    return { users: Array.isArray(users) ? users : [], roleOf }
  }, [user?.email])

  const users = data?.users || []
  const roleOf = data?.roleOf || (() => 'ROLE_CLIENT')
  const idOf = (u) => u.authUserId ?? u.id
  const actifOf = (u) => (u.actif ?? u.isActive ?? true)

  const toggle = async (u) => {
    const active = actifOf(u)
    try {
      await (active ? adminApi.deactivateUser(idOf(u)) : adminApi.reactivateUser(idOf(u)))
      notify(active ? 'Compte désactivé' : 'Compte réactivé'); reload()
    } catch (e) { notify(apiError(e, 'Action impossible')) }
  }

  const rolesFilter = ['TOUS', 'ROLE_CLIENT', 'ROLE_AGENT', 'ROLE_ADMIN', 'ROLE_SUPER_ADMIN']
  const visible = users
    .filter((u) => !u.isDeleted)
    .filter((u) => filtre === 'TOUS' || roleOf(u) === filtre)

  return (
    <div className="view active">
      <div className="pill-row" style={{ marginBottom: 20 }}>
        {rolesFilter.map((r) => <button key={r} className={`pill ${filtre === r ? 'on' : ''}`} onClick={() => setFiltre(r)}>{r === 'TOUS' ? 'Tous' : ROLE_LABEL[r]}</button>)}
      </div>
      <div className="panel-a">
        <div className="ph"><h3 className="serif">Utilisateurs ({visible.length})</h3></div>
        <AsyncState loading={loading} error={error} onRetry={reload} empty={!visible.length} emptyTitle="Aucun utilisateur">
          <table className="tbl">
            <thead><tr><th>Utilisateur</th><th>E-mail</th><th>Rôle</th><th>État</th><th>Actions</th></tr></thead>
            <tbody>
              {visible.map((u) => (
                <tr key={idOf(u)}>
                  <td><div className="bn serif" style={{ fontSize: 15 }}>{[u.firstname, u.lastname].filter(Boolean).join(' ') || u.nom || `#${idOf(u)}`}</div></td>
                  <td>{u.email}</td>
                  <td><Badge cls={ROLE_BADGE[roleOf(u)]}>{ROLE_LABEL[roleOf(u)]}</Badge></td>
                  <td><Badge cls={actifOf(u) ? 'b-dispo' : 'b-inactif'}>{actifOf(u) ? 'Actif' : 'Désactivé'}</Badge></td>
                  <td><button className="linklike" style={{ color: actifOf(u) ? '#B23A3A' : '#2E7D5B' }} onClick={() => toggle(u)}>{actifOf(u) ? 'Désactiver' : 'Réactiver'}</button></td>
                </tr>
              ))}
            </tbody>
          </table>
        </AsyncState>
      </div>
    </div>
  )
}
