import { useState, useMemo, useEffect } from 'react'
import { Link, useSearchParams } from 'react-router-dom'
import ListingCard from '../../components/ListingCard.jsx'
import { ErrorState, EmptyState } from '../../components/states.jsx'
import { ListingSkeletonGrid } from '../../components/Skeleton.jsx'
import { useApi } from '../../hooks/useApi.js'
import { useSavedSearches } from '../../hooks/useSavedSearches.js'
import { useToast } from '../../components/toast-context.jsx'
import { biensApi } from '../../api/biens.js'
import { CATEGORIES, CATEGORIE_LABEL } from '../../lib/constants.js'

const PAGE_SIZE = 9

export default function Biens() {
  const [params, setParams] = useSearchParams()
  const notify = useToast()
  const { items: saved, save, remove } = useSavedSearches()

  // État des filtres, initialisé depuis l'URL
  const [cat, setCat] = useState(params.get('categorie') || 'Toutes')
  const [ville, setVille] = useState(params.get('ville') || 'Toutes')
  const [budget, setBudget] = useState(params.get('budget') || '')
  const [chambres, setChambres] = useState(params.get('chambres') || 'Toutes')
  const [tri, setTri] = useState(params.get('tri') || 'recent')
  const [page, setPage] = useState(1)

  const { data, loading, error, reload } = useApi(() => biensApi.disponibles(), [])
  const biens = Array.isArray(data) ? data : []
  const villes = ['Toutes', ...Array.from(new Set(biens.map((b) => b.ville).filter(Boolean)))]

  // Synchronise l'URL avec les filtres (recherche partageable / bookmarkable)
  useEffect(() => {
    const p = {}
    if (cat !== 'Toutes') p.categorie = cat
    if (ville !== 'Toutes') p.ville = ville
    if (budget) p.budget = budget
    if (chambres !== 'Toutes') p.chambres = chambres
    if (tri !== 'recent') p.tri = tri
    setParams(p, { replace: true })
    setPage(1)
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [cat, ville, budget, chambres, tri])

  const resultats = useMemo(() => {
    let r = biens.filter((b) =>
      (cat === 'Toutes' || b.categorie === cat) &&
      (ville === 'Toutes' || b.ville === ville) &&
      (!budget || (b.prixMensuel || 0) <= Number(budget)) &&
      (chambres === 'Toutes' || (b.chambres || 0) >= Number(chambres)))
    if (tri === 'prix-asc') r = [...r].sort((a, b) => (a.prixMensuel || 0) - (b.prixMensuel || 0))
    if (tri === 'prix-desc') r = [...r].sort((a, b) => (b.prixMensuel || 0) - (a.prixMensuel || 0))
    if (tri === 'surface') r = [...r].sort((a, b) => (b.surface || 0) - (a.surface || 0))
    return r
  }, [biens, cat, ville, budget, chambres, tri])

  const pages = Math.max(1, Math.ceil(resultats.length / PAGE_SIZE))
  const visible = resultats.slice((page - 1) * PAGE_SIZE, page * PAGE_SIZE)

  const appliquer = (p) => {
    setCat(p.categorie || 'Toutes'); setVille(p.ville || 'Toutes'); setBudget(p.budget || '')
    setChambres(p.chambres || 'Toutes'); setTri(p.tri || 'recent')
  }
  const sauvegarder = () => {
    const parts = [cat !== 'Toutes' && CATEGORIE_LABEL[cat], ville !== 'Toutes' && ville, budget && `≤ ${Number(budget).toLocaleString('fr-FR')}`].filter(Boolean)
    const label = parts.length ? parts.join(' · ') : 'Tous les biens'
    save(label, { categorie: cat !== 'Toutes' ? cat : '', ville: ville !== 'Toutes' ? ville : '', budget, chambres: chambres !== 'Toutes' ? chambres : '', tri: tri !== 'recent' ? tri : '' })
    notify('Recherche sauvegardée')
  }

  return (
    <div className="page active client-inner">
      <div className="inner">
        <div className="wrap">
          <div className="crumb"><Link to="/">Accueil</Link> / <b>Biens</b></div>
          <div className="liste-top">
            <div>
              <h1 className="page-title serif">Tous les biens</h1>
              <div className="result-count" style={{ marginTop: 10 }}>{resultats.length} bien(s) disponible(s)</div>
            </div>
            <div className="sortbar">
              Trier :
              <select value={tri} onChange={(e) => setTri(e.target.value)}>
                <option value="recent">Plus récents</option>
                <option value="prix-asc">Prix croissant</option>
                <option value="prix-desc">Prix décroissant</option>
                <option value="surface">Surface</option>
              </select>
            </div>
          </div>

          {/* Recherches sauvegardées */}
          {saved.length > 0 && (
            <div className="saved-row">
              <span className="muted" style={{ fontSize: 12.5 }}>Mes recherches :</span>
              {saved.map((s) => (
                <span className="saved-chip" key={s.id}>
                  <span onClick={() => appliquer(s.params)}>{s.label}</span>
                  <span className="x" onClick={() => remove(s.id)} title="Supprimer">×</span>
                </span>
              ))}
            </div>
          )}

          <div className="liste-layout">
            <aside className="filters">
              <h4>Catégorie</h4>
              <div className="pill-row">
                {['Toutes', ...CATEGORIES].map((c) => (
                  <button key={c} className={`pill ${cat === c ? 'on' : ''}`} onClick={() => setCat(c)}>{c === 'Toutes' ? 'Toutes' : CATEGORIE_LABEL[c]}</button>
                ))}
              </div>
              <h4>Ville</h4>
              <div className="pill-row">
                {villes.map((v) => <button key={v} className={`pill ${ville === v ? 'on' : ''}`} onClick={() => setVille(v)}>{v}</button>)}
              </div>
              <h4>Chambres (min.)</h4>
              <div className="pill-row">
                {['Toutes', '1', '2', '3', '4'].map((n) => <button key={n} className={`pill ${chambres === n ? 'on' : ''}`} onClick={() => setChambres(n)}>{n === 'Toutes' ? 'Toutes' : `${n}+`}</button>)}
              </div>
              <h4>Budget max (FCFA / mois)</h4>
              <div className="frange">
                <input placeholder="Ex. 300000" inputMode="numeric" value={budget} onChange={(e) => setBudget(e.target.value.replace(/\D/g, ''))} />
              </div>
              <button className="btn-outline btn-sm" style={{ marginTop: 18 }} onClick={sauvegarder}>☆ Sauvegarder cette recherche</button>
            </aside>

            <div>
              {loading ? <ListingSkeletonGrid count={6} />
                : error ? <ErrorState error={error} onRetry={reload} />
                : resultats.length === 0 ? <EmptyState title="Aucun bien ne correspond" hint="Élargissez vos critères de recherche." />
                : (
                  <>
                    <div className="flist-grid">{visible.map((b) => <ListingCard key={b.id} bien={b} />)}</div>
                    {pages > 1 && (
                      <div className="pager">
                        <button onClick={() => setPage((p) => Math.max(1, p - 1))}>‹</button>
                        {Array.from({ length: pages }, (_, i) => i + 1).map((n) => (
                          <button key={n} className={n === page ? 'on' : ''} onClick={() => setPage(n)}>{n}</button>
                        ))}
                        <button onClick={() => setPage((p) => Math.min(pages, p + 1))}>›</button>
                      </div>
                    )}
                  </>
                )}
            </div>
          </div>
        </div>
      </div>
    </div>
  )
}
