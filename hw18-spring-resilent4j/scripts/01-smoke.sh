#!/usr/bin/env bash
set -euo pipefail
. "$(dirname "$0")/lib.sh"

hr
title "Preflight"
check_up "$BASE_MOD/api/v1/moderation/check" && echo "Moderation OK: $BASE_MOD"
check_up "$BASE_MAIN/actuator/health" && echo "Main OK: $BASE_MAIN"
[ -n "$PY3" ] && echo "JSON pretty: python3 -m json.tool" || echo "JSON pretty: (none)"

hr
title "1) OK -> expect 201"
resp="$(req_post_comment "OK" "hello world")"; code="$(printf "%s" "$resp" | status_of)"
echo "HTTP $code"; printf "%s" "$resp" | body_of | print_body; expect_code "$code" "201"

hr
title "2) FORBIDDEN -> expect 422"
resp="$(req_post_comment "FORBIDDEN" "spam message")"; code="$(printf "%s" "$resp" | status_of)"
echo "HTTP $code"; printf "%s" "$resp" | body_of | print_body; expect_code "$code" "422"

hr
title "3) VALIDATION blank -> expect 400"
resp="$(req_post_comment "OK" "   ")"; code="$(printf "%s" "$resp" | status_of)"
echo "HTTP $code"; printf "%s" "$resp" | body_of | print_body; expect_code "$code" "400"

hr
title "SMOKE DONE"
