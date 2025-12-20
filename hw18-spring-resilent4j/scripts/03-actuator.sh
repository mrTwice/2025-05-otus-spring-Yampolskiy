#!/usr/bin/env bash
set -euo pipefail
. "$(dirname "$0")/lib.sh"

hr
title "9) Actuator: CB state + NOT_PERMITTED evidence"
state="$(get_cb_field state)"
echo "CircuitBreaker moderation state: ${state:-UNKNOWN}"
echo "-- /actuator/circuitbreakers"
curl -sS "$BASE_MAIN/actuator/circuitbreakers" | pretty_json | print_body

np_ev="$(count_not_permitted_events || echo "")"
echo "NOT_PERMITTED events (count): ${np_ev:-UNKNOWN}"
echo "-- /actuator/circuitbreakerevents"
curl -sS "$BASE_MAIN/actuator/circuitbreakerevents" | pretty_json | print_body

hr
title "10) Prove OPEN is fast: ${STEP10_N} timings for POST ERROR_503 (ms)"
sum=0; min=999999; max=0
for i in $(seq 1 "$STEP10_N"); do
  ms="$(post_ms)"
  printf "%s " "$ms"
  sum=$((sum+ms))
  [ "$ms" -lt "$min" ] && min="$ms"
  [ "$ms" -gt "$max" ] && max="$ms"
done
printf "\n"
avg=$((sum/STEP10_N))
echo "min=${min}ms avg=${avg}ms max=${max}ms"

hr
title "ACTUATOR DONE"
