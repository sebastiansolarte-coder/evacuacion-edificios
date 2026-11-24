@echo off
REM Ejecuta el simulador en Windows. Agregue --consola para el menu de texto.
cd /d "%~dp0"
if not exist evacuacion.jar call compilar.bat
chcp 65001 > nul
java -Dfile.encoding=UTF-8 -Dstdout.encoding=UTF-8 -jar evacuacion.jar %*
