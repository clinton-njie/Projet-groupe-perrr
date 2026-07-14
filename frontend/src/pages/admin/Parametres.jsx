import { useState, useEffect } from 'react'
import { AsyncState } from '../../components/states.jsx'
import { useToast } from '../../components/toast-context.jsx'
import { useApi } from '../../hooks/useApi.js'
import { biensApi } from '../../api/biens.js'
import { apiError } from '../../api/http.js'
import { CATEGORIES, CATEGORIE_LABEL, PRIX_VISITE_DEFAUT } from '../../lib/constants.js'

export default function AdminParametres() {
  const notify = useToast()
  const [prix, setPrix] = useState({})
  const [saving, setSaving] = useState('')

  const { loading, error, reload } = useApi(async () => {
    const entries = await Promise.all(CATEGORIES.map(async (c) => {
      try { const r = await biensApi.prixVisite(c); return [c, r?.prixVisite ?? PRIX_VISITE_DEFAUT[c]] }
      catch { return [c, PRIX_VISITE_DEFAUT[c]] }
    }))
    const obj = Object.fromEntries(entries)
    setPrix(obj)
    return obj
  }, [])

  const save = async (c) => {
    setSaving(c)
    try { await biensApi.adminSetPrixVisite(c, Number(prix[c])); notify(`Tarif ${CATEGORIE_LABEL[c]} mis à jour`) }
    catch (e) { notify(apiError(e, 'Mise à jour impossible')) } finally { setSaving('') }
  }

  return (
    <div className="view active">
      <div className="form-card">
        <div className="form-sec">Tarifs de visite par catégorie (FCFA)</div>
        <p className="muted" style={{ fontSize: 13.5, marginBottom: 20 }}>Ces montants s'appliquent aux visites selon la catégorie du bien.</p>
        <AsyncState loading={loading} error={error} onRetry={reload} empty={false}>
          <div className="setting-grid">
            {CATEGORIES.map((c) => (
              <div className="field" key={c}>
                <label>{CATEGORIE_LABEL[c]}</label>
                <input type="number" min="0" value={prix[c] ?? ''} onChange={(e) => setPrix((p) => ({ ...p, [c]: e.target.value }))} />
                <button className="linklike btn-sm" style={{ color: 'var(--gold-deep)', marginTop: 8 }} onClick={() => save(c)} disabled={saving === c}>
                  {saving === c ? 'Enregistrement…' : 'Enregistrer'}
                </button>
              </div>
            ))}
          </div>
        </AsyncState>
      </div>
    </div>
  )
}
