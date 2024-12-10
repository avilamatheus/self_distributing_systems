# Como instalar Java e Maven

Para instalar o Java e o Maven, usaremos o SDKMAN. O SDKMAN é um gerenciador de versões de software para Java, Scala, Groovy, Kotlin, entre outras linguagens. Ele permite instalar, gerenciar e alternar entre diferentes versões de software.

## Instalando o SDKMAN

Para instalar o SDKMAN, execute o seguinte comando no terminal:

```bash
curl -s "https://get.sdkman.io" | bash
```

Após a instalação, execute o seguinte comando para carregar o SDKMAN no terminal

```bash
source ~/.sdkman/bin/sdkman-init.sh
```

Outras formas de instalação e informações podem ser encontradas no [site oficial do SDKMAN](https://sdkman.io/).

## Java

Com o SDKMAN instalado, podemos instalar o Java. Antes disso, verifique as versões disponíveis com o comando:

```bash
sdk list java
```

No caso deste projeto, foi utilizado o Java 17 (17.0.12-zulu). Para instalar essa versão, execute o comando:

```bash
sdk install java 17.0.12-zulu
```

Para verificar se a instalação foi bem-sucedida, execute o comando:

```bash
java -version
```

## Maven

Com o Java instalado, podemos instalar o Maven. Verifique as versões disponíveis com o comando:

```bash
sdk list maven
```

No caso deste projeto, foi utilizado o Maven 3.9.3. Para instalar essa versão, execute o comando:

```bash
sdk install maven 3.9.3
```

Para verificar se a instalação foi bem-sucedida, execute o comando:

```bash
mvn -version
```
