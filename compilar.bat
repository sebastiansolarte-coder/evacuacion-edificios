@echo off
REM Compila el proyecto en Windows y genera evacuacion.jar (requiere JDK 17+).
cd /d "%~dp0"
if exist out rmdir /s /q out
mkdir out
dir /s /b src\*.java > fuentes.txt
javac --release 17 -encoding UTF-8 -d out @fuentes.txt || exit /b 1
del fuentes.txt
jar --create --file evacuacion.jar --main-class evacuacion.App -C out .
echo OK - evacuacion.jar generado
