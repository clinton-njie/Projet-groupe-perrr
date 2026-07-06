#!/usr/bin/env bash
# E2E service VISITE (8085) : cycle escrow (création -> PAYEE -> terminer -> TERMINE) + vérif interaction.
set -uo pipefail
DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"; source "$DIR/../lib.sh"

section "VISITE ($PORT_VISITE)"
AG="vis_ag${TS}@t.com"; CL="vis_cl${TS}@t.com"
AGT=$(register_and_login "$AG" AGENT); CLT=$(register_and_login "$CL" CLIENT)
AID=$(id_of "$AG"); CID=$(id_of "$CL")
BID=$(creer_bien "$AGT")

VID=$(curl -s -X POST http://$H:$PORT_VISITE/api/visites -H 'Content-Type: application/json' \
  -d "{\"bienId\":$BID,\"clientEmail\":\"$CL\",\"dateHeureDebut\":\"2027-09-10T10:00:00\",\"dateHeureFin\":\"2027-09-10T11:00:00\",\"operateur\":\"ORANGE\",\"telephone\":\"690000001\"}" | jget id)
check "créer visite -> id"                 "200" "$([ -n "$VID" ] && echo 200 || echo 000)"
sleep 1
check "paiement escrow -> PAYEE"           "PAYEE" "$(curl -s http://$H:$PORT_VISITE/api/visites/$VID | jget statut)"
check "get visite par id"                  "200" "$(code http://$H:$PORT_VISITE/api/visites/$VID)"
check "terminer -> TERMINE"                "TERMINE" "$(curl -s -X PUT http://$H:$PORT_VISITE/api/visites/$VID/terminer | jget statut)"
check "vérif interaction (existe)"         "200" "$(code "http://$H:$PORT_VISITE/api/visites/verification?utilisateurId=$CID&agentId=$AID")"
check "visites d'un client"                "200" "$(code http://$H:$PORT_VISITE/api/visites/user/$CL)"

summary
