import { useState, useEffect } from 'react'
import { Link, useParams, useNavigate } from 'react-router-dom'
import { Ph, Avatar, StarsStatic, isRealImg } from '../../components/ui.jsx'
import { AsyncState } from '../../components/states.jsx'
import { VisiteModal, ReservationModal, SignalerModal } from '../../components/modals.jsx'
import { useApi } from '../../hooks/useApi.js'
import { useFavorites } from '../../hooks/useFavorites.js'
import { pushRecent } from '../../hooks/useSavedSearches.js'
import { useAuth } from '../../context/AuthContext.jsx'
import Lightbox from '../../components/Lightbox.jsx'
import ShareButton from '../../components/ShareButton.jsx'
import ListingCard from '../../components/ListingCard.jsx'
import { biensApi } from '../../api/biens.js'
import { usersApi } from '../../api/users.js'
import { avisApi } from '../../api/avis.js'
import { CATEGORIE_LABEL, PRIX_VISITE_DEFAUT, fmtPrix, specsBien, seedOf } from '../../lib/constants.js'

export default function BienDetail() {
  const { id } = useParams()
  const nav = useNavigate()
  const { user } = useAuth()
  const { has, toggle } = useFavorites()
  const [visiteOpen, setVisiteOpen] = useState(false)
  const [resaOpen, setResaOpen] = useState(false)
  const [signalerOpen, setSignalerOpen] = useState(false)
  const [lightbox, setLightbox] = useState(-1)

  const { data: bien, loading, error, reload } = useApi(() => biensApi.get(id), [id])
  const [prixVisite, setPrixVisite] = useState(null)
  const [agentId, setAgentId] = useState(null)
  const [agentProfile, setAgentProfile] = useState(null)
  const [avis, setAvis] = useState(null)
  const [similar, setSimilar] = useState([])

  // Galerie complète (principale + secondaires) pour la lightbox
  const gallery = bien
    ? [{ url: bien.photoPrincipale, seed: seedOf(bien) }, ...(bien.photosSecondaires || []).map((p, i) => ({ url: p.url, seed: seedOf(bien) + '-' + i }))]
    : []

  useEffect(() => {
    if (!bien) return
    pushRecent(bien.id)
    biensApi.prixVisite(bien.categorie).then((r) => setPrixVisite(r?.prixVisite ?? PRIX_VISITE_DEFAUT[bien.categorie])).catch(() => setPrixVisite(PRIX_VISITE_DEFAUT[bien.categorie]))
    // Biens similaires : même catégorie, autre bien, on privilégie la même ville
    biensApi.disponibles().then((all) => {
      const list = (Array.isArray(all) ? all : []).filter((b) => b.id !== bien.id && b.categorie === bien.categorie)
      list.sort((a, b) => (b.ville === bien.ville) - (a.ville === bien.ville))
      setSimilar(list.slice(0, 4))
    }).catch(() => {})
    if (bien.agentEmail) {
      usersApi.idByEmail(bien.agentEmail)
        .then((aid) => {
          setAgentId(aid)
          usersApi.getProfile(aid).then(setAgentProfile).catch(() => {})
          return avisApi.agent(aid)
        })
        .then((a) => setAvis(a)).catch(() => {})
    }
  }, [bien])

  const agentNom = agentProfile ? `${agentProfile.firstname || ''} ${agentProfile.lastname || ''}`.trim() : ''

  const guardAuth = (fn) => () => { if (!user) return nav('/connexion', { state: { from: `/biens/${id}` } }); fn() }

  return (
    <div className="page active client-inner">
      <div className="inner">
        <div className="wrap">
          <div className="crumb"><Link to="/">Accueil</Link> / <Link to="/biens">Biens</Link> / <b>{bien?.nom || '…'}</b></div>

          <AsyncState loading={loading} error={error} onRetry={reload} empty={!loading && !bien} emptyTitle="Bien introuvable">
            {bien && (
              <>
                <div className="detail-hero">
                  <div className="g main" onClick={() => setLightbox(0)}><Ph seed={seedOf(bien)} url={bien.photoPrincipale} /></div>
                  <div className="g" onClick={() => setLightbox(1)}><Ph seed={seedOf(bien) + '-2'} url={bien.photosSecondaires?.[0]?.url} /></div>
                  <div className="g" onClick={() => setLightbox(2)}>
                    <Ph seed={seedOf(bien) + '-3'} url={bien.photosSecondaires?.[1]?.url} />
                    <span className="gcount">⛶ {gallery.length} photo{gallery.length > 1 ? 's' : ''}</span>
                  </div>
                </div>

                <div className="detail-layout">
                  <div>
                    <span className="d-tag">{CATEGORIE_LABEL[bien.categorie] || bien.categorie}</span>
                    <h1 className="d-title serif">{bien.nom}</h1>
                    <div className="d-loc">{bien.quartier}, {bien.ville}</div>

                    <div className="d-specs">
                      {bien.categorie === 'CITE'
                        ? <div className="d-spec"><div className="v serif">{bien.nombrePiecesDisponibles ?? '—'}</div><div className="l">Pièces dispo.</div></div>
                        : <>
                            {bien.chambres != null && <div className="d-spec"><div className="v serif">{bien.chambres}</div><div className="l">Chambres</div></div>}
                            {bien.sallesBain != null && <div className="d-spec"><div className="v serif">{bien.sallesBain}</div><div className="l">Salles de bain</div></div>}
                          </>}
                      {bien.surface != null && <div className="d-spec"><div className="v serif">{bien.surface}</div><div className="l">m²</div></div>}
                      <div className="d-spec"><div className="v serif">{specsBien(bien) ? '' : ''}{fmtPrix(bien.prixMensuel)}</div><div className="l">FCFA / mois</div></div>
                    </div>

                    <div className="d-sec-title">Description</div>
                    <p className="d-desc">{bien.description || 'Aucune description fournie.'}</p>

                    {avis && (
                      <>
                        <div className="d-sec-title">Avis sur l'agent</div>
                        <div className="reviews">
                          <div className="rev-summary">
                            <div className="rev-avg">
                              <div className="big serif">{(avis.noteMoyenneGlobale ?? 0).toFixed(1)}</div>
                              <StarsStatic note={avis.noteMoyenneGlobale || 0} />
                              <div className="cnt">{(avis.avis?.length ?? 0)} avis</div>
                            </div>
                            <div className="rev-bars">
                              {[['Communication', avis.noteMoyenneCommunication], ['Professionnalisme', avis.noteMoyenneProfessionnalisme], ['Disponibilité', avis.noteMoyenneDisponibilite], ["Conformité", avis.noteMoyenneConformiteAnnonce], ['État général', avis.noteMoyenneEtatGeneral], ['Qualité/prix', avis.noteMoyenneRapportQualitePrix]].map(([l, v]) => (
                                <div className="rev-bar" key={l}><span className="l">{l}</span><span className="track"><span style={{ width: `${((v || 0) / 5) * 100}%` }} /></span><span className="v">{(v || 0).toFixed(1)}</span></div>
                              ))}
                            </div>
                          </div>
                          {(avis.avis || []).map((a, i) => (
                            <div className="rev-card" key={i}>
                              <Avatar seed={`avis-${i}`} size={46} />
                              <div>
                                <div className="rhead"><span className="rn serif">{a.auteur || a.clientNom || 'Client'}</span><StarsStatic note={a.noteGlobale || a.note || 0} /></div>
                                <div className="rc">{a.commentaire || ''}</div>
                              </div>
                            </div>
                          ))}
                          {(!avis.avis || avis.avis.length === 0) && <p className="muted" style={{ fontSize: 14 }}>Aucun avis pour le moment.</p>}
                        </div>
                      </>
                    )}
                  </div>

                  <aside>
                    <div className="book">
                      <div className="price serif">{fmtPrix(bien.prixMensuel)} <small>{bien.categorie === 'TERRAIN' ? 'FCFA' : 'FCFA / mois'}</small></div>
                      <div className="visite-fee">Frais de visite : <b>{prixVisite != null ? fmtPrix(prixVisite) : '…'} FCFA</b></div>

                      <button className="btn-primary" onClick={guardAuth(() => setVisiteOpen(true))}>Réserver une visite</button>
                      {bien.categorie !== 'TERRAIN' && (
                        bien.statut === 'DISPONIBLE'
                          ? <button className="btn-outline" onClick={guardAuth(() => setResaOpen(true))}>Réserver le bien</button>
                          : <button className="btn-outline" disabled style={{ opacity: .55, cursor: 'not-allowed' }}>{bien.statut === 'LOUE' ? 'Déjà loué' : 'Déjà réservé'}</button>
                      )}
                      <button className="btn-outline" onClick={guardAuth(() => nav('/messagerie', { state: { receiverId: agentId, bienId: bien.id, bienNom: bien.nom } }))}>Contacter l'agent</button>
                      <button className="btn-outline" onClick={() => toggle(bien.id)}>{has(bien.id) ? '♥ Dans vos favoris' : '♡ Ajouter aux favoris'}</button>
                      <div style={{ marginTop: 12 }}><ShareButton title={bien.nom} text={`${bien.nom} — ${bien.quartier}, ${bien.ville}`} /></div>

                      <Link className="agent-mini" to={`/agents/${encodeURIComponent(bien.agentEmail)}`} style={{ cursor: 'pointer' }}>
                        {isRealImg(agentProfile?.photoProfil)
                          ? <div className="av" style={{ width: 52, height: 52, borderRadius: '50%', flex: 'none', backgroundImage: `url(${agentProfile.photoProfil})`, backgroundSize: 'cover', backgroundPosition: 'center' }} />
                          : <Avatar seed={bien.agentEmail || 'agent'} size={52} />}
                        <div>
                          <div className="an serif">{agentNom || bien.agentEmail || 'Agent'}</div>
                          <div className="ar">Agent <span className="verif">✔ vérifié</span></div>
                        </div>
                      </Link>
                      <Link className="btn-outline" to={`/agents/${encodeURIComponent(bien.agentEmail)}`} style={{ display: 'block', textAlign: 'center', marginTop: 12 }}>
                        Consulter les autres biens de cet agent
                      </Link>
                      {user && agentId && (
                        <button className="linklike muted" style={{ fontSize: 12, marginTop: 16 }} onClick={() => setSignalerOpen(true)}>⚑ Signaler cet agent</button>
                      )}
                    </div>
                  </aside>
                </div>

                {/* Biens similaires */}
                {similar.length > 0 && (
                  <>
                    <div className="sec-head" style={{ marginTop: 70 }}>
                      <div><span className="eyebrow">À découvrir aussi</span><h2 className="serif">Biens similaires</h2></div>
                      <Link className="sec-link" to={`/biens?categorie=${bien.categorie}`}>Voir plus</Link>
                    </div>
                    <div className="mini-grid">{similar.map((b) => <ListingCard key={b.id} bien={b} />)}</div>
                  </>
                )}

                <VisiteModal open={visiteOpen} onClose={() => setVisiteOpen(false)} bien={bien} clientEmail={user?.email} prixVisite={prixVisite ?? PRIX_VISITE_DEFAUT[bien.categorie]} />
                <ReservationModal open={resaOpen} onClose={() => setResaOpen(false)} bien={bien} />
                <SignalerModal open={signalerOpen} onClose={() => setSignalerOpen(false)} agentId={agentId} agentNom={agentNom || bien.agentEmail} />
                {lightbox >= 0 && <Lightbox images={gallery} start={Math.min(lightbox, gallery.length - 1)} onClose={() => setLightbox(-1)} />}
              </>
            )}
          </AsyncState>
        </div>
      </div>
    </div>
  )
}
