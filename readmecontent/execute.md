# Como executar o projeto

## Pré-requisitos

- Dana ([Como instalar](./installDana.md))
- Docker e Docker Compose ([Como instalar](./installDocker.md))
- Java, Maven([Como instalar](./installJavaMaven.md))

## Executando o projeto

Primeiramente é necessário buildar o codigo fonte do projeto dana, executando o comando `./build.sh`.

Após isso, basta executar o comando `docker compose up --build -d` para instanciar os containers _dana_ e _container-manager_.

Ao final do build, para acessar o container _dana_, execute o comando `docker attach dana` e digite `help` para ver os comandos disponíveis.

Quando escolhido um dos 3 proxies (sharding, propagate, alternate), será solicitado o número de instâncias do componente _RemoteDist_ que o usuário deseja instanciar em containers. Após a escolha, a API irá instanciar os containers e o programa solicitará para que o usuario clique `Enter` para continuar a distribuição. Isso foi feito para que o usuário tenha tempo para executar `docker attach remote-dist{x}` no terminal e ver a lista sendo distribuída entre os containers.

Uma observação importante é que para alterar o número de containers, deve-se primeiro voltar a composição local e posteriormente escolher o proxy desejado com o novo número de containers desejado.
