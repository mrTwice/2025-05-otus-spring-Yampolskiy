#!/usr/bin/env bash
set -euo pipefail

BASE_MAIN="${BASE_MAIN:-http://127.0.0.1:8080}"
BASE_MOD="${BASE_MOD:-http://127.0.0.1:9090}"
BOOK_ID="${BOOK_ID:-1}"
COMMENTS_URL="$BASE_MAIN/api/v1/books/$BOOK_ID/comments"

STEP7_PAR="${STEP7_PAR:-30}"
STEP8_SEQ="${STEP8_SEQ:-20}"
STEP10_N="${STEP10_N:-20}"

VERBOSE="${VERBOSE:-1}"
TAIL_EVENTS="${TAIL_EVENTS:-10}"

have(){ command -v "$1" >/dev/null 2>&1; }
PY3=""; have python3 && PY3="python3" || true

hr(){ printf "\n\033[1m%s\033[0m\n" "============================================================"; }
title(){ printf "\n\033[1m%s\033[0m\n" "$1"; }

pretty_json(){ if [ -n "$PY3" ]; then "$PY3" -m json.tool 2>/dev/null || cat; else cat; fi; }

status_of(){ awk 'BEGIN{code=""} /^HTTP\//{code=$2} END{print code}'; }
body_of(){ awk 'BEGIN{p=0} p{print} /^\r?$/{p=1}'; }
print_body(){ if [ "$VERBOSE" = "1" ]; then cat; else cat >/dev/null; fi; }

expect_code(){ [ "$1" = "$2" ] && echo PASS || echo "FAIL (expected $2)"; }

check_up(){ curl -sS -o /dev/null "$1"; }

req_post_comment(){
  local mode="$1"; local text="$2"
  local mode_hdr=(); [ -n "$mode" ] && mode_hdr=(-H "X-Mode: $mode")
  curl -sS -i -X POST "$COMMENTS_URL" \
    -H "Content-Type: application/json" \
    "${mode_hdr[@]}" \
    -d "{\"text\":\"$text\"}"
}

get_cb_field(){
  local field="$1" json val=""
  json="$(curl -sS "$BASE_MAIN/actuator/circuitbreakers" || true)"

  if [ -n "$PY3" ]; then
    val="$(printf "%s" "$json" | "$PY3" - <<PY 2>/dev/null || true
import json,sys
try:
  d=json.load(sys.stdin)
  print(d["circuitBreakers"]["moderation"].get("$field",""))
except Exception:
  pass
PY
)"
  fi

  if [ -z "$val" ]; then
    if [ "$field" = "state" ]; then
      val="$(printf "%s" "$json" | sed -n 's/.*"moderation"[^{]*{[^}]*"state":"\([A-Z_]*\)".*/\1/p' | head -n1)"
    else
      val="$(printf "%s" "$json" | sed -n "s/.*\"$field\":\\([0-9][0-9]*\\).*/\\1/p" | head -n1)"
    fi
  fi

  echo "${val:-}"
}

count_not_permitted_events(){
  local json n=""
  json="$(curl -sS "$BASE_MAIN/actuator/circuitbreakerevents" || true)"

  if [ -n "$PY3" ]; then
    n="$(printf "%s" "$json" | "$PY3" - <<'PY' 2>/dev/null || true
import json,sys
try:
  d=json.load(sys.stdin)
  ev=d.get("circuitBreakerEvents",[])
  print(sum(1 for e in ev if e.get("type")== "NOT_PERMITTED"))
except Exception:
  pass
PY
)"
  fi

  [ -n "$n" ] && echo "$n" || (printf "%s" "$json" | grep -o '"type":"NOT_PERMITTED"' 2>/dev/null | wc -l | tr -d " ")
}

post_ms(){
  curl -sS -o /dev/null -w "%{time_total}" -X POST "$COMMENTS_URL" \
    -H "Content-Type: application/json" -H "X-Mode: ERROR_503" \
    -d "{\"text\":\"hello\"}" | awk '{printf "%d", ($1*1000)}'
}

print_last_events(){
  local n="${1:-$TAIL_EVENTS}"
  local json
  json="$(curl -sS "$BASE_MAIN/actuator/circuitbreakerevents" || true)"

  if [ -n "${PY3:-}" ]; then
    printf "%s" "$json" | "$PY3" - <<PY 2>/dev/null || true
import json,sys
n=int("$n")
d=json.load(sys.stdin)
ev=d.get("circuitBreakerEvents",[])
tail=ev[-n:] if n>0 else ev
print(json.dumps({"circuitBreakerEvents": tail}, ensure_ascii=False, indent=2))
PY
  else
    # fallback: просто не печатаем полный JSON, чтобы не заспамить консоль
    echo "(events output skipped: install python3 for pretty tail)"
  fi
}

