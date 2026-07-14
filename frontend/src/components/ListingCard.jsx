import { Link } from 'react-router-dom'
import { Ph } from './ui.jsx'
import { CATEGORIE_LABEL, STATUT_LABEL, fmtPrix, specsBien, seedOf } from '../lib/constants.js'

// Carte de bien façon "Barnes" : photo 4/5, tag statut, prix doré.
export default function ListingCard({ bien }) {
  return (
    <Link className="listing" to={`/biens/${bien.id}`}>
      <div className="listing-media">
        <Ph seed={seedOf(bien)} url={bien.photoPrincipale} />
        <span className="listing-tag">{STATUT_LABEL[bien.statut] || bien.statut}</span>
      </div>
      <div className="listing-body">
        <div className="listing-meta">{CATEGORIE_LABEL[bien.categorie] || bien.categorie} · {bien.quartier}, {bien.ville}</div>
        <div className="listing-name serif">{bien.nom}</div>
        <div className="listing-foot">
          <div className="listing-price">
            {bien.categorie === 'TERRAIN'
              ? <><b>{fmtPrix(bien.prixVente || bien.prixMensuel)}</b> <small>FCFA</small></>
              : <><b>{fmtPrix(bien.prixMensuel)}</b> <small>FCFA / mois</small></>}
          </div>
          <div className="listing-specs">{specsBien(bien)}</div>
        </div>
      </div>
    </Link>
  )
}
