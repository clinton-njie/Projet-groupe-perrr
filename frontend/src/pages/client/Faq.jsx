import { useState } from 'react'
import { Link } from 'react-router-dom'

const SECTIONS = [
  {
    titre: 'Compte & rôles',
    items: [
      ['Comment créer un compte ?', "Cliquez sur « Inscription », renseignez nom, prénom, e-mail et mot de passe (avec confirmation). Vous êtes inscrit comme client."],
      ['Comment devenir agent ?', "Depuis votre profil, cliquez sur « Devenir agent » et envoyez une demande avec une justification. Un administrateur la validera."],
      ['Quelle est la différence entre client et agent ?', "Le client consulte les biens, réserve des visites et loue. L'agent publie et gère ses propres biens, et dispose d'un tableau de bord."],
    ],
  },
  {
    titre: 'Visites & réservations',
    items: [
      ['Comment réserver une visite ?', "Sur la fiche d'un bien, cliquez sur « Réserver une visite », choisissez un créneau et payez les frais de visite via Mobile Money."],
      ['La visite est-elle payante ?', "Oui, un montant fixe selon la catégorie du bien (défini par la plateforme), réglé via Mobile Money. Ce paiement n'est pas remboursable."],
      ['Puis-je annuler une visite ?', "Une visite non encore payée peut être annulée. Une fois payée, elle n'est pas remboursable."],
      ['Comment réserver un logement ?', "Cliquez sur « Réserver le bien », choisissez la période : le montant (loyer × durée) est calculé automatiquement, puis réglé via Mobile Money."],
    ],
  },
  {
    titre: 'Paiement',
    items: [
      ['Quels moyens de paiement ?', "Le paiement se fait via Mobile Money : Orange Money ou MTN Mobile Money. Aucune carte bancaire nécessaire."],
      ['Mon argent est-il sécurisé ?', "Les paiements passent par Mobile Money de façon sécurisée. Le montant rémunère la prestation de l'agent et n'est pas remboursable."],
    ],
  },
  {
    titre: 'Agents & abonnement',
    items: [
      ['Comment publier un bien ?', "En tant qu'agent avec un abonnement actif, allez dans « Publier un bien », ajoutez photos et informations, et validez."],
      ["Combien coûte l'abonnement agent ?", "Mensuel 10 000 FCFA ou annuel 100 000 FCFA. Le paiement se fait via Mobile Money."],
    ],
  },
  {
    titre: 'Avis & messagerie',
    items: [
      ['Comment laisser un avis ?', "Après une transaction terminée, vous pouvez noter l'agent et le bien sur plusieurs critères, ou donner un avis sur la plateforme."],
      ['Comment contacter un agent ?', "Depuis la fiche d'un bien ou le profil de l'agent, cliquez sur « Contacter l'agent » pour démarrer une conversation."],
    ],
  },
]

function Item({ q, a }) {
  const [open, setOpen] = useState(false)
  return (
    <div className="faq-item">
      <button className="faq-q" onClick={() => setOpen((o) => !o)}>
        <span>{q}</span><span className="faq-ic">{open ? '−' : '+'}</span>
      </button>
      {open && <p className="faq-a">{a}</p>}
    </div>
  )
}

export default function Faq() {
  return (
    <div className="page active client-inner">
      <div className="inner">
        <div className="wrap" style={{ maxWidth: 900 }}>
          <div className="crumb"><Link to="/">Accueil</Link> / <b>FAQ</b></div>
          <span className="eyebrow">Centre d'aide</span>
          <h1 className="page-title serif" style={{ marginTop: 12, marginBottom: 8 }}>Questions fréquentes</h1>
          <p className="muted" style={{ marginBottom: 30 }}>Tout ce qu'il faut savoir pour utiliser ImmoAPP.</p>

          {SECTIONS.map((s) => (
            <div key={s.titre} style={{ marginBottom: 34 }}>
              <div className="d-sec-title">{s.titre}</div>
              {s.items.map(([q, a]) => <Item key={q} q={q} a={a} />)}
            </div>
          ))}

          <div className="becomes" style={{ marginTop: 20 }}>
            <div>
              <div className="bt serif">Une autre question ?</div>
              <p>Contactez un agent directement depuis la fiche d'un bien, ou créez votre compte pour commencer.</p>
            </div>
            <Link className="btn-gold" to="/biens">Explorer les biens</Link>
          </div>
        </div>
      </div>
    </div>
  )
}
