# Tests E2E — Plateforme Immobilière

Scripts de test end-to-end qui exercent les **vrais services** via HTTP (aucun mock).
Chaque service peut être testé **indépendamment**, et des scripts de **flux** valident
plusieurs services ensemble (parcours complets).

## Arborescence

```
tests/e2e/
├── lib.sh                       # helpers communs (curl, JSON, assertions, comptes)
├── run_all.sh                   # lance toute la suite
├── services/                    # 1 fichier par microservice (test indépendant)
│   ├── auth.sh          (8081)
│   ├── user.sh          (8082)
│   ├── admin.sh         (8083)
│   ├── bien.sh          (8084)
│   ├── visite.sh        (8085)
│   ├── reservation.sh   (8086)
│   ├── abonnement.sh    (8087)
│   ├── paiement.sh      (8088)
│   ├── messagerie.sh    (8089)
│   ├── avis.sh          (8090)
│   ├── notification.sh  (8091)
│   └── signalement.sh   (8095)
└── flux/                        # scénarios multi-services (flux complets)
    ├── flux_visite_notation.sh          # bien → visite → paiement → Kafka → avis → note agent
    ├── flux_reservation_notation.sh     # bien → réservation → Kafka → avis → note agent
    ├── flux_signalement_bannissement.sh # interaction → signalement → sanction admin → ban
    ├── flux_abonnement_kafka.sh         # souscription → Kafka paiement-traite → ACTIF
    └── flux_complet.sh                  # parcours transversal de TOUS les services
```

## Prérequis

1. **La stack doit tourner** (tous les conteneurs UP) :
   ```bash
   docker compose up -d          # (v2 ; PAS docker-compose v1)
   ```
   Attendre ~1 min que les services s'enregistrent dans Eureka et que les bases soient prêtes.

2. Outils locaux : **bash**, **curl**, **python3** (pour parser le JSON — `jq` n'est pas requis).

## Lancer les tests

Depuis la racine du projet ou n'importe où (les chemins sont résolus automatiquement) :

```bash
# Toute la suite (services + flux)
./tests/e2e/run_all.sh

# Seulement les tests par service
./tests/e2e/run_all.sh services

# Seulement les flux multi-services
./tests/e2e/run_all.sh flux

# Un service précis
./tests/e2e/services/visite.sh

# Un flux précis
./tests/e2e/flux/flux_complet.sh
```

## Lire les résultats

Chaque vérification affiche `OK` (vert) ou `KO` (rouge) avec le code/valeur obtenu :

```
### VISITE (8085) ###
  OK  créer visite -> id (200)
  OK  paiement escrow -> PAYEE (PAYEE)
  OK  terminer -> TERMINE (TERMINE)
------------------------------------------
RESULTAT : 6 OK / 0 KO
------------------------------------------
```

- Chaque script se termine avec le **code de sortie 0** si tout est vert, **1** sinon
  (utilisable en CI : `./tests/e2e/run_all.sh && echo OK`).
- `run_all.sh` affiche à la fin un **bilan global** (scripts réussis / échoués).

## Notes

- Les scripts créent des comptes jetables à chaque exécution (emails horodatés) : ils sont
  **ré-exécutables** sans nettoyage.
- Les flux Kafka (avis, abonnement) incluent une petite attente (`sleep`) pour laisser le
  consumer traiter l'événement.
- **Surcharger l'hôte/les ports** (par ex. pour passer par la Gateway) via variables d'env :
  ```bash
  H=localhost PORT_VISITE=8222 ./tests/e2e/services/visite.sh
  ```
- Le test `abonnement`/`flux_abonnement_kafka` utilise l'endpoint interne
  `POST /api/interne/abonnements/simuler-paiement` pour injecter le résultat de paiement
  (le service de paiement réel n'émet pas encore l'event `paiement-traite`).
