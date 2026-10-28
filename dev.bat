@echo off
rem Starts the site with fast-preview settings (see docker-compose.dev.yml).
docker compose -f docker-compose.yml -f docker-compose.dev.yml up -d --build %*
