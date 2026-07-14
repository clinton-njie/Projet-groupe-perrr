// Enums & libellés alignés sur le back-end. Pas de données métier ici.

export const CATEGORIES = ['APPARTEMENT', 'VILLA', 'CITE', 'TERRAIN']
export const CATEGORIE_LABEL = {
  APPARTEMENT: 'Appartement', VILLA: 'Villa', CITE: 'Cité / Studio', TERRAIN: 'Terrain',
}

export const STATUT_LABEL = { DISPONIBLE: 'Disponible', RESERVE: 'Réservé', LOUE: 'Loué' }
export const STATUT_BADGE = { DISPONIBLE: 'b-dispo', RESERVE: 'b-reserve', LOUE: 'b-loue' }

export const OPERATEURS = ['ORANGE', 'MTN']

export const VISITE_STATUT_LABEL = {
  EN_ATTENTE: 'En attente', PAYEE: 'Payée', EN_COURS: 'En cours', TERMINE: 'Terminée',
  ANNULEE_CLIENT: 'Annulée', ANNULEE_AGENT: 'Annulée (agent)', NON_PRESENTEE: 'Non présentée',
}
export const VISITE_STATUT_BADGE = {
  EN_ATTENTE: 'b-attente', PAYEE: 'b-paye', EN_COURS: 'b-reserve', TERMINE: 'b-loue',
  ANNULEE_CLIENT: 'b-inactif', ANNULEE_AGENT: 'b-inactif', NON_PRESENTEE: 'b-inactif',
}

export const RESA_STATUT_LABEL = { EN_ATTENTE: 'En attente', CONFIRME: 'Confirmée', ANNULE: 'Annulée', EXPIRE: 'Expirée' }
export const RESA_STATUT_BADGE = { EN_ATTENTE: 'b-attente', CONFIRME: 'b-paye', ANNULE: 'b-inactif', EXPIRE: 'b-inactif' }

export const ABO_STATUT_LABEL = {
  EN_ATTENTE_PAIEMENT: 'En attente de paiement', ACTIF: 'Actif', EXPIRE: 'Expiré', ANNULE: 'Annulé', ECHEC_PAIEMENT: 'Échec du paiement',
}
export const ABO_STATUT_BADGE = {
  EN_ATTENTE_PAIEMENT: 'b-attente', ACTIF: 'b-paye', EXPIRE: 'b-inactif', ANNULE: 'b-inactif', ECHEC_PAIEMENT: 'b-inactif',
}

export const ROLE_LABEL = {
  ROLE_CLIENT: 'Client', ROLE_AGENT: 'Agent', ROLE_ADMIN: 'Administrateur', ROLE_SUPER_ADMIN: 'Super-admin',
}
export const ROLE_BADGE = {
  ROLE_CLIENT: 'b-role-client', ROLE_AGENT: 'b-role-agent', ROLE_ADMIN: 'b-role-admin', ROLE_SUPER_ADMIN: 'b-role-super',
}

export const SIGNALEMENT_STATUT_LABEL = {
  EN_ATTENTE: 'En attente', TERMINE_AVEC_SANCTION: 'Sanctionné', TERMINE_SANS_SANCTION: 'Classé sans suite',
}
export const SIGNALEMENT_STATUT_BADGE = {
  EN_ATTENTE: 'b-attente', TERMINE_AVEC_SANCTION: 'b-inactif', TERMINE_SANS_SANCTION: 'b-loue',
}

// Prix de visite par défaut (seeder back) — la vraie valeur vient de l'API.
export const PRIX_VISITE_DEFAUT = { APPARTEMENT: 5000, VILLA: 10000, CITE: 3000, TERRAIN: 7000 }

// Critères de notation (ms-avis) — AvisTransactionDTO
export const CRITERES_AVIS = [
  { key: 'noteCommunication', label: 'Communication' },
  { key: 'noteProfessionnalisme', label: 'Professionnalisme' },
  { key: 'noteDisponibilite', label: 'Disponibilité' },
  { key: 'noteConformiteAnnonce', label: "Conformité de l'annonce" },
  { key: 'noteEtatGeneral', label: 'État général du bien' },
  { key: 'noteRapportQualitePrix', label: 'Rapport qualité / prix' },
]

export const PLANS = [
  { type: 'MENSUEL', prix: 10000, periode: 'mois', avantages: ['Publication de biens illimitée', 'Messagerie clients', 'Gestion des visites', 'Statistiques de base'] },
  { type: 'ANNUEL', prix: 100000, periode: 'an', feat: true, avantages: ['Tout le mensuel', '2 mois offerts', 'Mise en avant « À la une »', 'Badge agent vérifié', 'Support prioritaire'] },
]

export const fmtPrix = (n) => new Intl.NumberFormat('fr-FR').format(Number(n) || 0)

// Libellé d'un bien pour les specs de carte
export function specsBien(b) {
  if (!b) return ''
  if (b.categorie === 'TERRAIN') return `${b.surface || '—'} m²`
  if (b.categorie === 'CITE') return `${b.nombrePiecesDisponibles ?? '—'} pièces dispo.`
  return [b.chambres && `${b.chambres} ch`, b.sallesBain && `${b.sallesBain} sdb`, b.surface && `${b.surface} m²`].filter(Boolean).join(' · ')
}

// Graine de placeholder image déterministe pour un bien (avant vraie photo)
export const seedOf = (b) => `bien-${b?.id ?? b?.nom ?? 'x'}`
