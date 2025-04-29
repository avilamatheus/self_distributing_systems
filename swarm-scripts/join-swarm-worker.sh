#!/bin/bash

read -p "Digite o IP do manager (ex: 100.x.x.x): " MANAGER_IP
read -p "Digite o token de acesso (cole aqui): " TOKEN

echo "[+] Conectando ao Swarm do manager $MANAGER_IP..."
docker swarm join --token "$TOKEN" "$MANAGER_IP:2377"
