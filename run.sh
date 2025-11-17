#!/bin/sh
set -e

ARGS_FILE=${ARGS_FILE:-/app/args.txt}

# Read args line-by-line into a shell array
ARGS=""
while IFS= read -r line || [ -n "$line" ]; do
  [ -z "$line" ] && continue
  ARGS="$ARGS \"$line\""
done < "$ARGS_FILE"

eval exec java -jar /app/sip-generation-newspapers.jar $ARGS