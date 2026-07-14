import { useState, useRef } from 'react'
import { useNavigate } from 'react-router-dom'
import { useToast } from '../../components/toast-context.jsx'
import { biensApi } from '../../api/biens.js'
import { apiError } from '../../api/http.js'
import { CATEGORIES, CATEGORIE_LABEL } from '../../lib/constants.js'

export default function AgentPublier() {
  const nav = useNavigate()
  const notify = useToast()
  const mainRef = useRef(null)
  const secRef = useRef(null)
  const [form, setForm] = useState({ categorie: 'APPARTEMENT', nom: '', ville: '', quartier: '', prixMensuel: '', nombrePiecesDisponibles: '', description: '' })
  const [main, setMain] = useState(null)   // { secureUrl, publicId }
  const [secs, setSecs] = useState([])     // [{ secureUrl, publicId }]
  const [uploading, setUploading] = useState(false)
  const [busy, setBusy] = useState(false)
  const [error, setError] = useState('')
  const set = (k) => (e) => setForm((f) => ({ ...f, [k]: e.target.value }))

  const pickMain = async (e) => {
    const file = e.target.files?.[0]; if (!file) return
    setUploading(true); setError('')
    try { setMain(await biensApi.uploadImage(file)) }
    catch (err) { setError(apiError(err, "Échec de l'upload de la photo principale")) }
    finally { setUploading(false) }
  }
  const pickSecs = async (e) => {
    const files = Array.from(e.target.files || []).slice(0, 5 - secs.length); if (!files.length) return
    setUploading(true); setError('')
    try {
      const res = await Promise.all(files.map((f) => biensApi.uploadImage(f)))
      setSecs((s) => [...s, ...res].slice(0, 5))
    } catch (err) { setError(apiError(err, "Échec de l'upload d'une photo")) }
    finally { setUploading(false) }
  }

  const submit = async (e) => {
    e.preventDefault()
    if (!main) { setError('Ajoutez une photo principale.'); return }
    setBusy(true); setError('')
    try {
      const payload = {
        categorie: form.categorie,
        nom: form.nom,
        ville: form.ville,
        quartier: form.quartier,
        photoPrincipale: main.secureUrl,
        imagePublicId: main.publicId,
        photosSecondaires: secs.map((s) => ({ url: s.secureUrl, publicId: s.publicId })),
        prixMensuel: Number(form.prixMensuel),
        description: form.description,
        ...(form.categorie === 'CITE' ? { nombrePiecesDisponibles: Number(form.nombrePiecesDisponibles) } : {}),
      }
      await biensApi.create(payload)
      notify('Bien publié avec succès.')
      nav('/agent/biens')
    } catch (err) {
      setError(apiError(err, "La publication a échoué (abonnement actif requis ?)."))
    } finally { setBusy(false) }
  }

  return (
    <div className="view active">
      <form className="form-card" onSubmit={submit}>
        {error && <div className="form-error">{error}</div>}
        <div className="form-sec">Informations générales</div>
        <div className="fgrid">
          <div className="field">
            <label>Catégorie</label>
            <select value={form.categorie} onChange={set('categorie')}>
              {CATEGORIES.map((c) => <option key={c} value={c}>{CATEGORIE_LABEL[c]}</option>)}
            </select>
          </div>
          <div className="field"><label>Nom de l'annonce</label><input value={form.nom} onChange={set('nom')} maxLength={150} required /></div>
          <div className="field"><label>Ville</label><input value={form.ville} onChange={set('ville')} required /></div>
          <div className="field"><label>Quartier</label><input value={form.quartier} onChange={set('quartier')} required /></div>
          <div className="field"><label>Prix mensuel (FCFA)</label><input type="number" min="1" value={form.prixMensuel} onChange={set('prixMensuel')} required /></div>
          {/* Champ conditionnel : uniquement pour la catégorie CITE */}
          {form.categorie === 'CITE' && (
            <div className="field"><label>Nombre de pièces disponibles</label><input type="number" min="1" value={form.nombrePiecesDisponibles} onChange={set('nombrePiecesDisponibles')} required /></div>
          )}
        </div>

        <div className="form-sec">Description</div>
        <div className="field"><label>Description du bien</label><textarea rows={4} maxLength={1000} value={form.description} onChange={set('description')} placeholder="Décrivez le bien…" /></div>

        <div className="form-sec">Photo principale {uploading && <span className="muted">· envoi en cours…</span>}</div>
        <div className="dropzone" onClick={() => mainRef.current?.click()}>
          <div className="di">⬆</div>
          <div className="dt">{main ? 'Photo principale ajoutée ✓' : 'Cliquez pour sélectionner la photo principale'}</div>
          <div className="ds">Upload sur Cloudinary</div>
          <input ref={mainRef} type="file" accept="image/*" hidden onChange={pickMain} />
        </div>
        {main && <div className="thumb-row"><div className="tb" style={{ backgroundImage: `url(${main.secureUrl})` }} /></div>}

        <div className="form-sec">Photos supplémentaires (max 5)</div>
        <div className="thumb-row">
          {secs.map((s, i) => <div className="tb" key={i} style={{ backgroundImage: `url(${s.secureUrl})` }} />)}
          {secs.length < 5 && <div className="tb add" onClick={() => secRef.current?.click()}>+</div>}
          <input ref={secRef} type="file" accept="image/*" multiple hidden onChange={pickSecs} />
        </div>

        <div style={{ marginTop: 30, display: 'flex', gap: 12 }}>
          <button className="btn-pub" type="submit" disabled={busy || uploading}>{busy ? 'Publication…' : 'Publier le bien'}</button>
          <button type="button" className="btn-outline" style={{ width: 'auto', padding: '11px 24px', marginTop: 0 }} onClick={() => nav('/agent/biens')}>Annuler</button>
        </div>
      </form>
    </div>
  )
}
