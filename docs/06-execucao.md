# 06 · Execução

## Local, com Maven

Pré-requisito: JDK 21 e um PostgreSQL acessível.

```bash
./mvnw test
./mvnw spring-boot:run
```

Variáveis lidas de ambiente (valores padrão em `application.yml`):
`DB_URL`, `DB_USER`, `DB_PASSWORD`, `RESERVA_TTL_MINUTOS`, `RESERVA_EXPIRACAO_INTERVALO_MS`.

## Docker Compose

```bash
cp .env.example .env     # ajuste POSTGRES_PASSWORD
docker compose up --build
```

- `postgres`: PostgreSQL 16 com volume `pgdata`. Porta 5432.
- `app`: imagem multi-stage (JDK para compilar, JRE para rodar, usuário sem privilégio). Porta 8080. Espera o banco ficar saudável.
- O Flyway aplica as migrations no startup da aplicação.

## Dockerfile

1. **build**: `eclipse-temurin:21-jdk`. Baixa dependências em camada própria (cache), compila e extrai o jar em camadas com `jarmode=tools`.
2. **runtime**: `eclipse-temurin:21-jre`, usuário não-root. Copia as camadas na ordem de menor para maior mudança.

Testes não rodam dentro da imagem (`-DskipTests`): a verificação fica no CI.

## Cobertura

```bash
./mvnw verify
```

Gera `target/site/jacoco/index.html` e falha se a cobertura de linhas ficar abaixo de 80%.
