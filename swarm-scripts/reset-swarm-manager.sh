#!/bin/bash

echo "[!] Removendo stack sds (se existir)"
docker stack rm sds

echo "[!] Removendo todos os serviços Swarm"
docker service rm $(docker service ls -q) 2>/dev/null

echo "[!] Removendo todos os containers"
docker rm -f $(docker ps -aq) 2>/dev/null

echo "[!] Removendo rede sds_network (se existir)"
docker network rm sds_network 2>/dev/null

echo "[!] Saindo do Swarm (se estiver participando)"
docker swarm leave --force

echo "[✓] Manager resetado com sucesso."