#!/usr/bin/env bash
# FLUX COMPLET (signalement) : auth + user + bien + réservation(interaction) + signalements + admin(ban).
# Interaction -> signalement d'agent -> traitement admin avec sanction -> compte agent désactivé.
set -uo pipefail
DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"; source "$DIR/../lib.sh"

_c "===== FLUX SIGNALEMENT -> BANNISSEMENT ====="
AG="fs_ag${TS}@t.com"; CL="fs_cl${TS}@t.com"; AD="fs_ad${TS}@t.com"
AGT=$(register_and_login "$AG" AGENT); CLT=$(register_and_login "$CL" CLIENT); ADT=$(register_and_login "$AD" ADMIN)
AID=$(id_of "$AG")
BID=$(creer_bien "$AGT")

OBJ=$(curl -s "http://$H:$PORT_SIGNALEMENTS/signalements/objets?type=AGENT" -H "Authorization: Bearer $CLT" | python3 -c "import sys,json;d=json.load(sys.stdin);print(d[0]['id'] if d else '')")

check "1. refus signalement sans interaction" "403" "$(code -X POST http://$H:$PORT_SIGNALEMENTS/signalements -H "Authorization: Bearer $CLT" -H 'Content-Type: application/json' -d "{\"type\":\"AGENT\",\"objetSignalementId\":$OBJ,\"agentId\":$AID,\"description\":\"x\"}")"

# Interaction : réservation CONFIRME
curl -s -X POST http://$H:$PORT_RESERVATION/api/reservations -H "Authorization: Bearer $CLT" -H 'Content-Type: application/json' \
  -d "{\"propertyId\":$BID,\"startDate\":\"2027-08-01\",\"endDate\":\"2027-10-01\",\"operateur\":\"MTN\",\"telephone\":\"690777888\"}" >/dev/null
sleep 1
check "2. signalement créé après interaction" "201" "$(code -X POST http://$H:$PORT_SIGNALEMENTS/signalements -H "Authorization: Bearer $CLT" -H 'Content-Type: application/json' -d "{\"type\":\"AGENT\",\"objetSignalementId\":$OBJ,\"agentId\":$AID,\"description\":\"Comportement problematique\"}")"

SIG_ID=$(curl -s http://$H:$PORT_SIGNALEMENTS/signalements -H "Authorization: Bearer $ADT" | python3 -c "import sys,json
d=json.load(sys.stdin); v=[s for s in d if s.get('type')=='AGENT' and s.get('agentEmail')=='$AG' and s.get('statut')=='EN_ATTENTE']
print(v[0]['id'] if v else '')")
check "3. admin traite avec sanction"        "200" "$(code -X PUT http://$H:$PORT_SIGNALEMENTS/signalements/$SIG_ID/statut -H "Authorization: Bearer $ADT" -H 'Content-Type: application/json' -d '{"statut":"TERMINE_AVEC_SANCTION"}')"
sleep 2
check "4. agent banni (compte désactivé)"    "false|False" "$(curl -s http://$H:$PORT_USER/api/users/$AID | jget isActive)"

summary
