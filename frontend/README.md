# ImmoAPP — Front-end (React + Vite, PWA)

Front-end **branché au back-end en ligne** (`https://immobapp.duckdns.org`).
Inscription, connexion, biens, visites, réservations, paiement Mobile Money,
avis, messagerie, et gestion selon le rôle — de bout en bout.

## Démarrer

```bash
cd frontend
cp .env.example .env      # valeurs par défaut déjà correctes
npm install
npm run dev               # http://localhost:5173
```

Build & prévisualisation :

```bash
npm run build             # génère dist/ + service worker PWA
npm run preview
```

## Variables d'environnement (`.env`)

| Variable | Rôle | Défaut |
|----------|------|--------|
| `VITE_API_URL` | Base de l'API (gateway VPS) | `https://immobapp.duckdns.org` |
| `VITE_MESSAGING_URL` | Service messagerie (routé par la gateway sur le VPS) | idem |
| `VITE_PROXY_TARGET` | (Optionnel) cible du proxy de dev | = `VITE_API_URL` |

## CORS / proxy — important

Le back **n'a pas** le CORS activé. Pour éviter tout blocage navigateur **sans
modifier le back** :

- **En dev**, le serveur Vite **proxifie** `/api`, `/payments`, `/signalements`,
  `/visits`, `/admin` vers `VITE_API_URL`. Le navigateur ne parle qu'à
  `localhost:5173` (même origine) → **aucune erreur CORS**. Rien à configurer.
- **En prod**, il faut soit servir le front derrière le **même domaine** que
  l'API, soit **activer le CORS** côté back. Voir **[CORS-PRODUCTION.md](CORS-PRODUCTION.md)**
  (à activer manuellement — le front n'y touche pas).

`/admin` est à la fois une route de l'app **et** un préfixe back : le proxy ne
relaie que les **appels API** (Accept `application/json`) et laisse les
**navigations** afficher l'app.

## Compte de démonstration (fourni par la doc back)

- **Admin** : `admin@immobapp.com` / `Admin@Immo2026!`
- Ou **inscrivez-vous** (rôle client), puis demandez à devenir agent, etc.

## Architecture `src/`

```
api/         axios (http.js : Bearer + refresh 401) + un module par service
             (auth, users, biens, visites, reservations, abonnements, paiement,
              avis, signalements, messages, notifications, admin)
context/     AuthContext (login/register/logout, /me, refresh, rôles, authUserId)
routes/      AppRouter + ProtectedRoute (protection par rôle)
hooks/       useApi (loading/error), usePwa (online, install), useFavorites
components/  Nav, Footer, layouts (client/agent/admin), modales, messagerie, états
pages/       client/ agent/ admin/
lib/         constants (enums/labels), photo (placeholders dégradés)
styles/      immoapp.css (design repris de la maquette) + overrides
```

## Client HTTP & sécurité

- Instance **axios** `baseURL = VITE_API_URL` (relatif en dev → proxy).
- Intercepteur **requête** : ajoute `Authorization: Bearer <accessToken>`.
- Intercepteur **réponse** : sur **401**, rafraîchit via `/api/auth/refresh-token`,
  rejoue la requête ; si le refresh échoue → purge des tokens + redirection
  `/connexion`.
- Les **tokens ne sont jamais journalisés**. Auth & paiement ne sont **jamais**
  mis en cache par le service worker.

## PWA

- Manifest « ImmoAPP » (`theme_color #102C5C`, icônes 192/512 + maskable,
  `display: standalone`), **bouton d'installation** (`beforeinstallprompt`).
- **Precache** de l'app shell ; **network-first** pour les API ; **network-only**
  pour auth/paiement/abonnements ; images Cloudinary en cache-first.
- **Bandeau « Pas de connexion »** quand le réseau est absent.

## Règles métier (conformes au back, vérifiées via `/docs` + API réelle)

- Auth : `login → {accessToken, refreshToken, email, role}`, `/me → {email, roles[]}`
  (pas d'`id` → résolu via `GET /api/users/by-email/{email}`).
- Biens : **upload Cloudinary d'abord** (`POST /api/biens/images/upload`, champ
  `file` → `{secureUrl, publicId}`), puis création ; champ `nombrePiecesDisponibles`
  **seulement si CITE**.
- Visites : payantes (Mobile Money), annulation verrouillée < 24 h.
- Réservations : par période, montant calculé côté serveur.
- Abonnements : MENSUEL 10 000 / ANNUEL 100 000 FCFA, paiement **asynchrone**.
- Avis : 6 critères (1–5) via `POST /api/avis/transaction` + note plateforme.
- Messagerie : REST **sans temps réel** (bouton **Actualiser**), 1 conversation
  par bien.

## Correctifs back-end — déployés ✅

Les 5 limites back initialement signalées ont été corrigées côté VPS (livrées via
`../livraison-back-vps/`) et **vérifiées en ligne** :

1. **CORS** activé sur la gateway (origine du front autorisée, préflight OK).
2. **Notifications** : `GET /api/notifications/me` → le panneau 🔔 liste les
   notifications de l'utilisateur courant.
3. **Réservations agent** : `GET /api/reservations/agent` → l'écran agent liste les
   réservations reçues sur ses biens.
4. **Photo de profil (client)** : upload autorisé aux utilisateurs authentifiés
   (`POST /api/biens/images/upload` → 201) + `PUT /api/users/{id}` avec vérification
   d'appartenance → un client change sa propre photo.
5. **Demandes de rôle (admin)** : `GET /admin/role-requests` → 200 ; l'écran admin
   « Demandes de rôle » affiche et traite les demandes.

> Le proxy Vite reste actif en dev (pratique), mais le CORS étant désormais activé,
> un build de prod peut aussi appeler l'API en direct via `VITE_API_URL`.
