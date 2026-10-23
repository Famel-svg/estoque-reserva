# 00 · Escopo

## Problema

Vários clientes podem reservar o último item de um produto ao mesmo tempo. Sem controle de concorrência, duas reservas são aceitas para uma unidade só: o overbooking. Reservas que nunca são confirmadas também prendem estoque e precisam expirar.

## Objetivo

API REST em Java 21 + Spring Boot 3 que reserva estoque **sem overbooking**, com reservas que expiram automaticamente.

## Requisitos

- Entidades: `Produto` (com `estoque` e campo `@Version`) e `Reserva` (status `PENDENTE`, `CONFIRMADA`, `CANCELADA`, `EXPIRADA`; campo `expiresAt`).
- Endpoints:
  - `GET /produtos`
  - `POST /reservas`
  - `POST /reservas/{id}/confirmar`
  - `POST /reservas/{id}/cancelar`
- Reservar decrementa o estoque disponível.
- Reserva não confirmada expira via `@Scheduled` e devolve o estoque.
- Conflito de versão retorna `409`.
- Redis **não** entra nesta versão; aparece no README como evolução.

## Stack

PostgreSQL, Flyway, JUnit 5, Mockito (regras de domínio), Testcontainers (PostgreSQL) para integração.

## Entregáveis

- `Dockerfile` multi-stage e `docker-compose.yml`.
- GitHub Actions: build, testes e JaCoCo com mínimo de 80%.
- README com problema, arquitetura, como rodar, decisões (por que `@Version` e não lock distribuído) e seção "o que faria diferente".
- Teste obrigatório de concorrência: `ExecutorService` com 50 threads disputando 1 unidade. Afirma que exatamente 1 reserva tem sucesso.

## Convenções

- Commits pequenos, mensagens em português.
- Nenhum segredo no repositório: variáveis de ambiente e `.env.example`.

## Motivação

Projeto de portfólio. Concorrência e transação são pergunta clássica de entrevista, e o teste com 50 threads deixa a prova visível e demonstrável.
