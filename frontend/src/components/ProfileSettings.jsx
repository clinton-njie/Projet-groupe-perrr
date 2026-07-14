import { useState, useEffect, useRef } from 'react'
import { Avatar, isRealImg } from './ui.jsx'
import { Loader } from './states.jsx'
import { useToast } from './toast-context.jsx'
import { useAuth } from '../context/AuthContext.jsx'
import { usersApi } from '../api/users.js'
import { biensApi } from '../api/biens.js'
import { apiError } from '../api/http.js'
import { ROLE_LABEL } from '../lib/constants.js'

const FIELDS = ['telephone', 'adresse', 'ville', 'codePostal', 'pays', 'bio']

// Panneau « Mon profil » réutilisable (agent, admin, ou ailleurs).
export default function ProfileSettings() {
  const notify = useToast()
  const fileRef = useRef(null)
  const { user, profile, authUserId, roles, refreshProfile } = useAuth()
  const [form, setForm] = useState(null)
  const [busy, setBusy] = useState(false)
  const [uploading, setUploading] = useState(false)

  useEffect(() => { if (profile) setForm(Object.fromEntries(FIELDS.map((k) => [k, profile[k] || '']))) }, [profile])
  if (!profile || !form) return <Loader label="Chargement du profil…" />

  const set = (k) => (e) => setForm((f) => ({ ...f, [k]: e.target.value }))

  const save = async () => {
    setBusy(true)
    try { await usersApi.updateProfile(authUserId, { ...form, photoProfil: profile.photoProfil || null }); await refreshProfile(); notify('Profil enregistré') }
    catch (e) { notify(apiError(e, "Échec de l'enregistrement")) } finally { setBusy(false) }
  }

  const onPickPhoto = async (e) => {
    const file = e.target.files?.[0]; if (!file) return
    setUploading(true)
    try {
      const { secureUrl } = await biensApi.uploadImage(file)
      await usersApi.updateProfile(authUserId, { ...form, photoProfil: secureUrl })
      await refreshProfile(); notify('Photo de profil mise à jour')
    } catch (err) { notify(apiError(err, "L'upload de la photo a échoué")) } finally { setUploading(false) }
  }

  return (
    <div className="dash-2" style={{ gridTemplateColumns: '300px 1fr', alignItems: 'start' }}>
      <div className="panel" style={{ textAlign: 'center' }}>
        <label className="avatar-edit" title="Changer la photo">
          {isRealImg(profile.photoProfil)
            ? <div className="av" style={{ width: 110, height: 110, borderRadius: '50%', margin: '0 auto 16px', border: '2px solid var(--gold)', backgroundImage: `url(${profile.photoProfil})`, backgroundSize: 'cover', backgroundPosition: 'center', opacity: uploading ? .6 : 1 }} />
            : <div style={{ margin: '0 auto 16px', width: 110 }}><Avatar seed={user.email} size={110} style={{ border: '2px solid var(--gold)' }} /></div>}
          <span className="cam">{uploading ? '…' : '✎'}</span>
          <input ref={fileRef} type="file" accept="image/*" onChange={onPickPhoto} />
        </label>
        <div className="pn serif" style={{ fontSize: 22, color: 'var(--navy)' }}>{profile.firstname} {profile.lastname}</div>
        <div className="role">{ROLE_LABEL[roles?.[0]] || ''}</div>
        <div className="pe" style={{ fontSize: 13, color: 'var(--muted)', marginTop: 8 }}>{profile.email}</div>
        <p className="muted" style={{ fontSize: 12, marginTop: 14 }}>Cliquez sur la photo pour la changer.</p>
      </div>

      <div className="panel">
        <h3 className="serif">Informations personnelles</h3>
        <p className="psub">Mettez à jour vos coordonnées.</p>
        <div className="info-grid">
          <div className="field"><label>Prénom</label><input value={profile.firstname || ''} disabled /></div>
          <div className="field"><label>Nom</label><input value={profile.lastname || ''} disabled /></div>
          <div className="field"><label>E-mail</label><input value={profile.email || ''} disabled /></div>
          <div className="field"><label>Téléphone</label><input value={form.telephone} onChange={set('telephone')} /></div>
          <div className="field"><label>Ville</label><input value={form.ville} onChange={set('ville')} /></div>
          <div className="field"><label>Code postal</label><input value={form.codePostal} onChange={set('codePostal')} /></div>
          <div className="field"><label>Adresse</label><input value={form.adresse} onChange={set('adresse')} /></div>
          <div className="field"><label>Pays</label><input value={form.pays} onChange={set('pays')} /></div>
          <div className="field" style={{ gridColumn: '1 / -1' }}><label>Bio</label><textarea rows={3} maxLength={500} value={form.bio} onChange={set('bio')} /></div>
        </div>
        <button className="btn-block" style={{ maxWidth: 220 }} onClick={save} disabled={busy}>{busy ? 'Enregistrement…' : 'Enregistrer'}</button>
        <p className="muted" style={{ fontSize: 12, marginTop: 14 }}>Le changement de mot de passe nécessite une évolution du back-end (à venir).</p>
      </div>
    </div>
  )
}
