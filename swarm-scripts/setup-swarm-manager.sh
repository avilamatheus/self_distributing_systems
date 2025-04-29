#!/bin/bash

IP=$1

if [ -z "$IP" ]; then
  echo "Uso: ./setup-swarm-manager.sh <IP_para_advertise-addr>"
  exit 1
fi

echo "[+] Inicializando Swarm com IP: $IP"
docker swarm init --advertise-addr "$IP"

echo "[+] Criando rede overlay attachable: sds_network"
docker network create --driver overlay --attachable sds_network

TOKEN=$(docker swarm join-token -q worker)

# Salva o token na raiz do projeto
echo "$TOKEN" > swarm-worker-token.txt

echo "[✓] Token salvo em swarm-worker-token.txt"
echo
echo "Use este comando nos nós workers:"
echo "docker swarm join --token $TOKEN $IP:2377"
