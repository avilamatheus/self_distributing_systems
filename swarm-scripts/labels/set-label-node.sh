#!/bin/bash

LOCATION=$1

if [ -z "$LOCATION" ]; then
  echo "Uso: ./set-label-node.sh <edge|cloud|...>"
  exit 1
fi

NODE_NAME=$(docker info --format '{{.Name}}')

echo "[+] Atualizando o nó '$NODE_NAME' com label: location=$LOCATION"
docker node update --label-add "location=$LOCATION" "$NODE_NAME"

echo "[✓] Label 'location=$LOCATION' aplicada com sucesso."
