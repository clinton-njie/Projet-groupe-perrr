#!/usr/bin/env bash
# Lance TOUTE la suite E2E : chaque service indépendamment, puis les flux multi-services.
# Usage :
#   ./run_all.sh              # tout
#   ./run_all.sh services     # uniquement les tests par service
#   ./run_all.sh flux         # uniquement les flux multi-services
#
# Variables d'environnement : H (hôte, défaut localhost) et PORT_* pour surcharger les ports.
set -uo pipefail
DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
MODE="${1:-all}"

SERVICES=(auth user bien visite reservation paiement notification avis signalement abonnement messagerie admin)
FLUX=(flux_visite_notation flux_reservation_notation flux_signalement_bannissement flux_abonnement_kafka flux_complet)

declare -a OK_LIST KO_LIST
run() { # run <chemin>
  local f="$1" nom; nom="$(basename "$f" .sh)"
  echo; echo "============================================================"
  echo ">>> $nom"
  echo "============================================================"
  if bash "$f"; then OK_LIST+=("$nom"); else KO_LIST+=("$nom"); fi
}

[ "$MODE" = "all" ] || [ "$MODE" = "services" ] && for s in "${SERVICES[@]}"; do run "$DIR/services/$s.sh"; done
[ "$MODE" = "all" ] || [ "$MODE" = "flux" ] && for f in "${FLUX[@]}"; do run "$DIR/flux/$f.sh"; done

echo; echo "############################################################"
echo "# BILAN GLOBAL"
echo "############################################################"
printf '\033[1;32m  RÉUSSIS (%d)\033[0m : %s\n' "${#OK_LIST[@]}" "${OK_LIST[*]:-aucun}"
if [ "${#KO_LIST[@]}" -gt 0 ]; then
  printf '\033[1;31m  ÉCHOUÉS (%d)\033[0m : %s\n' "${#KO_LIST[@]}" "${KO_LIST[*]}"
  exit 1
fi
echo "  Tous les tests sont au vert."
