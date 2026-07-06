#!/usr/bin/env bash
# FLUX COMPLET (visite) : auth + user + bien + visite + paiement(escrow) + Kafka + ms-avis.
# Un client visite un bien -> paiement escrow -> visite terminée -> opportunité (Kafka)
# -> le client note -> la note de l'agent devient consultable.
set -uo pipefail
DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"; source "$DIR/../lib.sh"

_c "===== FLUX VISITE -> NOTATION ====="
AG="fv_ag${TS}@t.com"; CL="fv_cl${TS}@t.com"
AGT=$(register_and_login "$AG" AGENT); CLT=$(register_and_login "$CL" CLIENT)
AID=$(id_of "$AG"); CID=$(id_of "$CL")
echo "Agent=$AG (id=$AID)  Client=$CL (id=$CID)"

BID=$(creer_bien "$AGT")
check "1. bien créé par l'agent"          "200" "$([ -n "$BID" ] && echo 200 || echo 000)"

VID=$(curl -s -X POST http://$H:$PORT_VISITE/api/visites -H 'Content-Type: application/json' \
  -d "{\"bienId\":$BID,\"clientEmail\":\"$CL\",\"dateHeureDebut\":\"2027-05-10T14:00:00\",\"dateHeureFin\":\"2027-05-10T15:00:00\",\"operateur\":\"ORANGE\",\"telephone\":\"690333444\"}" | jget id)
sleep 1
check "2. visite payée (escrow) -> PAYEE"  "PAYEE" "$(curl -s http://$H:$PORT_VISITE/api/visites/$VID | jget statut)"
check "3. visite terminée -> TERMINE"      "TERMINE" "$(curl -s -X PUT http://$H:$PORT_VISITE/api/visites/$VID/terminer | jget statut)"

echo "   (attente consommation Kafka transaction.terminee...)"; sleep 5
OPP=$(curl -s http://$H:$PORT_AVIS/api/avis/opportunites/$CID -H "Authorization: Bearer $CLT")
OPPID=$(echo "$OPP" | python3 -c "import sys,json
try:
  d=json.load(sys.stdin); v=[o for o in d if o.get('typeTransaction')=='VISITE' and o.get('bienId')==$BID]
  print(v[0]['id'] if v else '')
except Exception: print('')")
check "4. opportunité créée par ms-avis (Kafka)" "200" "$([ -n "$OPPID" ] && echo 200 || echo 000)"

if [ -n "$OPPID" ]; then
  check "5. le client note l'opportunité"  "200|201" "$(code -X POST http://$H:$PORT_AVIS/api/avis/transaction -H "Authorization: Bearer $CLT" -H 'Content-Type: application/json' -d "{\"opportuniteId\":$OPPID,\"noteCommunication\":5,\"noteProfessionnalisme\":4,\"noteDisponibilite\":5,\"noteConformiteAnnonce\":4,\"noteEtatGeneral\":5,\"noteRapportQualitePrix\":3,\"commentaire\":\"Agent tres pro\"}")"
fi
NOTE=$(curl -s http://$H:$PORT_AVIS/api/avis/agent/$AID | jget noteMoyenneGlobale)
check "6. note de l'agent consultable"     "200" "$([ -n "$NOTE" ] && echo 200 || echo 000)"
echo "   -> note moyenne globale de l'agent = $NOTE"

summary
