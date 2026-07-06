#!/usr/bin/env bash
# Bibliothèque commune aux tests E2E. Sourcée par chaque script.
# Fournit : configuration des ports, helpers curl/JSON, assertions (check) et un résumé.
#
# Prérequis : bash, curl, python3.  (jq n'est PAS requis.)

# --- Configuration des hôtes/ports (ports exposés par docker-compose) ---
H="${H:-localhost}"
PORT_AUTH="${PORT_AUTH:-8081}"
PORT_USER="${PORT_USER:-8082}"
PORT_ADMIN="${PORT_ADMIN:-8083}"
PORT_BIEN="${PORT_BIEN:-8084}"
PORT_VISITE="${PORT_VISITE:-8085}"
PORT_RESERVATION="${PORT_RESERVATION:-8086}"
PORT_ABONNEMENT="${PORT_ABONNEMENT:-8087}"
PORT_PAIEMENT="${PORT_PAIEMENT:-8088}"
PORT_MESSAGERIE="${PORT_MESSAGERIE:-8089}"
PORT_AVIS="${PORT_AVIS:-8090}"
PORT_NOTIFICATION="${PORT_NOTIFICATION:-8091}"
PORT_SIGNALEMENTS="${PORT_SIGNALEMENTS:-8095}"
PORT_EUREKA="${PORT_EUREKA:-8761}"
PORT_GATEWAY="${PORT_GATEWAY:-8222}"

PW="Password123!"
TS="$(date +%s)$RANDOM"

# --- Couleurs ---
_g() { printf '\033[1;32m%s\033[0m\n' "$*"; }
_r() { printf '\033[1;31m%s\033[0m\n' "$*"; }
_c() { printf '\033[1;36m%s\033[0m\n' "$*"; }

# --- Compteurs ---
PASS=0; FAIL=0; declare -a FAILED

section() { echo; _c "### $* ###"; }

# extrait un champ JSON de premier niveau depuis stdin : ... | jget accessToken
jget() { python3 -c "import sys,json
try: print(json.load(sys.stdin).get('$1',''))
except Exception: print('')"; }

# code HTTP d'une requête : code -X POST http://... -H ... -d ...
code() { curl -s -o /dev/null -w "%{http_code}" "$@"; }

# check <label> <codes attendus regex ex '200|201'> <valeur obtenue>
check() {
  if [[ "$3" =~ ^($2)$ ]]; then
    printf '  \033[1;32mOK\033[0m  %s (%s)\n' "$1" "$3"; PASS=$((PASS+1))
  else
    printf '  \033[1;31mKO\033[0m  %s (attendu %s, obtenu %s)\n' "$1" "$2" "$3"; FAIL=$((FAIL+1)); FAILED+=("$1")
  fi
}

# Résumé + code de sortie (0 si tout OK, 1 sinon)
summary() {
  echo; echo "------------------------------------------"
  if [ "$FAIL" -eq 0 ]; then _g "RESULTAT : $PASS OK / 0 KO"; else
    _r "RESULTAT : $PASS OK / $FAIL KO"
    for f in "${FAILED[@]}"; do echo "   - $f"; done
  fi
  echo "------------------------------------------"
  [ "$FAIL" -eq 0 ]
}

# --- Helpers métier partagés ---

# Crée un compte + login. Usage : TOKEN=$(register_and_login email ROLE)  ROLE=AGENT|ADMIN|CLIENT
register_and_login() {
  local email="$1" role="$2"
  if [ "$role" = "CLIENT" ]; then
    curl -s -X POST "http://$H:$PORT_AUTH/api/auth/register" -H 'Content-Type: application/json' \
      -d "{\"email\":\"$email\",\"password\":\"$PW\",\"firstname\":\"Pre\",\"lastname\":\"Nom\"}" >/dev/null
  else
    curl -s -X POST "http://$H:$PORT_AUTH/api/auth/admin/register" -H 'Content-Type: application/json' \
      -d "{\"email\":\"$email\",\"password\":\"$PW\",\"firstname\":\"Pre\",\"lastname\":\"Nom\",\"role\":\"ROLE_$role\"}" >/dev/null
  fi
  curl -s -X POST "http://$H:$PORT_AUTH/api/auth/login" -H 'Content-Type: application/json' \
    -d "{\"email\":\"$email\",\"password\":\"$PW\"}" | jget accessToken
}

# id numérique d'un email (via user-service). Usage : ID=$(id_of email)
id_of() { curl -s "http://$H:$PORT_USER/api/users/by-email/$1"; }

# Crée un bien détenu par l'agent (token agent). Echo l'id du bien.
creer_bien() {
  local token="$1" cat="${2:-APPARTEMENT}"
  curl -s -X POST "http://$H:$PORT_BIEN/api/biens" -H "Authorization: Bearer $token" -H 'Content-Type: application/json' \
    -d "{\"categorie\":\"$cat\",\"nom\":\"Bien $TS\",\"ville\":\"Douala\",\"quartier\":\"Akwa\",\"photoPrincipale\":\"p\",\"photosSecondaires\":[],\"prixMensuel\":100000,\"nombrePiecesDisponibles\":2,\"description\":\"e2e\"}" | jget id
}
