#!/usr/bin/env bash
# E2E service ABONNEMENT (8087) : workflow Kafka async (souscription -> EN_ATTENTE -> paiement -> ACTIF),
# rôle invalide, renouvellement interdit si actif, échec + réessai, stats/interne.
set -uo pipefail
DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"; source "$DIR/../lib.sh"

section "ABONNEMENT ($PORT_ABONNEMENT)"
AG="abo_ag${TS}@t.com"; CL="abo_cl${TS}@t.com"; AD="abo_ad${TS}@t.com"
AGT=$(register_and_login "$AG" AGENT); CLT=$(register_and_login "$CL" CLIENT); ADT=$(register_and_login "$AD" ADMIN)
AID=$(id_of "$AG")

ABO=$(curl -s -X POST http://$H:$PORT_ABONNEMENT/api/abonnements -H "Authorization: Bearer $AGT" -H 'Content-Type: application/json' -d '{"type":"MENSUEL","modePaiement":"MOBILE_MONEY"}')
ABID=$(echo "$ABO" | jget id); RID=$(echo "$ABO" | jget requestId)
check "souscrire (agent) -> EN_ATTENTE_PAIEMENT" "EN_ATTENTE_PAIEMENT" "$(echo "$ABO" | jget statut)"
check "souscrire par client -> 403"              "403" "$(code -X POST http://$H:$PORT_ABONNEMENT/api/abonnements -H "Authorization: Bearer $CLT" -H 'Content-Type: application/json' -d '{"type":"MENSUEL"}')"
check "mon statut avant paiement (actif=false)"  "false|False" "$(curl -s http://$H:$PORT_ABONNEMENT/api/abonnements/mon-statut -H "Authorization: Bearer $AGT" | jget actif)"

# Simulation du résultat de paiement (event Kafka paiement-traite) : SUCCESS
curl -s -X POST http://$H:$PORT_ABONNEMENT/api/interne/abonnements/simuler-paiement -H 'Content-Type: application/json' \
  -d "{\"requestId\":\"$RID\",\"paiementId\":1,\"statut\":\"SUCCESS\",\"reference\":\"TX-$TS\"}" >/dev/null
sleep 4
check "paiement confirmé (Kafka) -> ACTIF"       "ACTIF" "$(curl -s http://$H:$PORT_ABONNEMENT/api/abonnements/mon-statut -H "Authorization: Bearer $AGT" | jget statut)"
check "renouvellement si actif valide -> 400"    "400" "$(code -X POST http://$H:$PORT_ABONNEMENT/api/abonnements/renouveler -H "Authorization: Bearer $AGT" -H 'Content-Type: application/json' -d '{"type":"ANNUEL"}')"

# Deuxième souscription -> paiement ECHEC -> réessai
ABO2=$(curl -s -X POST http://$H:$PORT_ABONNEMENT/api/abonnements -H "Authorization: Bearer $AGT" -H 'Content-Type: application/json' -d '{"type":"ANNUEL"}')
AB2=$(echo "$ABO2" | jget id); RID2=$(echo "$ABO2" | jget requestId)
curl -s -X POST http://$H:$PORT_ABONNEMENT/api/interne/abonnements/simuler-paiement -H 'Content-Type: application/json' -d "{\"requestId\":\"$RID2\",\"statut\":\"ECHEC\",\"message\":\"Solde insuffisant\"}" >/dev/null
sleep 4
check "paiement échoué -> ECHEC_PAIEMENT"        "ECHEC_PAIEMENT" "$(curl -s http://$H:$PORT_ABONNEMENT/api/interne/abonnements/request/$RID2 | jget statut)"
check "réessai paiement (agent propriétaire)"    "200" "$(code -X POST http://$H:$PORT_ABONNEMENT/api/abonnements/$AB2/reessayer-paiement -H "Authorization: Bearer $AGT")"

check "mon historique (agent)"                   "200" "$(code http://$H:$PORT_ABONNEMENT/api/abonnements/mon-historique -H "Authorization: Bearer $AGT")"
check "stats globales (admin)"                   "200" "$(code http://$H:$PORT_ABONNEMENT/api/admin/abonnements/statistiques -H "Authorization: Bearer $ADT")"
check "statut interne d'un user"                 "200" "$(code http://$H:$PORT_ABONNEMENT/api/interne/abonnements/utilisateur/$AID/statut)"
check "count interne"                            "200" "$(code http://$H:$PORT_ABONNEMENT/api/interne/abonnements/count)"
check "health interne"                           "200" "$(code http://$H:$PORT_ABONNEMENT/api/interne/abonnements/health)"

summary
