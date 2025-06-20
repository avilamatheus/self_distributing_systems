#!/bin/bash

echo "[+] Identificando nós no cluster..."
NODES=$(docker node ls --format '{{.Hostname}}')

INDEX=1

for NODE in $NODES; do
  IS_MANAGER=$(docker node inspect "$NODE" --format '{{ .Spec.Role }}')

  if [ "$IS_MANAGER" = "manager" ]; then
    echo "[✓] Definindo $NODE como location=cloud"
    docker node update --label-add location=cloud "$NODE"
  else
    echo "[✓] Definindo $NODE como location=edge$INDEX"
    docker node update --label-add location=edge$INDEX "$NODE"
    INDEX=$((INDEX + 1))
  fi
done

echo "[✓] Labels aplicadas com sucesso."
