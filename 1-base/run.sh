#!/bin/sh
# Ejecuta el sistema (requiere JDK 17+, Maven y MySQL encendido)
cd "$(dirname "$0")" && mvn -q clean javafx:run
