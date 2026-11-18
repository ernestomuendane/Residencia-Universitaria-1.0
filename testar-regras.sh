#!/bin/sh
# Verifica as regras de negócio do modelo (não precisa de base de dados)
cd "$(dirname "$0")"
java -cp out teste.TesteRegras
