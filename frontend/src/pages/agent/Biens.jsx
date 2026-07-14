import { useState, useMemo } from 'react'
import { useNavigate } from 'react-router-dom'
import { Badge, Ph } from '../../components/ui.jsx'
import { AsyncState } from '../../components/states.jsx'
import { useToast } from '../../components/toast-context.jsx'
import { useApi } from '../../hooks/useApi.js'
import { biensApi } from '../../api/biens.js'
import { apiError } from '../../api/http.js'
import { CATEGORIE_LABEL, STATUT_LABEL, STATUT_BADGE, fmtPrix, seedOf } from '../../lib/constants.js'

const STATUTS = ['TOUS', 'DISPONIBLE', 'RESERVE', 'LOUE']

export default function AgentBiens() {
  const nav = useNavigate()
  const notify = useToast()
  const [q, setQ] = useState('')
  const [statut, setStatut] = useState('TOUS')
  const { data, loading, error, reload } = useApi(() => biensApi.mesBiens(), [])
  const mesBiens = Array.isArray(data) ? data : []

  const visible = useMemo(() => mesBiens.filter((b) =>
    (statut === 'TOUS' || b.statut === statut) &&
    (!q || `${b.nom} ${b.ville} ${b.quartier}`.toLowerCase().includes(q.toLowerCase()))
  ), [mesBiens, q, statut])

  const supprimer = async (id) => {
    if (!confirm('Supprimer ce bien ?')) return
    try { await biensApi.remove(id); notify('Bien supprimé'); reload() }
    catch (e) { notify(apiError(e, 'Suppression impossible')) }
  }

  return (
    <div className="view active">
      <div className="panel-a">
        <div className="ph" style={{ flexWrap: 'wrap', gap: 12 }}>
          <h3 className="serif">Mes biens ({visible.length})</h3>
          <div style={{ display: 'flex', gap: 10, alignItems: 'center', flexWrap: 'wrap' }}>
            <input placeholder="Rechercher…" value={q} onChange={(e) => setQ(e.target.value)}
              style={{ border: '1px solid var(--line)', background: 'var(--white)', padding: '9px 13px', fontSize: 13.5, fontFamily: 'var(--sans)', color: 'var(--navy)' }} />
            <select value={statut} onChange={(e) => setStatut(e.target.value)}
              style={{ border: '1px solid var(--line)', background: 'var(--paper)', padding: '9px 12px', fontSize: 13, color: 'var(--navy)' }}>
              {STATUTS.map((s) => <option key={s} value={s}>{s === 'TOUS' ? 'Tous statuts' : STATUT_LABEL[s]}</option>)}
            </select>
            <button className="btn-pub" onClick={() => nav('/agent/publier')}>+ Publier</button>
          </div>
        </div>
        <AsyncState loading={loading} error={error} onRetry={reload} empty={!visible.length} emptyTitle="Aucun bien"
          emptyHint={mesBiens.length ? 'Aucun résultat pour ce filtre.' : 'Publiez votre premier bien (abonnement actif requis).'}>
          <table className="tbl">
            <thead><tr><th>Bien</th><th>Catégorie</th><th>Prix / mois</th><th>Statut</th><th>Actions</th></tr></thead>
            <tbody>
              {visible.map((b) => (
                <tr key={b.id}>
                  <td><div className="bien-cell"><div className="th"><Ph seed={seedOf(b)} url={b.photoPrincipale} style={{ width: 52, height: 40 }} /></div><div><div className="bn serif">{b.nom}</div><div className="bl">{b.quartier}, {b.ville}</div></div></div></td>
                  <td>{CATEGORIE_LABEL[b.categorie]}</td>
                  <td>{fmtPrix(b.prixMensuel)} FCFA</td>
                  <td><Badge cls={STATUT_BADGE[b.statut]}>{STATUT_LABEL[b.statut]}</Badge></td>
                  <td><span className="act-ic"><span title="Voir" onClick={() => nav(`/biens/${b.id}`)}>👁</span><span title="Supprimer" onClick={() => supprimer(b.id)}>🗑</span></span></td>
                </tr>
              ))}
            </tbody>
          </table>
        </AsyncState>
      </div>
    </div>
  )
}
