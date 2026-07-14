# CORS — à activer côté back-end pour la PRODUCTION

> ⚠️ **Ne pas appliquer automatiquement.** Ce fichier documente les en-têtes CORS
> à activer **manuellement** côté back (gateway ou reverse-proxy Caddy) quand le
> front sera hébergé sur un domaine différent de l'API. **Aucun fichier back-end
> ne doit être modifié par le front.**

## Pourquoi

En développement, le front n'a **pas** besoin de CORS : le serveur de dev **Vite
proxifie** les appels (`/api`, `/payments`, `/signalements`, `/visits`, `/admin`)
vers `https://immobapp.duckdns.org`. Le navigateur ne parle qu'à `localhost:5173`
(même origine) → pas de requête cross-origin.

En **production**, si le front est servi depuis un autre domaine que l'API
(ex. `https://app.immoapp.cm` ↔ `https://immobapp.duckdns.org`), le navigateur
effectue des requêtes **cross-origin** et exige des en-têtes CORS. Constat actuel
(vérifié) : l'API ne renvoie **aucun** en-tête `Access-Control-Allow-*`.

Deux solutions au choix :

- **A. Même origine (recommandé, aucun CORS)** — servir le build du front derrière
  le **même domaine** que l'API (Caddy sert les fichiers statiques et proxifie
  `/api`, `/payments`, `/signalements`, `/visits`, `/admin` vers la gateway).
- **B. Activer le CORS** — autoriser explicitement l'origine du front.

## En-têtes CORS attendus (solution B)

Autoriser l'origine exacte du front (adapter la valeur), les méthodes, les en-têtes
`Authorization` + `Content-Type`, et **répondre aux préflights `OPTIONS`** :

```
Access-Control-Allow-Origin: https://VOTRE-DOMAINE-FRONT
Access-Control-Allow-Methods: GET, POST, PUT, DELETE, PATCH, OPTIONS
Access-Control-Allow-Headers: Authorization, Content-Type
Access-Control-Allow-Credentials: false
Access-Control-Max-Age: 3600
Vary: Origin
```

Le préflight `OPTIONS` doit répondre **204** (ou 200) avec ces en-têtes, sans corps.

### Exemple — Caddy (reverse proxy)

```caddy
immobapp.duckdns.org {
    @cors_preflight method OPTIONS
    header {
        Access-Control-Allow-Origin "https://VOTRE-DOMAINE-FRONT"
        Access-Control-Allow-Methods "GET, POST, PUT, DELETE, PATCH, OPTIONS"
        Access-Control-Allow-Headers "Authorization, Content-Type"
        Access-Control-Max-Age "3600"
        Vary Origin
    }
    respond @cors_preflight 204

    reverse_proxy localhost:8222   # gateway
}
```

### Exemple — Spring Cloud Gateway (globalcors, à titre indicatif)

```yaml
spring:
  cloud:
    gateway:
      globalcors:
        cors-configurations:
          '[/**]':
            allowedOrigins: "https://VOTRE-DOMAINE-FRONT"
            allowedMethods: [GET, POST, PUT, DELETE, PATCH, OPTIONS]
            allowedHeaders: [Authorization, Content-Type]
            maxAge: 3600
```

## Côté front (une fois le CORS actif)

Le front lit `VITE_API_URL`. En build de production, il appelle directement cette
base. Aucune réécriture de code n'est nécessaire : il suffit que le CORS réponde
correctement (ou que le front soit servi en même origine — solution A).
