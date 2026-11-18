@echo off
rem Cria a base de dados rue (pede a palavra-passe do MariaDB).
rem Uso: criar-bd.bat            -> só a estrutura
rem      criar-bd.bat com-dados  -> estrutura + dados de exemplo
rem Se o seu cliente se chamar "mysql", troque "mariadb" por "mysql" nas linhas abaixo.
cd /d "%~dp0"
mariadb -u root -p --default-character-set=utf8mb4 < sql\rue.sql
if errorlevel 1 exit /b 1
if "%1"=="com-dados" mariadb -u root -p --default-character-set=utf8mb4 < sql\rue-dados-exemplo.sql
echo Base de dados rue criada.
