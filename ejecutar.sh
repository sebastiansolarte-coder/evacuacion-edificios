#!/usr/bin/env bash
# Ejecuta el simulador. Agregue --consola para usar el menú de texto.
set -euo pipefail
cd "$(dirname "$0")"
[ -f evacuacion.jar ] || ./compilar.sh
java -Dfile.encoding=UTF-8 -Dstdout.encoding=UTF-8 -jar evacuacion.jar "$@"
