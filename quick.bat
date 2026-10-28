@echo off
rem Fastest start: downloads the ready-made app image instead of building it.
docker compose -f docker-compose.yml -f docker-compose.prebuilt.yml up -d --no-build %*
echo.
echo Open http://localhost:8081 in a minute (the first start also creates the database).
