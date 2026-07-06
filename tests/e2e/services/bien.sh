#!/usr/bin/env bash
# E2E service BIEN (8084) : CRUD, contrôle de rôle, consultation publique, prix de visite.
set -uo pipefail
DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"; source "$DIR/../lib.sh"

section "BIEN ($PORT_BIEN)"
AGT=$(register_and_login "bien_ag${TS}@t.com" AGENT)
CLT=$(register_and_login "bien_cl${TS}@t.com" CLIENT)

BID=$(creer_bien "$AGT")
check "créer bien (agent)"             "200" "$([ -n "$BID" ] && echo 200 || echo 000)"
check "créer bien sans rôle (client)"  "401|403" "$(code -X POST http://$H:$PORT_BIEN/api/biens -H "Authorization: Bearer $CLT" -H 'Content-Type: application/json' -d '{"categorie":"VILLA","nom":"x","ville":"y","quartier":"z","photoPrincipale":"p","photosSecondaires":[],"prixMensuel":1,"nombrePiecesDisponibles":1,"description":"d"}')"
check "lister biens (public)"          "200" "$(code http://$H:$PORT_BIEN/api/biens)"
check "get bien par id"                "200" "$(code http://$H:$PORT_BIEN/api/biens/$BID)"
check "get bien inexistant"            "400|404|500" "$(code http://$H:$PORT_BIEN/api/biens/999999)"
check "biens disponibles"              "200" "$(code http://$H:$PORT_BIEN/api/biens/disponibles)"
check "biens par catégorie"            "200" "$(code http://$H:$PORT_BIEN/api/biens/categorie/APPARTEMENT)"
check "prix-visite par catégorie"      "200" "$(code http://$H:$PORT_BIEN/api/biens/categorie/APPARTEMENT/prix-visite)"

summary
