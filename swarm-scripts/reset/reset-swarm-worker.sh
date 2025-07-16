#!/bin/bash

echo "[!] Saindo do Swarm (se estiver participando)"
docker swarm leave --force

echo "[✓] Worker limpo e removido do cluster."
