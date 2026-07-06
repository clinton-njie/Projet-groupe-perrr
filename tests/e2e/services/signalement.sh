#!/usr/bin/env bash
# E2E service SIGNALEMENTS (8095) : objets, règles (interaction, doublon), sanction -> bannissement.
set -uo pipefail
DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"; source "$DIR/../lib.sh"

section "SIGNALEMENTS ($PORT_SIGNALEMENTS)"
AG="sig_ag${TS}@t.com"; CL="sig_cl${TS}@t.com"; AD="sig_ad${TS}@t.com"
AGT=$(register_and_login "$AG" AGENT); CLT=$(register_and_login "$CL" CLIENT); ADT=$(register_and_login "$AD" ADMIN)
AID=$(id_of "$AG"); CID=$(id_of "$CL")
BID=$(creer_bien "$AGT")

OBJ_AG=$(curl -s "http://$H:$PORT_SIGNALEMENTS/signalements/objets?type=AGENT" -H "Authorization: Bearer $CLT" | python3 -c "import sys,json;d=json.load(sys.stdin);print(d[0]['id'] if d else '')")
OBJ_TE=$(curl -s "http://$H:$PORT_SIGNALEMENTS/signalements/objets?type=TECHNIQUE" -H "Authorization: Bearer $CLT" | python3 -c "import sys,json;d=json.load(sys.stdin);print(d[0]['id'] if d else '')")
check "lister objets AGENT (motifs seedés)"   "200" "$([ -n "$OBJ_AG" ] && echo 200 || echo 000)"

check "signalement AGENT sans interaction"    "403" "$(code -X POST http://$H:$PORT_SIGNALEMENTS/signalements -H "Authorization: Bearer $CLT" -H 'Content-Type: application/json' -d "{\"type\":\"AGENT\",\"objetSignalementId\":$OBJ_AG,\"agentId\":$AID,\"description\":\"x\"}")"

# Interaction : réservation CONFIRME
curl -s -X POST http://$H:$PORT_RESERVATION/api/reservations -H "Authorization: Bearer $CLT" -H 'Content-Type: application/json' \
  -d "{\"propertyId\":$BID,\"startDate\":\"2027-08-01\",\"endDate\":\"2027-10-01\",\"operateur\":\"MTN\",\"telephone\":\"690000006\"}" >/dev/null
sleep 1
check "signalement AGENT avec interaction"    "201" "$(code -X POST http://$H:$PORT_SIGNALEMENTS/signalements -H "Authorization: Bearer $CLT" -H 'Content-Type: application/json' -d "{\"type\":\"AGENT\",\"objetSignalementId\":$OBJ_AG,\"agentId\":$AID,\"description\":\"probleme\"}")"
check "doublon EN_ATTENTE même agent+objet"   "409" "$(code -X POST http://$H:$PORT_SIGNALEMENTS/signalements -H "Authorization: Bearer $CLT" -H 'Content-Type: application/json' -d "{\"type\":\"AGENT\",\"objetSignalementId\":$OBJ_AG,\"agentId\":$AID,\"description\":\"encore\"}")"
check "signalement TECHNIQUE"                 "201" "$(code -X POST http://$H:$PORT_SIGNALEMENTS/signalements -H "Authorization: Bearer $CLT" -H 'Content-Type: application/json' -d "{\"type\":\"TECHNIQUE\",\"objetSignalementId\":$OBJ_TE,\"description\":\"bug\"}")"

# Admin traite avec sanction -> bannissement de l'agent
SIG_ID=$(curl -s http://$H:$PORT_SIGNALEMENTS/signalements -H "Authorization: Bearer $ADT" | python3 -c "import sys,json
d=json.load(sys.stdin); v=[s for s in d if s.get('type')=='AGENT' and s.get('agentEmail')=='$AG' and s.get('statut')=='EN_ATTENTE']
print(v[0]['id'] if v else '')")
check "lister signalements (admin)"           "200" "$(code http://$H:$PORT_SIGNALEMENTS/signalements -H "Authorization: Bearer $ADT")"
check "sanctionner (TERMINE_AVEC_SANCTION)"   "200" "$(code -X PUT http://$H:$PORT_SIGNALEMENTS/signalements/$SIG_ID/statut -H "Authorization: Bearer $ADT" -H 'Content-Type: application/json' -d '{"statut":"TERMINE_AVEC_SANCTION"}')"
sleep 2
check "agent banni (compte désactivé)"        "false|False" "$(curl -s http://$H:$PORT_USER/api/users/$AID | jget isActive)"
check "re-transition interdite (hors EN_ATTENTE)" "409" "$(code -X PUT http://$H:$PORT_SIGNALEMENTS/signalements/$SIG_ID/statut -H "Authorization: Bearer $ADT" -H 'Content-Type: application/json' -d '{"statut":"TERMINE_SANS_SANCTION"}')"

summary
