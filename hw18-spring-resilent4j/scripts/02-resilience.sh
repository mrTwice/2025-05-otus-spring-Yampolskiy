#!/usr/bin/env bash
set -euo pipefail
. "$(dirname "$0")/lib.sh"

hr
title "4) ERROR_503 from moderation -> expect 503"
resp="$(req_post_comment "ERROR_503" "hello")"; code="$(printf "%s" "$resp" | status_of)"
echo "HTTP $code"; printf "%s" "$resp" | body_of | print_body; expect_code "$code" "503"

hr
title "5) ERROR_500 from moderation -> expect 503"
resp="$(req_post_comment "ERROR_500" "hello")"; code="$(printf "%s" "$resp" | status_of)"
echo "HTTP $code"; printf "%s" "$resp" | body_of | print_body; expect_code "$code" "503"

hr
title "6) SLOW from moderation -> expect 503 (timeout)"
resp="$(req_post_comment "SLOW" "hello")"; code="$(printf "%s" "$resp" | status_of)"
echo "HTTP $code"; printf "%s" "$resp" | body_of | print_body; expect_code "$code" "503"

hr
title "7) Bulkhead stress: ${STEP7_PAR} parallel SLOW (code distribution)"
tmp="$(mktemp)"
for i in $(seq 1 "$STEP7_PAR"); do
  (
    c="$(curl -sS -o /dev/null -w "%{http_code}" -X POST "$COMMENTS_URL" \
      -H "Content-Type: application/json" -H "X-Mode: SLOW" \
      -d "{\"text\":\"hello\"}" || echo "000")"
    echo "$c" >> "$tmp"
  ) &
done
wait
echo "HTTP codes (count):"
sort "$tmp" | uniq -c | sort -nr
rm -f "$tmp"

hr
title "8) Warm up / try open CB: ${STEP8_SEQ} sequential ERROR_503 (codes)"
for i in $(seq 1 "$STEP8_SEQ"); do
  c="$(curl -sS -o /dev/null -w "%{http_code}" -X POST "$COMMENTS_URL" \
    -H "Content-Type: application/json" -H "X-Mode: ERROR_503" \
    -d "{\"text\":\"hello\"}" || echo "000")"
  printf "%s " "$c"
done
printf "\n"

hr
title "RESILIENCE DONE"
