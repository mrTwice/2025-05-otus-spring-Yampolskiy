#!/usr/bin/env bash
set -euo pipefail
DIR="$(cd "$(dirname "$0")" && pwd)"

"$DIR/01-smoke.sh"
"$DIR/02-resilience.sh"
"$DIR/03-actuator.sh"
