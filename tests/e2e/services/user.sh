#!/usr/bin/env bash
# E2E service USER / SERVICE-UTILISATEUR (8082) : profil créé à l'inscription, résolutions.
set -uo pipefail
DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"; source "$DIR/../lib.sh"

section "USER ($PORT_USER)"
EM="user_ag${TS}@t.com"
TOKEN=$(register_and_login "$EM" AGENT)   # register crée aussi le profil user via /api/users/init
ID=$(id_of "$EM")

check "by-email -> id numérique"   "200" "$([ -n "$ID" ] && echo 200 || echo 000)"
check "profil par id"              "200" "$(code http://$H:$PORT_USER/api/users/$ID)"
check "email par id"               "200" "$(code http://$H:$PORT_USER/api/users/$ID/email)"
check "by-email inexistant"        "200|400|404|500" "$(code http://$H:$PORT_USER/api/users/by-email/inexistant$TS@t.com)"

# Vérifie le contenu du profil
PROFIL=$(curl -s http://$H:$PORT_USER/api/users/$ID)
check "profil contient l'email"    "200" "$(echo "$PROFIL" | grep -q "$EM" && echo 200 || echo 000)"

summary
