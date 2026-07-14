import { useState } from 'react'
import { Badge, Modal } from '../../components/ui.jsx'
import { AsyncState } from '../../components/states.jsx'
import { useToast } from '../../components/toast-context.jsx'
import { useApi } from '../../hooks/useApi.js'
import { useAuth } from '../../context/AuthContext.jsx'
import { adminApi } from '../../api/admin.js'
import { biensApi } from '../../api/biens.js'
import { apiError } from '../../api/http.js'
import { buildRoleResolver } from '../../lib/roles.js'
import { setKnownRole } from '../../lib/roleRegistry.js'
import { ROLE_LABEL, ROLE_BADGE } from '../../lib/constants.js'

const ADMIN_ROLES = ['ROLE_ADMIN', 'ROLE_SUPER_ADMIN']

export default function AdminAdministrateurs() {
  const notify = useToast()
  const { user, roles, isSuperAdmin } = useAuth()
  const [open, setOpen] = useState(false)
  const [email, setEmail] = useState('')
  const [busy, setBusy] = useState(false)
  const [lastLink, setLastLink] = useState(null)

  const { data, loading, error, reload } = useApi(async () => {
    const [users, reqs, biens] = await Promise.all([
      adminApi.users(), adminApi.roleRequests().catch(() => []), biensApi.list().catch(() => []),
    ])
    const roleOf = buildRoleResolver({ roleRequests: reqs, biens, currentEmail: user?.email, currentRole: roles?.[0] })
    return { users: Array.isArray(users) ? users : [], roleOf }
  }, [user?.email])

  const roleOf = data?.roleOf || (() => '')
  const admins = (data?.users || []).filter((u) => !u.isDeleted && ADMIN_ROLES.includes(roleOf(u)))
  const idOf = (u) => u.authUserId ?? u.id
  const actifOf = (u) => (u.actif ?? u.isActive ?? true)

  const inviter = async (e) => {
    e.preventDefault()
    setBusy(true)
    try {
      const res = await adminApi.invite(email)
      // Le back renvoie inviteLink = http://.../creer-compte-admin?token=<uuid>
      const raw = res?.inviteLink || res?.link || res?.url || ''
      const m = String(raw).match(/token=([^&]+)/)
      const token = res?.token || res?.inviteToken || (m && m[1])
      const link = token ? `${window.location.origin}/invitation/${token}` : (raw || null)
      setKnownRole(email, 'ROLE_ADMIN') // l'invité deviendra admin → l'afficher dans la liste
      setLastLink(link)
      setEmail('')
      notify('Invitation envoyée'); reload()
    } catch (err) { notify(apiError(err, "L'invitation a échoué")) } finally { setBusy(false) }
  }

  const toggle = async (u) => {
    const active = actifOf(u)
    try {
      await (active ? adminApi.deactivateUser(idOf(u)) : adminApi.reactivateUser(idOf(u)))
      notify(active ? 'Administrateur désactivé' : 'Administrateur réactivé'); reload()
    } catch (e) { notify(apiError(e, 'Action impossible')) }
  }

  return (
    <div className="view active">
      <div className="panel-a">
        <div className="ph">
          <h3 className="serif">Administrateurs</h3>
          <button className="btn-pub" onClick={() => { setLastLink(null); setOpen(true) }}>+ Inviter un admin</button>
        </div>
        <AsyncState loading={loading} error={error} onRetry={reload} empty={!admins.length} emptyTitle="Aucun administrateur">
          <table className="tbl">
            <thead><tr><th>Nom</th><th>E-mail</th><th>Rôle</th><th>État</th>{isSuperAdmin && <th>Actions</th>}</tr></thead>
            <tbody>
              {admins.map((a) => {
                const self = a.email === user?.email
                return (
                  <tr key={idOf(a)}>
                    <td><div className="bn serif" style={{ fontSize: 15 }}>{[a.firstname, a.lastname].filter(Boolean).join(' ') || a.nom || '—'}{self && <span className="muted" style={{ fontSize: 12 }}> (vous)</span>}</div></td>
                    <td>{a.email}</td>
                    <td><Badge cls={ROLE_BADGE[roleOf(a)]}>{ROLE_LABEL[roleOf(a)]}</Badge></td>
                    <td><Badge cls={actifOf(a) ? 'b-dispo' : 'b-inactif'}>{actifOf(a) ? 'Actif' : 'Désactivé'}</Badge></td>
                    {isSuperAdmin && (
                      <td>
                        {self ? <span className="muted" style={{ fontSize: 12 }}>—</span>
                          : <button className="linklike" style={{ color: actifOf(a) ? '#B23A3A' : '#2E7D5B' }} onClick={() => toggle(a)}>{actifOf(a) ? 'Désactiver' : 'Réactiver'}</button>}
                      </td>
                    )}
                  </tr>
                )
              })}
            </tbody>
          </table>
        </AsyncState>
      </div>

      <Modal open={open} onClose={() => setOpen(false)} title="Inviter un administrateur" subtitle="Réservé au super-administrateur">
        <form onSubmit={inviter}>
          <div className="field"><label>Adresse e-mail</label><input type="email" required value={email} onChange={(e) => setEmail(e.target.value)} placeholder="nouvel.admin@immoapp.com" /></div>
          {lastLink && (<><p className="muted" style={{ fontSize: 12, margin: '4px 0' }}>Lien d'invitation :</p><div className="invite-link">{lastLink}</div></>)}
          <button className="btn-block" type="submit" disabled={busy}>{busy ? 'Envoi…' : "Envoyer l'invitation"}</button>
        </form>
      </Modal>
    </div>
  )
}
