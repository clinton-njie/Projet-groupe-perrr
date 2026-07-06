#!/usr/bin/env bash
# E2E service MS-AVIS (8090) : opportunité (créée par Kafka après une visite) -> notation -> note agent.
set -uo pipefail
DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"; source "$DIR/../lib.sh"

section "MS-AVIS ($PORT_AVIS)"
AG="avis_ag${TS}@t.com"; CL="avis_cl${TS}@t.com"
AGT=$(register_and_login "$AG" AGENT); CLT=$(register_and_login "$CL" CLIENT)
AID=$(id_of "$AG"); CID=$(id_of "$CL")
BID=$(creer_bien "$AGT")

# Prérequis : une visite terminée -> event Kafka transaction.terminee -> ms-avis crée l'opportunité
VID=$(curl -s -X POST http://$H:$PORT_VISITE/api/visites -H 'Content-Type: application/json' \
  -d "{\"bienId\":$BID,\"clientEmail\":\"$CL\",\"dateHeureDebut\":\"2027-09-12T10:00:00\",\"dateHeureFin\":\"2027-09-12T11:00:00\",\"operateur\":\"ORANGE\",\"telephone\":\"690000005\"}" | jget id)
sleep 1
curl -s -X PUT http://$H:$PORT_VISITE/api/visites/$VID/terminer >/dev/null
echo "  (attente consommation Kafka...)"; sleep 5

OPP=$(curl -s http://$H:$PORT_AVIS/api/avis/opportunites/$CID -H "Authorization: Bearer $CLT")
OPPID=$(echo "$OPP" | python3 -c "import sys,json
try:
  d=json.load(sys.stdin); v=[o for o in d if o.get('bienId')==$BID]
  print(v[0]['id'] if v else '')
except Exception: print('')")
check "lire opportunités (client, 200)"     "200" "$([ -n "$OPP" ] && echo 200 || echo 000)"
check "opportunité VISITE créée par Kafka"  "200" "$([ -n "$OPPID" ] && echo 200 || echo 000)"
check "lire opportunités d'un autre (403)"  "403" "$(code http://$H:$PORT_AVIS/api/avis/opportunites/$AID -H "Authorization: Bearer $CLT")"
if [ -n "$OPPID" ]; then
  check "soumettre avis (client)"           "200|201" "$(code -X POST http://$H:$PORT_AVIS/api/avis/transaction -H "Authorization: Bearer $CLT" -H 'Content-Type: application/json' -d "{\"opportuniteId\":$OPPID,\"noteCommunication\":5,\"noteProfessionnalisme\":4,\"noteDisponibilite\":5,\"noteConformiteAnnonce\":4,\"noteEtatGeneral\":5,\"noteRapportQualitePrix\":4,\"commentaire\":\"Tres bien\"}")"
  check "doublon avis même opportunité"     "409" "$(code -X POST http://$H:$PORT_AVIS/api/avis/transaction -H "Authorization: Bearer $CLT" -H 'Content-Type: application/json' -d "{\"opportuniteId\":$OPPID,\"noteCommunication\":1,\"noteProfessionnalisme\":1,\"noteDisponibilite\":1,\"noteConformiteAnnonce\":1,\"noteEtatGeneral\":1,\"noteRapportQualitePrix\":1}")"
fi
check "note moyenne de l'agent"             "200" "$(code http://$H:$PORT_AVIS/api/avis/agent/$AID)"
check "avis d'un bien"                      "200" "$(code http://$H:$PORT_AVIS/api/avis/bien/$BID)"

summary
