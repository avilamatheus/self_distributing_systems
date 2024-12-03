# Etapa 1: Construa a imagem intermediária 'dana'
FROM ubuntu:20.04

ENV DANA_HOME=/dana_home

# Instale as dependências necessárias
RUN apt-get update && \
    apt-get install -y \
    wget \
    unzip \
    curl \
    && rm -rf /var/lib/apt/lists/*

# Crie o diretório DANA_HOME
RUN mkdir -p $DANA_HOME

# Baixe e descompacte o DANA
RUN wget -O /tmp/dana.zip https://www.projectdana.com/download/ubu64/253 --no-check-certificate && \
    unzip /tmp/dana.zip -d $DANA_HOME && \
    rm /tmp/dana.zip

# Altere as permissões dos arquivos dnc e dana
RUN chmod +x $DANA_HOME/dana $DANA_HOME/dnc

# Adicione DANA_HOME ao PATH
ENV PATH=$PATH:$DANA_HOME

# Defina o diretório de trabalho
RUN mkdir -p /self_distributing_systems

COPY ./client /self_distributing_systems/client
COPY ./constant /self_distributing_systems/constant
COPY ./distributor /self_distributing_systems/distributor
COPY ./readn /self_distributing_systems/readn
COPY ./readn-writen /self_distributing_systems/readn-writen
COPY ./server /self_distributing_systems/server
COPY ./writen /self_distributing_systems/writen

WORKDIR /self_distributing_systems/distributor

# Comando específico da imagem final
CMD ["dana", "-sp", "../server;../readn", "Distributor.o"]