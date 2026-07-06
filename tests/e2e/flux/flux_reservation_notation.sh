#!/usr/bin/env bash
# FLUX COMPLET (réservation) : auth + user + bien + réservation + paiement + Kafka + ms-avis.
# Réservation confirmée -> bien LOUE -> opportunité (Kafka) -> notation -> note agent.
set -uo pipefail
DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"; source "$DIR/../lib.sh"

_c "===== FLUX RESERVATION -> NOTATION ====="
AG="fr_ag${TS}@t.com"; CL="fr_cl${TS}@t.com"
AGT=$(register_and_login "$AG" AGENT); CLT=$(register_and_login "$CL" CLIENT)
AID=$(id_of "$AG"); CID=$(id_of "$CL")
echo "Agent=$AG (id=$AID)  Client=$CL (id=$CID)"

BID=$(creer_bien "$AGT" VILLA)
check "1. bien créé"                       "200" "$([ -n "$BID" ] && echo 200 || echo 000)"

RID=$(curl -s -X POST http://$H:$PORT_RESERVATION/api/reservations -H "Authorization: Bearer $CLT" -H 'Content-Type: application/json' \
  -d "{\"propertyId\":$BID,\"startDate\":\"2027-06-01\",\"endDate\":\"2027-09-01\",\"operateur\":\"MTN\",\"telephone\":\"690555666\"}" | jget id)
sleep 1
check "2. réservation confirmée -> bien LOUE" "LOUE" "$(curl -s http://$H:$PORT_BIEN/api/biens/$BID | jget statut)"

echo "   (attente consommation Kafka...)"; sleep 5
OPP=$(curl -s http://$H:$PORT_AVIS/api/avis/opportunites/$CID -H "Authorization: Bearer $CLT")
OPPID=$(echo "$OPP" | python3 -c "import sys,json
try:
  d=json.load(sys.stdin); v=[o for o in d if o.get('typeTransaction')=='RESERVATION' and o.get('bienId')==$BID]
  print(v[0]['id'] if v else '')
except Exception: print('')")
check "3. opportunité RESERVATION créée (Kafka)" "200" "$([ -n "$OPPID" ] && echo 200 || echo 000)"

if [ -n "$OPPID" ]; then
  check "4. le client note"                "200|201" "$(code -X POST http://$H:$PORT_AVIS/api/avis/transaction -H "Authorization: Bearer $CLT" -H 'Content-Type: application/json' -d "{\"opportuniteId\":$OPPID,\"noteCommunication\":4,\"noteProfessionnalisme\":5,\"noteDisponibilite\":4,\"noteConformiteAnnonce\":5,\"noteEtatGeneral\":4,\"noteRapportQualitePrix\":5,\"commentaire\":\"Location fluide\"}")"
fi
NOTE=$(curl -s http://$H:$PORT_AVIS/api/avis/agent/$AID | jget noteMoyenneGlobale)
check "5. note de l'agent consultable"     "200" "$([ -n "$NOTE" ] && echo 200 || echo 000)"
echo "   -> note moyenne globale de l'agent = $NOTE"

summary
