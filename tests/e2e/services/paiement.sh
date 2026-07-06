#!/usr/bin/env bash
# E2E service PAIEMENT (8088) : mode escrow VISITE (AUTORISE -> capturer/rembourser).
# Le paiement est déclenché en interne par visite/réservation ; on valide ici le cycle escrow.
set -uo pipefail
DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"; source "$DIR/../lib.sh"

section "PAIEMENT ($PORT_PAIEMENT)"
AG="pay_ag${TS}@t.com"; CL="pay_cl${TS}@t.com"
AGT=$(register_and_login "$AG" AGENT); register_and_login "$CL" CLIENT >/dev/null
BID=$(creer_bien "$AGT")

# Une visite déclenche le paiement escrow : AUTORISE -> visite PAYEE
VID=$(curl -s -X POST http://$H:$PORT_VISITE/api/visites -H 'Content-Type: application/json' \
  -d "{\"bienId\":$BID,\"clientEmail\":\"$CL\",\"dateHeureDebut\":\"2027-11-10T10:00:00\",\"dateHeureFin\":\"2027-11-10T11:00:00\",\"operateur\":\"ORANGE\",\"telephone\":\"690000003\"}" | jget id)
sleep 1
check "escrow AUTORISE (visite PAYEE)"   "PAYEE" "$(curl -s http://$H:$PORT_VISITE/api/visites/$VID | jget statut)"
# terminer -> capturer les fonds (transfert à l'agent)
check "capture des fonds (TERMINE)"      "TERMINE" "$(curl -s -X PUT http://$H:$PORT_VISITE/api/visites/$VID/terminer | jget statut)"

# Une autre visite puis annulation client -> remboursement
VID2=$(curl -s -X POST http://$H:$PORT_VISITE/api/visites -H 'Content-Type: application/json' \
  -d "{\"bienId\":$BID,\"clientEmail\":\"$CL\",\"dateHeureDebut\":\"2027-11-11T10:00:00\",\"dateHeureFin\":\"2027-11-11T11:00:00\",\"operateur\":\"ORANGE\",\"telephone\":\"690000004\"}" | jget id)
sleep 1
check "annulation client -> remboursement" "ANNULEE_CLIENT" "$(curl -s -X PUT http://$H:$PORT_VISITE/api/visites/$VID2/annuler | jget statut)"

summary
