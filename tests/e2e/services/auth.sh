#!/usr/bin/env bash
# E2E service AUTH (8081) : inscription, connexion, /me, refresh, rôles.
set -uo pipefail
DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"; source "$DIR/../lib.sh"

section "AUTH ($PORT_AUTH)"
AG="auth_ag${TS}@t.com"; CL="auth_cl${TS}@t.com"

check "register agent"            "200|201" "$(code -X POST http://$H:$PORT_AUTH/api/auth/admin/register -H 'Content-Type: application/json' -d "{\"email\":\"$AG\",\"password\":\"$PW\",\"firstname\":\"A\",\"lastname\":\"G\",\"role\":\"ROLE_AGENT\"}")"
check "register client"           "200|201" "$(code -X POST http://$H:$PORT_AUTH/api/auth/register -H 'Content-Type: application/json' -d "{\"email\":\"$CL\",\"password\":\"$PW\",\"firstname\":\"C\",\"lastname\":\"L\"}")"
check "register email dupliqué"   "400|409" "$(code -X POST http://$H:$PORT_AUTH/api/auth/register -H 'Content-Type: application/json' -d "{\"email\":\"$CL\",\"password\":\"$PW\",\"firstname\":\"C\",\"lastname\":\"L\"}")"

RESP=$(curl -s -X POST http://$H:$PORT_AUTH/api/auth/login -H 'Content-Type: application/json' -d "{\"email\":\"$AG\",\"password\":\"$PW\"}")
TOKEN=$(echo "$RESP" | jget accessToken)
REFRESH=$(echo "$RESP" | jget refreshToken)
check "login agent -> accessToken" "200" "$([ -n "$TOKEN" ] && echo 200 || echo 000)"
check "login mauvais mot de passe"  "401|403" "$(code -X POST http://$H:$PORT_AUTH/api/auth/login -H 'Content-Type: application/json' -d "{\"email\":\"$AG\",\"password\":\"WRONG\"}")"
check "GET /me (authentifié)"        "200" "$(code http://$H:$PORT_AUTH/api/auth/me -H "Authorization: Bearer $TOKEN")"
check "GET /me sans token"           "401|403" "$(code http://$H:$PORT_AUTH/api/auth/me)"
[ -n "$REFRESH" ] && check "refresh-token" "200" "$(code -X POST http://$H:$PORT_AUTH/api/auth/refresh-token -H 'Content-Type: application/json' -d "{\"refreshToken\":\"$REFRESH\"}")"

summary
