#!/usr/bin/env bash
# E2E service MESSAGERIE (8089) : envoi de message, boîte de réception, conversation.
set -uo pipefail
DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"; source "$DIR/../lib.sh"

section "MESSAGERIE ($PORT_MESSAGERIE)"
AG="msg_ag${TS}@t.com"; CL="msg_cl${TS}@t.com"
AGT=$(register_and_login "$AG" AGENT); CLT=$(register_and_login "$CL" CLIENT)
AID=$(id_of "$AG")
BID=$(creer_bien "$AGT")

check "envoyer message (client -> agent)"  "200|201" "$(code -X POST http://$H:$PORT_MESSAGERIE/api/messages -H "Authorization: Bearer $CLT" -H 'Content-Type: application/json' -d "{\"receiverId\":$AID,\"bienId\":$BID,\"contenu\":\"Bonjour, ce bien est-il disponible ?\"}")"
check "envoyer sans token -> 401/403"       "401|403" "$(code -X POST http://$H:$PORT_MESSAGERIE/api/messages -H 'Content-Type: application/json' -d "{\"receiverId\":$AID,\"bienId\":$BID,\"contenu\":\"x\"}")"
check "inbox agent"                         "200" "$(code http://$H:$PORT_MESSAGERIE/api/messages/inbox -H "Authorization: Bearer $AGT")"
check "inbox client"                        "200" "$(code http://$H:$PORT_MESSAGERIE/api/messages/inbox -H "Authorization: Bearer $CLT")"

summary
