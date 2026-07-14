import { Link } from 'react-router-dom'

export default function Footer() {
  return (
    <footer>
      <div className="wrap">
        <div className="foot-top">
          <div>
            <div className="foot-brand">Immo<span>APP</span></div>
            <p>La plateforme immobilière qui met en relation clients, agents et propriétaires au Cameroun — visites, réservations et paiements sécurisés via Mobile Money.</p>
          </div>
          <div className="foot-col">
            <h4>Explorer</h4>
            <ul>
              <li><Link to="/biens">Tous les biens</Link></li>
              <li><Link to="/biens?categorie=APPARTEMENT">Appartements</Link></li>
              <li><Link to="/biens?categorie=VILLA">Villas</Link></li>
              <li><Link to="/biens?categorie=TERRAIN">Terrains</Link></li>
              <li><Link to="/agents">Nos agents</Link></li>
            </ul>
          </div>
          <div className="foot-col">
            <h4>Compte</h4>
            <ul>
              <li><Link to="/connexion">Connexion</Link></li>
              <li><Link to="/inscription">Inscription</Link></li>
              <li><Link to="/profil">Mon profil</Link></li>
              <li><Link to="/devenir-agent">Devenir agent</Link></li>
            </ul>
          </div>
          <div className="foot-col">
            <h4>Espaces</h4>
            <ul>
              <li><Link to="/agent">Espace agent</Link></li>
              <li><Link to="/admin">Espace admin</Link></li>
              <li><Link to="/faq">FAQ / Aide</Link></li>
            </ul>
          </div>
        </div>
        <div className="foot-bot">
          <span>© 2026 ImmoAPP — Tous droits réservés.</span>
          <span>Douala · Yaoundé · Cameroun</span>
        </div>
      </div>
    </footer>
  )
}
