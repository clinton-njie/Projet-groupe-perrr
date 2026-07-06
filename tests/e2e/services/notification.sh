#!/usr/bin/env bash
# E2E service NOTIFICATION (8091) : endpoint générique + endpoint paiement.
set -uo pipefail
DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"; source "$DIR/../lib.sh"

section "NOTIFICATION ($PORT_NOTIFICATION)"

check "notif générique {destinataire,message}" "200|201" \
  "$(code -X POST http://$H:$PORT_NOTIFICATION/api/notifications -H 'Content-Type: application/json' -d '{"destinataire":"admin@test.com","message":"Nouveau signalement en attente"}')"
check "notif générique invalide (400)"          "400" \
  "$(code -X POST http://$H:$PORT_NOTIFICATION/api/notifications -H 'Content-Type: application/json' -d '{"destinataire":"","message":""}')"
check "notif paiement SUCCESS"                  "200|201" \
  "$(code -X POST http://$H:$PORT_NOTIFICATION/api/notifications/paiement -H 'Content-Type: application/json' -d '{"nomUtilisateur":"Cli","nomBien":"Appt","natureType":"VISITE","statut":"SUCCESS","montant":5000}')"
check "notif paiement REFUNDED"                 "200|201" \
  "$(code -X POST http://$H:$PORT_NOTIFICATION/api/notifications/paiement -H 'Content-Type: application/json' -d '{"nomUtilisateur":"Cli","nomBien":"Appt","natureType":"RESERVATION","statut":"REFUNDED","montant":5000}')"

summary
