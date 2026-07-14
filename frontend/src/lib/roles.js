// Résolution du rôle d'un utilisateur côté admin.
// /admin/users (service-user) ne renvoie PAS le rôle. On le déduit :
//  1. l'utilisateur courant → son vrai rôle (JWT) ;
//  2. propriétaire d'au moins un bien → AGENT ;
//  3. demande de rôle APPROUVÉE → rôle demandé ;
//  4. sinon → CLIENT.
import { getKnownRoles } from './roleRegistry.js'

export function buildRoleResolver({ roleRequests = [], biens = [], currentEmail, currentRole } = {}) {
  const roleMap = {}
  ;(Array.isArray(roleRequests) ? roleRequests : [])
    .filter((r) => r.statut === 'APPROUVEE')
    .sort((a, b) => new Date(a.processedAt || 0) - new Date(b.processedAt || 0))
    .forEach((r) => { roleMap[r.authUserId] = r.requestedRole })
  const agentEmails = new Set((Array.isArray(biens) ? biens : []).map((b) => b.agentEmail).filter(Boolean))
  const known = getKnownRoles() // email(lowercase) -> role (admins créés/invités via l'UI)

  return (u) => {
    const id = u.authUserId ?? u.id
    const email = u.email
    if (currentEmail && email === currentEmail && currentRole) return currentRole
    if (u.role) return u.role
    if (u.roles && u.roles[0]) return u.roles[0]
    if (email && known[email.toLowerCase()]) return known[email.toLowerCase()]
    if (email && agentEmails.has(email)) return 'ROLE_AGENT'
    if (roleMap[id]) return roleMap[id]
    return 'ROLE_CLIENT'
  }
}
