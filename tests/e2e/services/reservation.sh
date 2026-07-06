#!/usr/bin/env bash
# E2E service RESERVATION (8086) : création -> paiement REUSSI -> CONFIRME -> bien LOUE + vérif interaction.
set -uo pipefail
DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"; source "$DIR/../lib.sh"

section "RESERVATION ($PORT_RESERVATION)"
AG="res_ag${TS}@t.com"; CL="res_cl${TS}@t.com"
AGT=$(register_and_login "$AG" AGENT); CLT=$(register_and_login "$CL" CLIENT)
AID=$(id_of "$AG"); CID=$(id_of "$CL")
BID=$(creer_bien "$AGT" VILLA)

RID=$(curl -s -X POST http://$H:$PORT_RESERVATION/api/reservations -H "Authorization: Bearer $CLT" -H 'Content-Type: application/json' \
  -d "{\"propertyId\":$BID,\"startDate\":\"2027-10-01\",\"endDate\":\"2027-12-01\",\"operateur\":\"MTN\",\"telephone\":\"690000002\"}" | jget id)
check "créer réservation -> id"          "200" "$([ -n "$RID" ] && echo 200 || echo 000)"
check "créer sans token -> 401/403"      "401|403" "$(code -X POST http://$H:$PORT_RESERVATION/api/reservations -H 'Content-Type: application/json' -d "{\"propertyId\":$BID,\"startDate\":\"2027-10-01\",\"endDate\":\"2027-12-01\",\"operateur\":\"MTN\",\"telephone\":\"1\"}")"
sleep 1
check "confirmation -> bien LOUE"        "LOUE" "$(curl -s http://$H:$PORT_BIEN/api/biens/$BID | jget statut)"
check "get réservation (auth)"           "200" "$(code http://$H:$PORT_RESERVATION/api/reservations/$RID -H "Authorization: Bearer $CLT")"
check "vérif interaction (CONFIRME)"     "200" "$(code "http://$H:$PORT_RESERVATION/api/reservations/verification?utilisateurId=$CID&agentId=$AID")"

summary
