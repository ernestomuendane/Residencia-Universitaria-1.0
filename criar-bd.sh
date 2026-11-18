#!/bin/sh
# Cria a base de dados rue.   Uso:  ./criar-bd.sh [utilizador] [--com-dados]
# (pede a palavra-passe do MariaDB; use "mysql" em vez de "mariadb" se for o seu cliente)
cd "$(dirname "$0")"
UTIL="${1:-root}"
mariadb -u "$UTIL" -p --default-character-set=utf8mb4 < sql/rue.sql || exit 1
if [ "$2" = "--com-dados" ] || [ "$1" = "--com-dados" ]; then
  mariadb -u "$UTIL" -p --default-character-set=utf8mb4 < sql/rue-dados-exemplo.sql || exit 1
fi
echo "Base de dados rue criada."
