#!/usr/bin/env bash
# Compila el código y las pruebas, y ejecuta las pruebas automáticas.
set -euo pipefail
cd "$(dirname "$0")"
rm -rf out-pruebas
mkdir -p out-pruebas
find src test -name "*.java" > fuentes.txt
javac --release 17 -encoding UTF-8 -d out-pruebas @fuentes.txt
rm -f fuentes.txt
java -Dstdout.encoding=UTF-8 -cp out-pruebas evacuacion.pruebas.PruebasProyecto
