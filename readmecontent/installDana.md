# Como instalar o Dana

## Download

O Dana deve ser baixado especificamente na versão 253, disponível para Ubuntu:  
[**Dana 253**](https://www.projectdana.com/download/ubu64/253)

## Instalação

Após o download, descompacte o arquivo para uma pasta de sua preferência e leia o arquivo `HowToInstall.txt` para instruções de instalação. Abaixo está o passo a passo detalhado:

### 1. Configurar a variável de ambiente `DANA_HOME`

Defina a variável de ambiente `DANA_HOME` para apontar para o diretório onde estão os arquivos `dnc` e `dana`.

### 2. Adicionar `DANA_HOME` ao caminho de busca do sistema

Adicione o diretório especificado na variável `DANA_HOME` ao caminho de busca para executáveis do sistema.

### 3. Ajustar permissões dos arquivos no Linux

No Linux, é necessário tornar os arquivos `dana` e `dnc` executáveis. Para isso, abra um terminal na pasta `DANA_HOME` e execute o comando:

```bash
chmod +x dana dnc
```

### 4. Configurar as variáveis de ambiente permanentemente

Na maioria das distribuições Linux, você pode configurar variáveis de ambiente permanentes editando o arquivo `~/.bashrc`. Adicione as seguintes linhas ao final do arquivo:

```bash
export DANA_HOME=/caminho/para/a/pasta/dana/
PATH=$PATH:$DANA_HOME
```

Substitua `/caminho/para/a/pasta/dana/` pelo caminho real onde você descompactou os arquivos.

Depois de salvar o arquivo, reinicie sua sessão (saia e entre novamente no sistema) para aplicar as mudanças.

### 5. Verificar a instalação

Abra um terminal em qualquer lugar e execute o comando:

`dana app.SysTest`

Se o Dana foi instalado corretamente, você verá uma saída indicando que o sistema está funcionando.

### Mais ajuda e tutoriais

Para mais ajuda e tutoriais, acesse a [documentação oficial do Dana](http://www.projectdana.com/).
