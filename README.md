# Como executar o projeto

## Pré-requisitos

- Docker
- Docker Compose
- Docker Swarm
- Tailscale (opcional, para rodar containers na nuvem)

## Instruções

### 1. Setup do nó manager (seu computador local)

- Execute o script `./swarm-scripts/setup-swarm-manager.sh` para configurar o nó manager do Docker Swarm.
- O script pedirá um IP para que os demais nós possam se conectar. Use o IP da sua máquina local ou o IP fornecido pelo Tailscale (caso esteja utilizando).
- A saída do script será:
  - Um comando `docker swarm join` que deverá ser executado nos nós workers;
  - Um arquivo `.txt` contendo o token de join para reutilização posterior.

### 2. Setup dos nós workers (containers na nuvem)

- Em cada worker, execute o script `./swarm-scripts/join-swarm-worker.sh`.
- O script solicitará o IP do nó manager e o token de join obtido no passo anterior.

### 3. Definição de labels nos nós

- Em cada nó, execute:

  ```bash
  docker info --format '{{.Name}}'
  ```

- Use o nome retornado para aplicar os labels:

  ```bash
  docker node update --label-add "location=$LOCATION" "$NODE_NAME"
  ```

  > Onde `$LOCATION` pode ser `edge`, `cloud`, etc., e `$NODE_NAME` é o nome do nó obtido.

### 4. Criar a stack do Docker Swarm

- Execute:

  ```bash
  docker stack deploy -c docker-compose-swarm.yaml sds
  ```

- Isso irá criar a stack usando o arquivo `docker-compose-swarm.yaml`, inicializando a API e o Dana.

### 5. Execução da Dana e da API

- A API `container-manager` ficará acessível na porta `8079`.
- O Dana rodará na porta `8080` do nó manager.
- Para acessar o terminal do Dana:

  ```bash
  docker ps          # pegue o nome do container Dana
  docker attach <nome_do_container>
  ```

---

### 6. Trocando entre composições: local ↔ distribuída

A aplicação Dana pode operar em dois modos principais:

- **Local**: todo o processamento ocorre no próprio container `dana`.
- **Distribuído**: o processamento é delegado para containers `remote-dist`, com três estratégias possíveis:
  - `sharding`
  - `propagate`
  - `alternate`

A troca de composição é **manual**, feita via terminal interativo da Dana. Você digita o modo desejado (`sharding`, `propagate`, `alternate`) e pressiona `Enter`.

Após a escolha, a Dana solicitará que você pressione `Enter` para iniciar a nova composição. **Antes de confirmar, é necessário enviar uma requisição `POST` para a API `container-manager` com a configuração desejada de containers `remote-dist`**:

```http
POST http://localhost:8079/docker/start-containers
Content-Type: application/json

{
  "containerName": "remote-dist",
  "cmd": "dana -sp ../readn RemoteDist.o",
  "deployments": [
    { "location": "edge", "numberOfContainers": 1 },
    { "location": "cloud", "numberOfContainers": 2 }
  ]
}
```

- `containerName`: nome lógico dos containers.
- `cmd`: comando a ser executado nos containers (ex: `dana -sp ../readn RemoteDist.o`).
- `deployments`: define a quantidade e a localização dos containers `remote-dist`.

Você pode enviar essa requisição com `curl`, Postman, Insomnia ou qualquer cliente HTTP. Exemplo com `curl`:

```bash
curl -X POST http://localhost:8079/docker/start-containers \
  -H "Content-Type: application/json" \
  -d @payload.json
```

---

### 7. Alterando a composição distribuída

Caso deseje alterar a **composição distribuída** — seja trocando o modo de execução (`sharding`, `propagate`, `alternate`) ou modificando o número/localização dos contêineres `remote-dist` — siga o fluxo abaixo:

1. No terminal da Dana, **digite `local` e pressione `Enter`** para retornar à composição local.
2. Em seguida, **digite o novo modo desejado** (`sharding`, `propagate` ou `alternate`) e pressione `Enter`.
3. **Antes de confirmar**, envie uma nova requisição `POST` para a API `container-manager` com os parâmetros atualizados, **caso o número ou a localização dos contêineres tenha mudado**.

> ✅ Se você apenas estiver trocando o modo de execução e **os mesmos contêineres `remote-dist` ainda se aplicarem**, **não é necessário reenviar a requisição**. A Dana irá reutilizar os contêineres já em execução.

### 8. Parar a stack

Para parar a stack e remover os serviços, execute:

```bash
docker stack rm sds
```

Isso não removerá os containers `remote-dist` em execução, para isso use o comando:

```bash
docker service ls
```

- Identifique o serviço `remote-dist-LOCATION` e remova-o com:

```
docker service rm remote-dist-LOCATION
```

> LOCATION pode ser `edge`, `cloud`, etc., dependendo de onde os containers estão rodando.

### 8. Scripts utéis

- `./swarm-scripts/labels/list-nodes-info.sh`: lista todos os nós e suas labels, exemplo:

  ```bash
  📡 Listando nós do Swarm...

  🖥️  Hostname: lenovo
  🔧 Função:   Manager
  📍 Local:    edge
  -------------------------
  🖥️  Hostname: sds-tcc-matheus
  🔧 Função:   Worker
  📍 Local:    cloud
  -------------------------
  ```

- `./swarm-scripts/reset/reset-swarm-manager.sh`: reseta o nó manager, removendo o Swarm, remove os serviços e containers, e limpa as redes criadas pelo Swarm.
- `./swarm-scripts/reset/reset-swarm-worker.sh`: reseta o nó worker, saindo do Swarm.
