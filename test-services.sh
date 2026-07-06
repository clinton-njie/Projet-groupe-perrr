#!/usr/bin/env bash
# Vérifie l'état (health-check) de tous les microservices du projet.
# Ne modifie aucun service : lecture seule, requêtes GET uniquement.
#
# Usage:
#   ./test-services.sh              # teste tous les services
#   ./test-services.sh visite       # filtre par nom (grep insensible à la casse)
#
# Format de chaque entrée : "Nom|port|mode|endpoint|note"
#   mode  : SYNC (REST) ou ASYNC (message broker) — classification fournie par l'équipe
#   note  : port vérifié dans le code, ou port cible/attendu si le service n'est pas
#           encore déployé (voir README / discussion d'équipe pour les écarts connus)

SERVICES=(
  "Eureka (Service Discovery)|8761|INFRA|/actuator/health|vérifié"
  "API Gateway|8222|SYNC|/actuator/health|vérifié"
  "Auth Service|8081|SYNC|/actuator/health|vérifié"
  "User Service|8082|SYNC|/actuator/health|vérifié"
  "Service Visite|8085|SYNC|/actuator/health|vérifié"
  "Service Messagerie|8086|NON-CLASSÉ|/actuator/health|vérifié (absent du tableau fourni)"
  "Gestion Biens|8084|SYNC|/actuator/health|attendu — code local configuré sur 8082 (conflit avec User Service)"
  "Service Réservation|8086|SYNC|/actuator/health|attendu — code local configuré sur 8088, pas de Dockerfile"
  "Service Paiement|8088|SYNC|/actuator/health|attendu — pas encore fusionné dans develop"
  "Service Abonnement|8087|ASYNC|/actuator/health|attendu — pas de Dockerfile"
  "Service Signalement|8090|ASYNC|/actuator/health|attendu — pas encore fusionné dans develop"
  "Service Avis|8089|ASYNC|/actuator/health|attendu — pas encore fusionné dans develop"
  "Service Notification|8091|ASYNC|/actuator/health|attendu — pas encore fusionné dans develop"
  "Service Media|8093|NON-CLASSÉ|/actuator/health|attendu — pas encore fusionné dans develop"
  "Admin Service|8083|SYNC|/actuator/health|attendu — pas encore fusionné dans develop"
)

FILTER="${1:-}"
HOST="localhost"
TIMEOUT=3

GREEN="\033[0;32m"; RED="\033[0;31m"; YELLOW="\033[0;33m"; NC="\033[0m"

up=0; down=0; total=0

printf "%-24s %-6s %-11s %-8s %s\n" "SERVICE" "PORT" "MODE" "STATUT" "NOTE"
printf '%.0s-' {1..100}; echo

for entry in "${SERVICES[@]}"; do
  IFS='|' read -r name port mode endpoint note <<< "$entry"

  if [[ -n "$FILTER" ]] && ! echo "$name" | grep -qi "$FILTER"; then
    continue
  fi

  total=$((total + 1))
  code=$(curl -s -o /dev/null -w "%{http_code}" --max-time "$TIMEOUT" "http://${HOST}:${port}${endpoint}" 2>/dev/null)

  if [[ "$code" == "200" ]]; then
    status="${GREEN}UP  ${NC}"
    up=$((up + 1))
  else
    status="${RED}DOWN${NC}"
    down=$((down + 1))
  fi

  printf "%-24s %-6s %-11s %b   %s\n" "$name" "$port" "$mode" "$status" "$note"
done

echo
printf '%.0s-' {1..100}; echo
echo -e "Total: $total | ${GREEN}UP: $up${NC} | ${RED}DOWN: $down${NC}"
