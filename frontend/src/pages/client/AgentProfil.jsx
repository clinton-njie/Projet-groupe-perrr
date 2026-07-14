import { useState, useMemo } from 'react'
import { Link, useParams, useNavigate } from 'react-router-dom'
import { Avatar, StarsStatic, isRealImg } from '../../components/ui.jsx'
import { AsyncState } from '../../components/states.jsx'
import ListingCard from '../../components/ListingCard.jsx'
import { useApi } from '../../hooks/useApi.js'
import { useAuth } from '../../context/AuthContext.jsx'
import { usersApi } from '../../api/users.js'
import { avisApi } from '../../api/avis.js'
import { biensApi } from '../../api/biens.js'

export default function AgentProfil() {
  const { email } = useParams()
  const nav = useNavigate()
  const { user } = useAuth()
  const [ville, setVille] = useState('Toutes')

  const { data, loading, error, reload } = useApi(async () => {
    const id = await usersApi.idByEmail(email)
    const [profile, avis, allBiens] = await Promise.all([
      usersApi.getProfile(id).catch(() => null),
      avisApi.agent(id).catch(() => null),
      biensApi.list(),
    ])
    const biens = (Array.isArray(allBiens) ? allBiens : []).filter((b) => b.agentEmail === email)
    return { id, profile, avis, biens }
  }, [email])

  const profile = data?.profile
  const avis = data?.avis
  const biens = data?.biens || []
  const nom = profile ? `${profile.firstname || ''} ${profile.lastname || ''}`.trim() || email : email

  const villes = useMemo(() => ['Toutes', ...Array.from(new Set(biens.map((b) => b.ville).filter(Boolean)))], [biens])
  const visible = ville === 'Toutes' ? biens : biens.filter((b) => b.ville === ville)

  // Contacter l'agent : conversation rattachée à un bien → on utilise son 1er bien comme contexte.
  const isSelf = user?.email === email
  const contactBien = biens[0]
  const contacter = () => {
    if (!user) return nav('/connexion', { state: { from: `/agents/${encodeURIComponent(email)}` } })
    if (!contactBien) return
    nav('/messagerie', { state: { receiverId: data.id, bienId: contactBien.id, bienNom: contactBien.nom } })
  }

  return (
    <div className="page active client-inner">
      <div className="inner">
        <div className="wrap">
          <div className="crumb"><Link to="/">Accueil</Link> / <Link to="/biens">Biens</Link> / <b>Agent</b></div>

          <AsyncState loading={loading} error={error} onRetry={reload} empty={!loading && !data} emptyTitle="Agent introuvable">
            {data && (
              <>
                {/* En-tête agent */}
                <div className="agent-hero">
                  {isRealImg(profile?.photoProfil)
                    ? <div className="agent-hero-av" style={{ backgroundImage: `url(${profile.photoProfil})` }} />
                    : <div className="agent-hero-av"><Avatar seed={email} size={110} /></div>}
                  <div className="agent-hero-info">
                    <span className="eyebrow">Agent ImmoAPP</span>
                    <h1 className="serif">{nom}</h1>
                    <div className="agent-hero-meta">
                      {profile?.ville && <span>{profile.ville}, {profile.pays || 'Cameroun'}</span>}
                      <span className="verif">✔ Agent vérifié</span>
                    </div>
                    <div className="agent-hero-stats">
                      <div className="ahs">
                        <div className="v serif">{(avis?.noteMoyenneGlobale ?? 0).toFixed(1)}</div>
                        <div className="l"><StarsStatic note={avis?.noteMoyenneGlobale || 0} /> · {(avis?.avis?.length ?? 0)} avis</div>
                      </div>
                      <div className="ahs">
                        <div className="v serif">{biens.length}</div>
                        <div className="l">bien{biens.length > 1 ? 's' : ''} en ligne</div>
                      </div>
                    </div>
                    {profile?.bio && <p className="agent-hero-bio">{profile.bio}</p>}
                    {!isSelf && (
                      <button className="btn-gold" style={{ marginTop: 20, border: 'none' }} onClick={contacter} disabled={!contactBien}>
                        Contacter l'agent
                      </button>
                    )}
                  </div>
                </div>

                {/* Ses biens */}
                <div className="sec-head" style={{ marginTop: 20 }}>
                  <div><span className="eyebrow">Ses annonces</span><h2 className="serif">Biens de {profile?.firstname || 'cet agent'}</h2></div>
                </div>

                {villes.length > 2 && (
                  <div className="pill-row" style={{ marginBottom: 26 }}>
                    {villes.map((v) => <button key={v} className={`pill ${ville === v ? 'on' : ''}`} onClick={() => setVille(v)}>{v}</button>)}
                  </div>
                )}

                {visible.length === 0
                  ? <p className="muted" style={{ paddingBottom: 40 }}>Aucun bien pour ce filtre.</p>
                  : <div className="flist-grid">{visible.map((b) => <ListingCard key={b.id} bien={b} />)}</div>}
              </>
            )}
          </AsyncState>
        </div>
      </div>
    </div>
  )
}
