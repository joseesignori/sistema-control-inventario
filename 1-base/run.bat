@echo off
rem Ejecuta el sistema (requiere JDK 17+, Maven y MySQL de XAMPP encendido)
cd /d "%~dp0"
mvn -q clean javafx:run
pause
