#!/usr/bin/env bash
set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
TARGET_DIR="${1:-$PWD}"

if [[ ! -d "$TARGET_DIR" ]]; then
  echo "Target directory does not exist: $TARGET_DIR"
  echo "Usage: instantgame.command [target-directory]"
  if [[ -t 0 ]]; then
    read -r -p "Press Enter to close..." _
  fi
  exit 1
fi

cd "$TARGET_DIR"
"$SCRIPT_DIR/instantgame" init

echo
echo "InstantGame template created at: $TARGET_DIR/instantgame/GENERATE.md"
echo "Edit GENERATE.md, then run: $SCRIPT_DIR/instantgame generate"

if [[ -t 0 ]]; then
  read -r -p "Press Enter to close..." _
fi
