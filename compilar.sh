#!/usr/bin/env bash
# Compila el proyecto y genera evacuacion.jar (requiere JDK 17 o superior).
set -euo pipefail
cd "$(dirname "$0")"
rm -rf out
mkdir -p out
find src -name "*.java" > fuentes.txt
javac --release 17 -encoding UTF-8 -Xlint:all,-serial,-this-escape -d out @fuentes.txt
rm -f fuentes.txt
jar --create --file evacuacion.jar --main-class evacuacion.App -C out .
echo "OK -> evacuacion.jar generado"
