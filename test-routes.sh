#!/usr/bin/env bash
# Teste les routes métier de chaque microservice (au-delà du simple health-check
# déjà couvert par test-services.sh). Regroupe les appels par mode de communication
# SYNC (REST direct) / ASYNC (event-driven, selon l'architecture cible de l'équipe).
#
# Prérequis : curl, jq
# Usage :
#   ./test-routes.sh           # lance tout le flux sync puis async
#   ./test-routes.sh sync      # uniquement le bloc synchrone
#   ./test-routes.sh async     # uniquement le bloc asynchrone
#
# Ports = exactement le tableau d'architecture donné par l'équipe :
#   Auth 8081 | User 8082 | Biens 8084 | Réservation 8086 | Visite 8085
#   Paiement 8088 | Abonnement 8087 | Signalement 8090 | Avis 8089
#   Notification 8091 | Media 8093 | Admin 8083 | Eureka 8761 | Gateway 8222
#
# Mise à jour 2026-07-04 : bien-service/reservation-service/service-visite/
# service-paiement ont été réécrits selon la spec mobile money + escrow, et
# service-notification a été créé. Voir /home/christale/.claude/plans/
# peppy-foraging-bunny.md pour le détail complet de la refonte.
#
# Notes de fiabilité :
#  - bien-service : spring.application.name = bien-service, port 8084 (yml et
#    docker-compose alignés désormais).
#  - reservation-service : spring.application.name = reservation-service,
#    port 8086 (yml ET docker-compose racine corrigés dans cette passe — le
#    docker-compose racine mappait 8088:8088 par erreur, corrigé en 8086:8086).
#  - service-visite : spring.application.name = visite-service (dossier
#    inchangé), routes déplacées de /visits vers /api/visites (sauf
#    available-slots, resté à /visits/available-slots). Flux Kafka
#    transaction.terminee supprimé (agent-approval retiré du flux métier).
#  - service-paiement : spring.application.name = payment-service, contrat
#    unifié {referenceId, referenceType, nomUtilisateur, nomBien, montant,
#    operateur, telephone}, mode escrow pour VISITE (capturer/rembourser).
#  - service-notification (nouveau, port 8091) : POST /api/notifications/paiement.
#  - ms-avis : port 8089 en profil "local" (celui du tableau), mais 8085 en
#    profil "docker" -> collision avec service-visite (aussi 8085) si déployé
#    en conteneur avec le profil docker actif. Non traité dans cette passe.
#  - bien-service, service-visite, service-paiement, service-signalements,
#    ms-avis, service-admin, service-notification ne sont PAS dans le
#    docker-compose.yml racine : à lancer individuellement (mvn spring-boot:run
#    + SERVER_PORT si besoin, ou leur propre docker-compose).
#  - Service Media (8093) : aucun code trouvé dans le repo à ce jour -> section
#    marquée TODO, non testable.
#  - Service Messagerie : absent du tableau d'architecture fourni par l'équipe,
#    présent dans le repo sur le port 8086 -> collision avec Service
#    Réservation si les deux tournent en local en même temps. Inclus en bonus.
#  - Service Abonnement/Signalement/Avis restent classés ASYNC dans le tableau
#    cible mais exposent aujourd'hui une API REST classique dans le code
#    (aucun broker branché) -> testés en REST ici, inchangé par cette passe.
#  - Point de vigilance non automatisable ici : compatibilité du secret JWT
#    partagé entre le mécanisme jjwt de bien-service/service-visite et
#    oauth2ResourceServer().jwt() de reservation-service. Le flux SYNC
#    ci-dessous (register -> login -> POST /api/reservations) sert justement
#    de test de bout en bout pour ça : un 401 sur la création de réservation
#    avec un token valide indique un problème de secret partagé.

set -uo pipefail

HOST="localhost"
AUTH_PORT=8081
USER_PORT=8082
BIEN_PORT=8084
ADMIN_PORT=8083
RESERVATION_PORT=8086
VISITE_PORT=8085
MESSAGERIE_PORT=8086    # hors tableau officiel ; collision de port avec Réservation en local
ABONNEMENT_PORT=8087
PAIEMENT_PORT=8088
SIGNALEMENT_PORT=8090
AVIS_PORT=8089
NOTIFICATION_PORT=8091
MEDIA_PORT=8093         # non implémenté dans le repo
GATEWAY_PORT=8222
EUREKA_PORT=8761

GREEN="\033[0;32m"; RED="\033[0;31m"; YELLOW="\033[0;33m"; BLUE="\033[0;34m"; NC="\033[0m"
PASS=0; FAIL=0; SKIP=0

command -v jq >/dev/null || { echo "jq est requis (sudo apt install jq)"; exit 1; }

section() { echo -e "\n${BLUE}== $1 ==${NC}"; }

# call METHOD URL [BODY] [TOKEN] -> stocke la réponse dans $BODY_OUT, le code dans $CODE_OUT
call() {
  local method="$1" url="$2" body="${3:-}" token="${4:-}"
  local args=(-s -o /tmp/routes_resp.json -w "%{http_code}" -X "$method" "$url" -H "Content-Type: application/json" --max-time 5)
  [[ -n "$token" ]] && args+=(-H "Authorization: Bearer $token")
  [[ -n "$body" ]] && args+=(-d "$body")
  CODE_OUT=$(curl "${args[@]}" 2>/dev/null)
  BODY_OUT=$(cat /tmp/routes_resp.json 2>/dev/null)
}

check() {
  local label="$1" expected="$2"
  if [[ "$CODE_OUT" == "$expected" ]]; then
    echo -e "  ${GREEN}OK${NC}   [$CODE_OUT] $label"
    PASS=$((PASS + 1))
  elif [[ "$CODE_OUT" == "000" ]]; then
    echo -e "  ${YELLOW}DOWN${NC} [conn refusée] $label — service non démarré ?"
    FAIL=$((FAIL + 1))
  else
    echo -e "  ${RED}KO${NC}   [$CODE_OUT attendu $expected] $label — $(echo "$BODY_OUT" | head -c 200)"
    FAIL=$((FAIL + 1))
  fi
}

skip() { echo -e "  ${YELLOW}SKIP${NC} $1"; SKIP=$((SKIP + 1)); }

STAMP=$(date +%s)
CLIENT_EMAIL="client.${STAMP}@test.local"
AGENT_EMAIL="agent.${STAMP}@test.local"
PASSWORD="Password123"

run_sync() {
section "SYNC — Auth Service (:$AUTH_PORT)"
call POST "http://$HOST:$AUTH_PORT/api/auth/register" \
  "{\"firstname\":\"Cli\",\"lastname\":\"Ent\",\"email\":\"$CLIENT_EMAIL\",\"password\":\"$PASSWORD\"}"
check "POST /api/auth/register (client)" 201
call POST "http://$HOST:$AUTH_PORT/api/auth/register" \
  "{\"firstname\":\"Age\",\"lastname\":\"Nt\",\"email\":\"$AGENT_EMAIL\",\"password\":\"$PASSWORD\"}"
check "POST /api/auth/register (agent — rôle CLIENT par défaut, à monter en ROLE_AGENT en base si besoin)" 201

call POST "http://$HOST:$AUTH_PORT/api/auth/login" \
  "{\"email\":\"$CLIENT_EMAIL\",\"password\":\"$PASSWORD\"}"
check "POST /api/auth/login (client)" 200
CLIENT_TOKEN=$(echo "$BODY_OUT" | jq -r '.accessToken // empty')

call POST "http://$HOST:$AUTH_PORT/api/auth/login" \
  "{\"email\":\"$AGENT_EMAIL\",\"password\":\"$PASSWORD\"}"
check "POST /api/auth/login (agent)" 200
AGENT_TOKEN=$(echo "$BODY_OUT" | jq -r '.accessToken // empty')

if [[ -n "${CLIENT_TOKEN:-}" ]]; then
  call GET "http://$HOST:$AUTH_PORT/api/auth/me" "" "$CLIENT_TOKEN"
  check "GET /api/auth/me" 200
else
  skip "GET /api/auth/me (pas de token — login précédent a échoué)"
fi

section "SYNC — User Service (:$USER_PORT)"
if [[ -n "${CLIENT_TOKEN:-}" ]]; then
  call GET "http://$HOST:$USER_PORT/api/users/by-email/$CLIENT_EMAIL" "" "$CLIENT_TOKEN"
  check "GET /api/users/by-email/{email}" 200
else
  skip "GET /api/users/by-email — dépend du login"
fi

section "SYNC — Gestion Biens (:$BIEN_PORT)"
if [[ -n "${AGENT_TOKEN:-}" ]]; then
  call POST "http://$HOST:$BIEN_PORT/api/biens" \
    '{"categorie":"CITE","nom":"Cité Test","ville":"Douala","quartier":"Bonamoussadi","photoPrincipale":"http://x/1.jpg","prixMensuel":80000,"nombrePiecesDisponibles":12,"description":"bien de test"}' \
    "$AGENT_TOKEN"
  check "POST /api/biens (agent, catégorie CITE)" 201
  BIEN_ID=$(echo "$BODY_OUT" | jq -r '.id // empty')

  call POST "http://$HOST:$BIEN_PORT/api/biens" \
    '{"categorie":"CITE","nom":"Sans pièces","ville":"Douala","quartier":"Akwa","photoPrincipale":"http://x/2.jpg","prixMensuel":50000,"description":"doit échouer"}' \
    "$AGENT_TOKEN"
  check "POST /api/biens (CITE sans nombrePiecesDisponibles -> validation)" 400
else
  skip "POST /api/biens — dépend du token agent"
fi
call GET "http://$HOST:$BIEN_PORT/api/biens"
check "GET /api/biens" 200
call GET "http://$HOST:$BIEN_PORT/api/biens/disponibles"
check "GET /api/biens/disponibles" 200
call GET "http://$HOST:$BIEN_PORT/api/biens/categorie/CITE/prix-visite"
check "GET /api/biens/categorie/CITE/prix-visite" 200

section "SYNC — Service Réservation (:$RESERVATION_PORT)"
if [[ -n "${CLIENT_TOKEN:-}" && -n "${BIEN_ID:-}" ]]; then
  call POST "http://$HOST:$RESERVATION_PORT/api/reservations" \
    "{\"propertyId\":$BIEN_ID,\"startDate\":\"2026-08-01\",\"endDate\":\"2026-09-15\",\"operateur\":\"MTN\",\"telephone\":\"677000001\"}" \
    "$CLIENT_TOKEN"
  check "POST /api/reservations (montant auto-calculé, paiement déclenché)" 200
  RESERVATION_ID=$(echo "$BODY_OUT" | jq -r '.id // empty')
  # Si ce 401 apparaît avec un token valide : secret JWT non partagé entre auth-service et reservation-service (voir note en tête de script)
else
  skip "POST /api/reservations — dépend du bien créé"
fi
if [[ -n "${RESERVATION_ID:-}" ]]; then
  call GET "http://$HOST:$RESERVATION_PORT/api/reservations/$RESERVATION_ID"
  check "GET /api/reservations/{id} (status=CONFIRME attendu après callback paiement)" 200
fi
if [[ -n "${CLIENT_TOKEN:-}" ]]; then
  call GET "http://$HOST:$RESERVATION_PORT/api/reservations/user/1"
  check "GET /api/reservations/user/{userId}" 200
fi

section "SYNC — Service Visite (:$VISITE_PORT)"
call GET "http://$HOST:$VISITE_PORT/visits/available-slots?bienId=1&date=2026-08-10"
check "GET /visits/available-slots (inchangé, hors périmètre de la refonte)" 200
if [[ -n "${BIEN_ID:-}" ]]; then
  call POST "http://$HOST:$VISITE_PORT/api/visites" \
    "{\"bienId\":$BIEN_ID,\"clientEmail\":\"$CLIENT_EMAIL\",\"dateHeureDebut\":\"2026-08-10T10:00:00\",\"dateHeureFin\":\"2026-08-10T11:00:00\",\"operateur\":\"ORANGE\",\"telephone\":\"655000000\"}"
  check "POST /api/visites (prix de visite récupéré par catégorie, escrow initié)" 201
  VISITE_ID=$(echo "$BODY_OUT" | jq -r '.id // empty')
else
  skip "POST /api/visites — dépend du bien créé"
fi
if [[ -n "${VISITE_ID:-}" ]]; then
  call GET "http://$HOST:$VISITE_PORT/api/visites/$VISITE_ID"
  check "GET /api/visites/{id} (statut=PAYEE attendu après callback escrow)" 200
  call PUT "http://$HOST:$VISITE_PORT/api/visites/$VISITE_ID/annuler"
  check "PUT /api/visites/{id}/annuler (>=24h avant -> remboursement, ANNULEE_CLIENT)" 200
fi

section "SYNC — Service Paiement (:$PAIEMENT_PORT, contrat unifié escrow)"
call POST "http://$HOST:$PAIEMENT_PORT/payments" \
  '{"referenceId":999,"referenceType":"VISITE","nomUtilisateur":"Jean Test","nomBien":"Villa Test","montant":10000,"operateur":"ORANGE","telephone":"677000000"}'
check "POST /payments (referenceType=VISITE -> statut AUTORISE, escrow)" 201
PAYMENT_ID=$(echo "$BODY_OUT" | jq -r '.paymentId // empty')
if [[ -n "${PAYMENT_ID:-}" ]]; then
  call PUT "http://$HOST:$PAIEMENT_PORT/api/paiements/$PAYMENT_ID/capturer"
  check "PUT /api/paiements/{id}/capturer (transfert agent)" 200
fi

section "SYNC — Service Admin (:$ADMIN_PORT — nécessite un token ROLE_ADMIN/ROLE_SUPER_ADMIN)"
skip "GET /admin/users — pas de compte admin auto-provisionné par ce script (créer via /api/auth/admin/register ou seed DB)"

section "BONUS — Service Messagerie (:$MESSAGERIE_PORT, hors tableau d'architecture fourni)"
if [[ -n "${CLIENT_TOKEN:-}" ]]; then
  call GET "http://$HOST:$MESSAGERIE_PORT/api/messages/inbox" "" "$CLIENT_TOKEN"
  check "GET /api/messages/inbox" 200
else
  skip "GET /api/messages/inbox — dépend du token client"
fi
}

run_async() {
section "ASYNC (cible) — Service Abonnement (:$ABONNEMENT_PORT) — actuellement REST classique dans le code"
if [[ -n "${CLIENT_TOKEN:-}" ]]; then
  call POST "http://$HOST:$ABONNEMENT_PORT/api/abonnements/souscrire" '{"type":"MENSUEL"}' "$CLIENT_TOKEN"
  check "POST /api/abonnements/souscrire" 201
  call GET "http://$HOST:$ABONNEMENT_PORT/api/abonnements/me" "" "$CLIENT_TOKEN"
  check "GET /api/abonnements/me" 200
else
  skip "abonnements — dépend du token client"
fi

section "ASYNC (cible) — Service Signalement (:$SIGNALEMENT_PORT) — actuellement REST classique dans le code"
call POST "http://$HOST:$SIGNALEMENT_PORT/signalements" \
  '{"titre":"Fuite d'\''eau","type":"TECHNIQUE","description":"test","dateSignalement":"2026-07-04","utilisateurId":1}'
check "POST /signalements" 201
call GET "http://$HOST:$SIGNALEMENT_PORT/signalements"
check "GET /signalements" 200

section "ASYNC (cible) — Service Avis (:$AVIS_PORT) — actuellement REST classique dans le code"
if [[ -n "${CLIENT_TOKEN:-}" ]]; then
  call POST "http://$HOST:$AVIS_PORT/api/avis/plateforme" \
    '{"notePlateforme":5,"commentaire":"test"}' "$CLIENT_TOKEN"
  check "POST /api/avis/plateforme" 201
else
  skip "POST /api/avis/plateforme — dépend du token client"
fi
call GET "http://$HOST:$AVIS_PORT/api/avis/plateforme"
check "GET /api/avis/plateforme" 200

section "ASYNC (cible) — Service Notification (:$NOTIFICATION_PORT)"
call POST "http://$HOST:$NOTIFICATION_PORT/api/notifications/paiement" \
  '{"nomUtilisateur":"Jean","nomBien":"Les Bougainvilliers","natureType":"RESERVATION","statut":"SUCCESS","montant":150000}'
check "POST /api/notifications/paiement (message personnalisé formaté et loggé)" 201

section "ASYNC (cible) — Service Media (:$MEDIA_PORT)"
skip "aucun code trouvé dans le repo pour ce service — TODO une fois implémenté"
}

MODE="${1:-all}"
case "$MODE" in
  sync) run_sync ;;
  async) run_async ;;
  all|*) run_sync; run_async ;;
esac

echo
echo "--------------------------------------------------------------"
echo -e "Total: $((PASS + FAIL + SKIP)) | ${GREEN}OK: $PASS${NC} | ${RED}KO: $FAIL${NC} | ${YELLOW}SKIP: $SKIP${NC}"
rm -f /tmp/routes_resp.json
