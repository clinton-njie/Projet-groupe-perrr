import { useState, useEffect } from 'react'
import { Link, useNavigate } from 'react-router-dom'
import { Ph, isRealImg } from '../../components/ui.jsx'
import ListingCard from '../../components/ListingCard.jsx'
import { AsyncState } from '../../components/states.jsx'
import { useApi } from '../../hooks/useApi.js'
import { biensApi } from '../../api/biens.js'
import { CATEGORIES, CATEGORIE_LABEL, seedOf } from '../../lib/constants.js'

const VILLES = ['Douala', 'Yaoundé', 'Bafoussam', 'Dschang', 'Buea', 'Limbé', 'Kribi', 'Bamenda', 'Garoua', 'Maroua', 'Bertoua', 'Sangmélima']

export default function Home() {
  const [slide, setSlide] = useState(0)
  const nav = useNavigate()
  const [sVille, setSVille] = useState('')
  const [sCat, setSCat] = useState('')
  const [sBudget, setSBudget] = useState('')

  const lancerRecherche = () => {
    const p = new URLSearchParams()
    if (sVille) p.set('ville', sVille)
    if (sCat) p.set('categorie', sCat)
    if (sBudget) p.set('budget', sBudget.replace(/\D/g, ''))
    nav(`/biens${p.toString() ? '?' + p.toString() : ''}`)
  }
  const { data, loading, error, reload } = useApi(() => biensApi.disponibles(), [])
  const biens = Array.isArray(data) ? data : []
  const featured = biens.slice(0, 6)

  // Une vraie photo par catégorie (1er bien de la catégorie ayant une photo Cloudinary)
  const catImg = {}
  CATEGORIES.forEach((c) => {
    const b = biens.find((x) => x.categorie === c && /^https:\/\//.test(x.photoPrincipale || ''))
    if (b) catImg[c] = b.photoPrincipale
  })

  // Slides du hero : photos réelles des biens si dispo, sinon placeholders.
  const slides = (featured.length ? featured.slice(0, 4) : [0, 1, 2, 3].map((i) => ({ id: `p${i}` })))
    .map((b, i) => ({ seed: seedOf(b) || `hero-${i}`, url: b.photoPrincipale, place: b.ville ? `${b.quartier || ''}, ${b.ville}` : 'Cameroun' }))

  useEffect(() => {
    if (slides.length < 2) return
    const t = setInterval(() => setSlide((s) => (s + 1) % slides.length), 6000)
    return () => clearInterval(t)
  }, [slides.length])

  const catSeeds = { APPARTEMENT: 'cat-appart', VILLA: 'cat-villa', CITE: 'cat-cite', TERRAIN: 'cat-terrain' }
  const cur = slides[slide] || slides[0] || { place: 'Cameroun' }

  return (
    <div className="page active">
      <section className="hero">
        {slides.map((s, i) => (
          <div className={`hero-slide ${i === slide ? 'is-active' : ''}`} key={i}>
            <Ph seed={s.seed} url={s.url} className="img" style={{ position: 'absolute', inset: 0 }} />
          </div>
        ))}
        <div className="hero-content">
          <span className="eyebrow">Immobilier d'exception · Cameroun</span>
          <h1 className="serif">L'adresse qui vous <em>ressemble</em></h1>
          <p className="hero-sub">
            Appartements, villas, cités et terrains sélectionnés à Douala et Yaoundé. Visitez, réservez et payez en toute sécurité via Mobile Money.
          </p>
          <div className="search">
            <div className="search-field">
              <label>Localisation</label>
              <select value={sVille} onChange={(e) => setSVille(e.target.value)}>
                <option value="">Toutes les villes</option>
                {VILLES.map((v) => <option key={v} value={v}>{v}</option>)}
              </select>
            </div>
            <div className="search-field">
              <label>Catégorie</label>
              <select value={sCat} onChange={(e) => setSCat(e.target.value)}>
                <option value="">Toutes</option>
                {CATEGORIES.map((c) => <option key={c} value={c}>{CATEGORIE_LABEL[c]}</option>)}
              </select>
            </div>
            <div className="search-field">
              <label>Budget max</label>
              <input placeholder="500 000 FCFA" value={sBudget} onChange={(e) => setSBudget(e.target.value)} inputMode="numeric" />
            </div>
            <button className="search-btn" onClick={lancerRecherche}>Rechercher</button>
          </div>
        </div>
        <div className="hero-bar">
          <div className="hero-caption">
            <div className="c-label">Sélection du moment</div>
            <div className="c-place serif">{cur.place}</div>
          </div>
          <div className="hero-nav">
            <span className="counter"><b>{String(slide + 1).padStart(2, '0')}</b> / {String(slides.length).padStart(2, '0')}</span>
            <div className="arrows">
              <button onClick={() => setSlide((s) => (s - 1 + slides.length) % slides.length)}>‹</button>
              <button onClick={() => setSlide((s) => (s + 1) % slides.length)}>›</button>
            </div>
          </div>
        </div>
      </section>

      <section className="featured">
        <div className="wrap">
          <div className="sec-head">
            <div><span className="eyebrow">Biens à la une</span><h2 className="serif">Notre sélection</h2></div>
            <Link className="sec-link" to="/biens">Voir tous les biens</Link>
          </div>
          <AsyncState loading={loading} error={error} onRetry={reload}
            empty={!featured.length} emptyTitle="Aucun bien pour l'instant" emptyHint="Revenez bientôt : de nouvelles annonces arrivent.">
            <div className="feat-grid">{featured.map((b) => <ListingCard key={b.id} bien={b} />)}</div>
          </AsyncState>
        </div>
      </section>

      <section className="cats">
        <div className="wrap">
          <div className="sec-head"><div><span className="eyebrow">Explorer</span><h2 className="serif">Par catégorie</h2></div></div>
          <div className="cat-grid">
            {CATEGORIES.map((c) => (
              <Link className="cat" to={`/biens?categorie=${c}`} key={c}>
                <Ph seed={catSeeds[c]} url={catImg[c]} />
                <div className="cat-label"><div className="n serif">{CATEGORIE_LABEL[c]}</div><div className="k">Découvrir</div></div>
              </Link>
            ))}
          </div>
        </div>
      </section>

      <section className="steps">
        <div className="wrap">
          <div className="sec-head"><div><span className="eyebrow">Comment ça marche</span><h2 className="serif">Trois étapes simples</h2></div></div>
          <div className="steps-grid">
            {[
              ['01', 'Explorez', "Parcourez des biens vérifiés et filtrez selon vos critères : ville, catégorie, budget."],
              ['02', 'Visitez', "Réservez une visite payante sur un créneau de l'agent, réglée via Mobile Money."],
              ['03', 'Réservez', "Réservez votre location pour la période souhaitée. Le montant est calculé automatiquement."],
            ].map(([n, t, p]) => (
              <div className="step" key={n}><div className="num">{n}</div><div className="bar" /><h3 className="serif">{t}</h3><p>{p}</p></div>
            ))}
          </div>
        </div>
      </section>

      <section className="cta">
        <div className="inner">
          <span className="eyebrow">Vous êtes propriétaire ou agence ?</span>
          <h2 className="serif">Publiez vos biens sur ImmoAPP</h2>
          <p>Rejoignez notre réseau d'agents et donnez à vos biens la visibilité qu'ils méritent.</p>
          <Link className="btn-gold" to="/devenir-agent">Devenir agent</Link>
        </div>
      </section>
    </div>
  )
}
