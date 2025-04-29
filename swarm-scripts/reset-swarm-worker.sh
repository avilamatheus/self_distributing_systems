#!/bin/bash

echo "[!] Removendo todos os containers locais"
docker rm -f $(docker ps -aq) 2>/dev/null

echo "[!] Saindo do Swarm (se estiver participando)"
docker swarm leave --force

echo "[✓] Worker limpo e removido do cluster."
