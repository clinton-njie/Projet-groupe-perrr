#!/usr/bin/env bash
# FLUX COMPLET DE LA PLATEFORME : un même trio (agent, client, admin) parcourt tous les services.
# Sert de smoke test transversal de bout en bout.
set -uo pipefail
DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"; source "$DIR/../lib.sh"

_c "===== FLUX COMPLET PLATEFORME ====="
AG="fc_ag${TS}@t.com"; CL="fc_cl${TS}@t.com"; AD="fc_ad${TS}@t.com"

section "Auth + User"
AGT=$(register_and_login "$AG" AGENT); CLT=$(register_and_login "$CL" CLIENT); ADT=$(register_and_login "$AD" ADMIN)
AID=$(id_of "$AG"); CID=$(id_of "$CL")
check "tokens obtenus (agent/client/admin)" "200" "$([ -n "$AGT" ] && [ -n "$CLT" ] && [ -n "$ADT" ] && echo 200 || echo 000)"
check "ids résolus (user-service)"          "200" "$([ -n "$AID" ] && [ -n "$CID" ] && echo 200 || echo 000)"

section "Bien"
BID=$(creer_bien "$AGT")
check "création bien (agent)"               "200" "$([ -n "$BID" ] && echo 200 || echo 000)"
check "consultation publique"               "200" "$(code http://$H:$PORT_BIEN/api/biens/$BID)"

section "Visite (escrow) + Paiement"
VID=$(curl -s -X POST http://$H:$PORT_VISITE/api/visites -H 'Content-Type: application/json' \
  -d "{\"bienId\":$BID,\"clientEmail\":\"$CL\",\"dateHeureDebut\":\"2027-09-10T10:00:00\",\"dateHeureFin\":\"2027-09-10T11:00:00\",\"operateur\":\"ORANGE\",\"telephone\":\"690000001\"}" | jget id)
sleep 1
check "visite PAYEE (escrow AUTORISE)"      "PAYEE"   "$(curl -s http://$H:$PORT_VISITE/api/visites/$VID | jget statut)"
check "visite TERMINE (capture)"            "TERMINE" "$(curl -s -X PUT http://$H:$PORT_VISITE/api/visites/$VID/terminer | jget statut)"

section "Réservation"
RID=$(curl -s -X POST http://$H:$PORT_RESERVATION/api/reservations -H "Authorization: Bearer $CLT" -H 'Content-Type: application/json' \
  -d "{\"propertyId\":$BID,\"startDate\":\"2027-10-01\",\"endDate\":\"2027-12-01\",\"operateur\":\"MTN\",\"telephone\":\"690000002\"}" | jget id)
sleep 1
check "réservation -> bien LOUE"            "LOUE" "$(curl -s http://$H:$PORT_BIEN/api/biens/$BID | jget statut)"

section "ms-avis (opportunité créée par Kafka -> notation)"
sleep 4
OPP=$(curl -s http://$H:$PORT_AVIS/api/avis/opportunites/$CID -H "Authorization: Bearer $CLT")
OPPID=$(echo "$OPP" | python3 -c "import sys,json
try:
  d=json.load(sys.stdin); print(d[0]['id'] if d else '')
except Exception: print('')")
check "opportunité créée"                   "200" "$([ -n "$OPPID" ] && echo 200 || echo 000)"
[ -n "$OPPID" ] && check "soumission d'avis" "200|201" "$(code -X POST http://$H:$PORT_AVIS/api/avis/transaction -H "Authorization: Bearer $CLT" -H 'Content-Type: application/json' -d "{\"opportuniteId\":$OPPID,\"noteCommunication\":5,\"noteProfessionnalisme\":4,\"noteDisponibilite\":5,\"noteConformiteAnnonce\":4,\"noteEtatGeneral\":5,\"noteRapportQualitePrix\":4}")"
check "note agent consultable"              "200" "$(code http://$H:$PORT_AVIS/api/avis/agent/$AID)"

section "Signalements"
OBJ=$(curl -s "http://$H:$PORT_SIGNALEMENTS/signalements/objets?type=AGENT" -H "Authorization: Bearer $CLT" | python3 -c "import sys,json;d=json.load(sys.stdin);print(d[0]['id'] if d else '')")
check "signalement (interaction existante)"  "201" "$(code -X POST http://$H:$PORT_SIGNALEMENTS/signalements -H "Authorization: Bearer $CLT" -H 'Content-Type: application/json' -d "{\"type\":\"AGENT\",\"objetSignalementId\":$OBJ,\"agentId\":$AID,\"description\":\"parcours complet\"}")"

section "Abonnement (Kafka)"
ABO=$(curl -s -X POST http://$H:$PORT_ABONNEMENT/api/abonnements -H "Authorization: Bearer $AGT" -H 'Content-Type: application/json' -d '{"type":"MENSUEL"}')
RIDABO=$(echo "$ABO" | jget requestId)
check "souscription -> EN_ATTENTE_PAIEMENT"  "EN_ATTENTE_PAIEMENT" "$(echo "$ABO" | jget statut)"
curl -s -X POST http://$H:$PORT_ABONNEMENT/api/interne/abonnements/simuler-paiement -H 'Content-Type: application/json' -d "{\"requestId\":\"$RIDABO\",\"paiementId\":1,\"statut\":\"SUCCESS\"}" >/dev/null
sleep 4
check "paiement Kafka -> ACTIF"              "ACTIF" "$(curl -s http://$H:$PORT_ABONNEMENT/api/abonnements/mon-statut -H "Authorization: Bearer $AGT" | jget statut)"

section "Messagerie"
check "envoi message client -> agent"        "200|201" "$(code -X POST http://$H:$PORT_MESSAGERIE/api/messages -H "Authorization: Bearer $CLT" -H 'Content-Type: application/json' -d "{\"receiverId\":$AID,\"bienId\":$BID,\"contenu\":\"Bonjour\"}")"

section "Notification"
check "notification générique"               "200|201" "$(code -X POST http://$H:$PORT_NOTIFICATION/api/notifications -H 'Content-Type: application/json' -d '{"destinataire":"admin","message":"parcours complet"}')"

section "Admin"
check "admin liste utilisateurs"             "200" "$(code http://$H:$PORT_ADMIN/admin/users -H "Authorization: Bearer $ADT")"
check "admin signalements"                   "200" "$(code http://$H:$PORT_ADMIN/admin/signalements -H "Authorization: Bearer $ADT")"

section "Infra"
check "Eureka"                               "200" "$(code http://$H:$PORT_EUREKA/actuator/health)"
check "Gateway"                              "200|404" "$(code http://$H:$PORT_GATEWAY/actuator/health)"

summary
