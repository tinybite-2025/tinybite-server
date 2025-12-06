#!/bin/bash

APP_NAME=tinybite-api
BLUE_PORT=8080
GREEN_PORT=8081
NGINX_CONF=/home/ubuntu/tinybite/nginx/default.conf

echo "deploy start"

echo "Pulling latest image..."
docker pull ghcr.io/tinybite-2025/tinybite-server:latest

if ! docker ps --format '{{.Names}}' | grep -q "${APP_NAME}-blue" && \
   ! docker ps --format '{{.Names}}' | grep -q "${APP_NAME}-green"; then
  echo "First deployment detected — starting blue container..."
  docker compose -f docker-compose.common.yml -f docker-compose.blue.yml up -d
  exit 0
fi


if docker ps --format '{{.Names}}' | grep -q "${APP_NAME}-blue"; then
  CURRENT="blue"
  NEXT="green"
  NEXT_PORT=$GREEN_PORT
else
  CURRENT="green"
  NEXT="blue"
  NEXT_PORT=$BLUE_PORT
fi


echo "Current Container : $CURRENT"
echo "Next Container : $NEXT"

echo "deploy $NEXT Container"
docker compose -f docker-compose.common.yml -f docker-compose.${NEXT}.yml up -d


echo "running health check on port ${NEXT_PORT}"
success=false
for i in {1..20}; do
  sleep 3
  if curl -fs "http://localhost:${NEXT_PORT}/test/health" | grep -q "UP"; then
    echo "Health Check Passed"
    success=true
    break
  fi
  echo "Waiting for Service to be UP ... (${i}/20)"
done


# 실행 실패 시 -> 롤백 진행 후 종료
if [ "$success" = false ]; then
  echo "Health check failed! Rolling back..."
  docker compose -f docker-compose.${NEXT}.yml down
  exit 1
fi


# Reload Nginx
echo "if success, switch nginx conf and stop old container"
  sudo sed -i "s/${APP_NAME}-${CURRENT}/${APP_NAME}-${NEXT}/" $NGINX_CONF
  sudo docker exec nginx nginx -s reload

# Stop old container
echo "==> Stopping old container ${APP_NAME}-${CURRENT}"
docker stop ${APP_NAME}-${CURRENT} || true
docker rm ${APP_NAME}-${CURRENT} || true

echo "Cleaning unused images"
docker image prune -f >/dev/null 2>&1

echo "=============================="
echo "DEPLOYMENT SUCCESSFUL"
echo "Active container: ${NEXT}"
echo "=============================="