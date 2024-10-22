# Usar uma imagem do Maven com Java 17 para buildar o projeto
FROM maven:3.9.3-amazoncorretto-17 AS build

# Definir o diretório de trabalho dentro do container
WORKDIR /app

# Copiar os arquivos do projeto Maven para o diretório de trabalho
COPY pom.xml .
COPY src ./src

# Executar o comando para fazer o build do projeto (gera o JAR no diretório target)
RUN mvn clean package -DskipTests

# Usar uma imagem mais leve do JDK para rodar a aplicação
FROM openjdk:17-jdk-slim

# Definir o diretório de trabalho no container final
WORKDIR /app

# Copiar o JAR gerado no estágio de build para o container final
COPY --from=build /app/target/*.jar app.jar

# Definir o comando de entrada para rodar o JAR
ENTRYPOINT ["java", "-jar", "/app/app.jar"]
