import { useState, useRef, useEffect } from 'react'
import { Link, useNavigate } from 'react-router-dom'
import { Avatar, isRealImg } from '../../components/ui.jsx'
import { AvisPlateformeModal } from '../../components/modals.jsx'
import { Loader } from '../../components/states.jsx'
import { useToast } from '../../components/toast-context.jsx'
import { useAuth } from '../../context/AuthContext.jsx'
import { usersApi } from '../../api/users.js'
import { biensApi } from '../../api/biens.js'
import { apiError } from '../../api/http.js'
import { ROLE_LABEL } from '../../lib/constants.js'

const FIELDS = ['telephone', 'adresse', 'ville', 'codePostal', 'pays', 'bio']

export default function Profil() {
  const nav = useNavigate()
  const notify = useToast()
  const fileRef = useRef(null)
  const { user, profile, authUserId, roles, refreshProfile, isAgent, isAdmin, logout } = useAuth()
  const [form, setForm] = useState(null)
  const [localPhoto, setLocalPhoto] = useState(null)
  const [busy, setBusy] = useState(false)
  const [uploading, setUploading] = useState(false)
  const [avisOpen, setAvisOpen] = useState(false)

  useEffect(() => {
    if (profile) setForm(Object.fromEntries(FIELDS.map((k) => [k, profile[k] || ''])))
  }, [profile])

  if (!profile || !form) return <div className="client-inner"><div className="inner"><Loader label="Chargement du profil…" /></div></div>

  const set = (k) => (e) => setForm((f) => ({ ...f, [k]: e.target.value }))
  const currentPhoto = localPhoto || profile.photoProfil

  const save = async () => {
    setBusy(true)
    try {
      await usersApi.updateProfile(authUserId, { ...form, photoProfil: profile.photoProfil || null })
      await refreshProfile()
      notify('Profil enregistré')
    } catch (e) { notify(apiError(e, "Échec de l'enregistrement")) } finally { setBusy(false) }
  }

  // Changement de photo : upload Cloudinary → PUT profil → aperçu.
  const onPickPhoto = async (e) => {
    const file = e.target.files?.[0]
    if (!file) return
    const reader = new FileReader()
    reader.onload = () => setLocalPhoto(reader.result) // aperçu immédiat
    reader.readAsDataURL(file)
    setUploading(true)
    try {
      const { secureUrl } = await biensApi.uploadImage(file)
      await usersApi.updateProfile(authUserId, { ...form, photoProfil: secureUrl })
      await refreshProfile()
      setLocalPhoto(null)
      notify('Photo de profil mise à jour')
    } catch (err) {
      setLocalPhoto(null)
      // Le seul endpoint d'upload (bien-service) est réservé AGENT/ADMIN → 403 pour un client.
      if (err?.response?.status === 403) {
        notify("Upload de photo indisponible pour les clients (restriction back-end).")
      } else {
        notify(apiError(err, "L'upload de la photo a échoué"))
      }
    } finally { setUploading(false) }
  }

  const menu = [['Informations', '/profil', true], ['Mes visites', '/mes-visites'], ['Mes réservations', '/mes-reservations'], ['Mes favoris', '/mes-favoris'], ['Messagerie', '/messagerie']]
  const doLogout = async () => { await logout(); nav('/') }

  return (
    <div className="page active client-inner">
      <div className="inner">
        <div className="wrap">
          <div className="crumb"><Link to="/">Accueil</Link> / <b>Mon profil</b></div>
          <h1 className="page-title serif">Mon espace</h1>

          <div className="profil-layout">
            <aside className="profil-side">
              <div className="profil-card">
                <label className="avatar-edit" title="Changer la photo">
                  {isRealImg(currentPhoto) || (localPhoto)
                    ? <div className="av" style={{ width: 96, height: 96, borderRadius: '50%', margin: '0 auto 16px', border: '2px solid var(--gold)', backgroundImage: `url(${currentPhoto})`, backgroundSize: 'cover', backgroundPosition: 'center', opacity: uploading ? 0.6 : 1 }} />
                    : <div style={{ margin: '0 auto 16px', width: 96 }}><Avatar seed={user.email} size={96} style={{ border: '2px solid var(--gold)' }} /></div>}
                  <span className="cam">{uploading ? '…' : '✎'}</span>
                  <input ref={fileRef} type="file" accept="image/*" onChange={onPickPhoto} />
                </label>
                <div className="pn serif">{profile.firstname} {profile.lastname}</div>
                <div className="role">{ROLE_LABEL[roles?.[0]] || 'Client'}</div>
                <div className="pe">{profile.email}</div>
              </div>
              <ul className="side-menu">
                {menu.map(([t, to, on]) => <li key={to}><Link className={on ? 'on' : ''} to={to}>{t} <span>›</span></Link></li>)}
                <li><a onClick={() => setAvisOpen(true)}>Donner mon avis sur ImmoAPP <span>★</span></a></li>
                <li><a className="danger" onClick={doLogout}>Déconnexion <span>↩</span></a></li>
              </ul> 
            </aside>

            <div>
              <div className="panel">
                <h3 className="serif">Informations personnelles</h3>
                <p className="psub">Complétez votre profil ImmoAPP.</p>
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
              </div>

              {!isAgent && !isAdmin && (
                <div className="becomes">
                  <div>
                    <div className="bt serif">Vous êtes propriétaire ou agence ?</div>
                    <p>Passez au statut d'agent pour publier vos propres biens sur ImmoAPP.</p>
                  </div>
                  <Link className="btn-gold" to="/devenir-agent">Devenir agent</Link>
                </div>
              )}
            </div>
          </div>
        </div>
      </div>
      <AvisPlateformeModal open={avisOpen} onClose={() => setAvisOpen(false)} />
    </div>
  )
}
