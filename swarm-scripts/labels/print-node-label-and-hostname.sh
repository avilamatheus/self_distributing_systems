#!/bin/bash

# Printa a localização do nó e o hostname
docker node ls --format '{{ .Hostname }}' | while read node; do
  loc=$(docker node inspect "$node" --format '{{ index .Spec.Labels "location" }}')
  echo "$node -> location=$loc"
done
