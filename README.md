# Sistemas Auto-distribuídos: Instanciação dinâmica de componentes

## Introdução

Este projeto consiste em permitir que os componentes _RemoteDist_ sejam instanciados dinamicamente em tempo de execução, sem a necessidade de recompilação do código fonte. Para isso, foi desenvolvida uma API que se comunica com o Docker Engine para instanciar, listar e remover containers do tipo _RemoteDist_. Como isso foi feito, além de mais detalhes sobre o projeto, serão apresentados nas próximas seções.

## Estrutura do Projeto (Containers)

### dana

O container chamado _dana_ é responsável por executar a aplicação dana, contendo todos os devidos componentes. A imagem do container é criada a partir de um arquivo Dockerfile, que baixa o dana versão 253 e copia o codigo fonte deste projeto para o container, e, ao final, executa o comando `dana -sp "../server;../readn" Distributor.o` para executar a aplicação.

O código fonte dana foi alterado para suportar a instanciação dinâmica de componentes _RemoteDist_. Para isso, durante a escolha do proxy (sharding, propagate, alternate), é solicitado o número de instancias do componente _RemoteDist_ que o usuário deseja instanciar em containers, realizando um POST para a API solicitando a instanciação dos containers.

Além disso, os proxies em si realizam um GET para a API para obter a lista de containers _RemoteDist_ instanciados, e, a partir disso, realizar a distribuição de requisições entre os containers.

### container-manager

O container chamado _container-manager_ é responsável por gerenciar a instância dos containers _RemoteDist_. A API foi desenvolvida em Java/Spring Boot, sendo gerenciada com Maven. A API se comunica com o Docker Engine utilizando a biblioteca _docker-java_.

Basicamente, a API tem como responsabilidade instanciar, listar e remover containers do tipo _RemoteDist_, sempre conectando-os à mesma rede Docker que o container _dana_ e a API estão conectados.

A API possui os seguintes endpoints:

- `GET /docker/list-containers`: Lista todos os containers do tipo _RemoteDist_ instanciados.
- `GET /docker/list-containers-detailed`: Lista todos os containers do tipo _RemoteDist_ instanciados, com informações detalhadas.
- `POST /docker/start-containers/{num}`: Instancia um número `num` de containers do tipo _RemoteDist_.
- `DELETE /docker/remove-containers`: Remove todos os containers do tipo _RemoteDist_ instanciados.

A imagem do container é buildada a partir de um Dockerfile que copia o código fonte da API para o container e executa comandos maven para buildar a aplicação e ao final executa-lá com comandos java.

Outro aspecto a se considerar é que a API reaproveita remote-dists que foram instanciados anteriormente, evitando a necessidade de dropar e criar novos containers a cada requisição. Se o usuario solicitar a instanciação de 5 containers, e já existirem 3 containers instanciados, a API irá instanciar apenas 2 novos containers. Caso contrário, se o usuário solicitar a instanciação de 2 containers, e já existirem 5 containers instanciados, a API não irá instanciar novos containers, mas sim dropar os containers excedentes.

Outro ponto relevante é que, por estar na mesma rede Docker, os remotes dists instanciados pela API são acessíveis pelo container _dana_ apenas pelo nome do container, sem a necessidade de expor portas, facilitando a comunicação entre os containers.

### remote-dist{x}

Os containers _remote-dist{x}_ são instâncias do componente _RemoteDist_ que são instanciados dinamicamente pela API. Onde _{x}_ é um número que identifica o container.

Para buildar os RemotesDists, a API reaproveita a imagem do container _dana_, apenas alterando o comando de execução para `dana -sp ../readn RemoteDist.o`.

## docker-compose.yaml

Nesse arquivo é definido a estrutura dos containers que serão instanciados primeiramente, sendo eles o _dana_ e o _container-manager_. Além disso, é definida a rede Docker que os containers estarão conectados, nesse caso, `sds_network`.

O container _dana_ é exposto na porta 8080 do host, enquanto a API é exposta na porta 8079. Os remote-dists não possuem portas expostas, sendo acessíveis apenas dentro da _sds_network_ pelos containers _dana_ e _container-manager_.

## Pré Requisitos

- Dana versão 253 (disponível em https://www.projectdana.com/download/ubu64/253, para Ubuntu 64 bits)
- Docker e Docker Compose (disponíveis em https://docs.docker.com/get-docker/ e https://docs.docker.com/compose/install/)

## Execução

Primeiramente é necessário buildar o codigo fonte do projeto dana, executando o comando `./build.sh`.

Após isso, basta executar o comando `docker compose up --build -d` para instanciar os containers _dana_ e _container-manager_.

Ao final do build, para acessar o container _dana_, execute o comando `docker attach dana` e digite `help` para ver os comandos disponíveis.

Quando escolhido um dos 3 proxies (sharding, propagate, alternate), será solicitado o número de instâncias do componente _RemoteDist_ que o usuário deseja instanciar em containers. Após a escolha, a API irá instanciar os containers e o programa solicitará para que o usuario clique `Enter` para continuar a distribuição. Isso foi feito para que o usuário tenha tempo para dar `docker attach remote-dist{x}` e ver a lista sendo distribuída entre os containers.

Uma observação importante é que para alterar o número de containers, deve-se primeiro voltar a composição local e posteriormente escolher o proxy desejado com o novo número de containers desejado.
