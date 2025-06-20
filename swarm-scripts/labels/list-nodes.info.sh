#!/bin/bash

echo "📡 Listando nós do Swarm..."
echo

docker node ls --format '{{.ID}} {{.Hostname}} {{.ManagerStatus}}' | while read id hostname roleinfo; do
  if [[ "$roleinfo" == *Leader* || "$roleinfo" == *Reachable* ]]; then
    role="Manager"
  else
    role="Worker"
  fi

  location=$(docker node inspect "$id" --format '{{ index .Spec.Labels "location" }}')
  location=${location:-<sem label>}

  echo "🖥️  Hostname: $hostname"
  echo "🔧 Função:   $role"
  echo "📍 Local:    $location"
  echo "-------------------------"
done
