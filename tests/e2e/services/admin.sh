#!/usr/bin/env bash
# E2E service ADMIN (8083) : gestion utilisateurs, signalements, objets, sécurité par rôle.
set -uo pipefail
DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"; source "$DIR/../lib.sh"

section "ADMIN ($PORT_ADMIN)"
AD="adm_ad${TS}@t.com"; CL="adm_cl${TS}@t.com"
ADT=$(register_and_login "$AD" ADMIN); CLT=$(register_and_login "$CL" CLIENT)

check "lister utilisateurs (admin)"       "200" "$(code http://$H:$PORT_ADMIN/admin/users -H "Authorization: Bearer $ADT")"
check "lister signalements (admin)"       "200" "$(code http://$H:$PORT_ADMIN/admin/signalements -H "Authorization: Bearer $ADT")"
check "objets de signalement (admin)"     "200" "$(code http://$H:$PORT_ADMIN/admin/signalements/objets -H "Authorization: Bearer $ADT")"
check "role-requests (admin)"             "200" "$(code http://$H:$PORT_ADMIN/admin/role-requests -H "Authorization: Bearer $ADT")"
check "accès admin par un client -> 403"  "401|403" "$(code http://$H:$PORT_ADMIN/admin/users -H "Authorization: Bearer $CLT")"
check "accès admin sans token -> 401/403" "401|403" "$(code http://$H:$PORT_ADMIN/admin/users)"

summary
