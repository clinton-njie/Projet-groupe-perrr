#!/usr/bin/env bash
# FLUX COMPLET (abonnement) : auth + user + abonnement + Kafka (paiement-traite).
# Souscription -> EN_ATTENTE_PAIEMENT -> résultat paiement via Kafka -> ACTIF.
set -uo pipefail
DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"; source "$DIR/../lib.sh"

_c "===== FLUX ABONNEMENT (Kafka async) ====="
AG="fa_ag${TS}@t.com"
AGT=$(register_and_login "$AG" AGENT)
AID=$(id_of "$AG")

ABO=$(curl -s -X POST http://$H:$PORT_ABONNEMENT/api/abonnements -H "Authorization: Bearer $AGT" -H 'Content-Type: application/json' -d '{"type":"MENSUEL","modePaiement":"MOBILE_MONEY"}')
RID=$(echo "$ABO" | jget requestId)
check "1. souscription -> EN_ATTENTE_PAIEMENT" "EN_ATTENTE_PAIEMENT" "$(echo "$ABO" | jget statut)"

curl -s -X POST http://$H:$PORT_ABONNEMENT/api/interne/abonnements/simuler-paiement -H 'Content-Type: application/json' \
  -d "{\"requestId\":\"$RID\",\"paiementId\":1,\"statut\":\"SUCCESS\",\"reference\":\"TX-$TS\"}" >/dev/null
echo "   (attente consommation Kafka paiement-traite...)"; sleep 5
STAT=$(curl -s http://$H:$PORT_ABONNEMENT/api/abonnements/mon-statut -H "Authorization: Bearer $AGT")
check "2. paiement confirmé (Kafka) -> ACTIF"  "ACTIF" "$(echo "$STAT" | jget statut)"
check "3. abonnement actif (dates calculées)"  "true|True" "$(echo "$STAT" | jget actif)"
check "4. statut interne consultable"          "200" "$(code http://$H:$PORT_ABONNEMENT/api/interne/abonnements/utilisateur/$AID/statut)"

summary
