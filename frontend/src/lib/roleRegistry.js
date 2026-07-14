// Registre local (localStorage) des rôles connus.
// /admin/users ne renvoie pas le rôle et il n'existe pas d'endpoint listant les
// admins. Quand le super-admin CRÉE ou INVITE un admin via l'UI, on mémorise ici
// son email → rôle, pour que la liste des utilisateurs/administrateurs l'affiche
// correctement. (Limite : par navigateur ; un vrai champ rôle côté back rendrait
// ce registre inutile.)
const KEY = 'immoapp_known_roles'

const read = () => { try { return JSON.parse(localStorage.getItem(KEY)) || {} } catch { return {} } }

export function setKnownRole(email, role) {
  if (!email || !role) return
  const m = read(); m[email.toLowerCase()] = role
  localStorage.setItem(KEY, JSON.stringify(m))
}

export function getKnownRoles() { return read() }

export function getKnownRole(email) { return email ? read()[email.toLowerCase()] : undefined }
