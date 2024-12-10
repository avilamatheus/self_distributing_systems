# API container-manager: O que é e como funciona

## Descrição

O container chamado _container-manager_ é responsável por gerenciar a instância de diversos tipos de containers utilizados no projeto de sistemas auto-distribuídos, incluindo os _RemoteDist_. A API foi desenvolvida em Java/Spring Boot, sendo gerenciada com Maven, e utiliza a biblioteca _docker-java_ para comunicação com o Docker Engine.

A API utiliza a imagem _dana_ como base para criar os containers, garantindo que todos os componentes necessários ao projeto sejam instanciados de maneira consistente. Sua responsabilidade inclui instanciar, listar, executar comandos e remover containers, sempre conectando-os à mesma rede Docker que o container _dana_ e a própria API.

## Endpoints Disponíveis

### 1. Instanciar Containers

**Endpoint:**  
`POST /docker/start-containers/{nome-do-container}/{num-de-containers}`

**Descrição:**  
Instancia o número especificado de containers de um tipo específico, utilizando a imagem _dana_ como base. O comando a ser executado pelos containers deve ser especificado no cabeçalho HTTP `cmd`.

---

### 2. Listar Containers

**Endpoint:**  
`GET /docker/list-containers/{nome-do-container}`

**Descrição:**  
Lista todos os containers ativos de um tipo específico.

---

### 3. Listar Containers com Detalhes

**Endpoint:**  
`GET /docker/list-containers/detailed/{nome-do-container}`

**Descrição:**  
Retorna uma lista detalhada dos containers de um tipo específico, incluindo informações como status, ID, logs, ips, portas e outras informações relevantes que o Docker Engine disponibiliza.

---

## Funcionamento

### Lógica de Reutilização de Containers

Para otimizar os recursos do sistema, a API reaproveita containers que foram instanciados anteriormente. A lógica funciona da seguinte maneira:

- **Caso o número de containers solicitados seja maior que o número existente:**  
  A API instanciará apenas o número necessário de novos containers para atingir o valor solicitado.

- **Caso o número de containers solicitados seja menor que o número existente:**  
  A API removerá os containers excedentes para igualar ao valor solicitado.

---

### Configuração de Redes

Todos os containers gerenciados pelo _container-manager_ são automaticamente conectados à mesma rede Docker que o _dana_ e a própria API. Isso facilita a comunicação entre os containers, que podem ser acessados pelo nome diretamente dentro da rede, eliminando a necessidade de expor portas ao host.

---

### Uso da Imagem _Dana_

A API utiliza exclusivamente a imagem _dana_ como base para criar os containers. Essa abordagem garante que todos os componentes instanciados sejam compatíveis e atendam aos requisitos do projeto de sistemas auto-distribuídos. A imagem contém os binários e configurações necessárias para que os containers executem comandos do projeto de maneira eficiente e integrada.

---

## Conclusão

A API _container-manager_ é uma solução central no gerenciamento de containers Docker para o projeto de sistemas auto-distribuídos. Ela oferece funcionalidades como reutilização inteligente de recursos, configuração automática de redes e execução de comandos personalizados, além de garantir a padronização ao utilizar a imagem _dana_ como base para todas as instâncias de containers.
