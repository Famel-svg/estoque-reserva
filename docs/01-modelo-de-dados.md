# 01 · Modelo de dados

Schema versionado com Flyway (`src/main/resources/db/migration`). Hibernate roda com `ddl-auto: validate`: o schema é do Flyway, nunca do JPA.

## Tabelas

| Tabela | Coluna | Tipo | Observação |
|---|---|---|---|
| `produto` | `id` | BIGSERIAL | PK |
| | `nome` | VARCHAR(120) | obrigatório |
| | `estoque_disponivel` | INTEGER | `CHECK (>= 0)`: o banco também barra estoque negativo |
| | `version` | BIGINT | controle otimista (`@Version`) |
| `reserva` | `id` | BIGSERIAL | PK |
| | `produto_id` | BIGINT | FK para `produto` |
| | `quantidade` | INTEGER | `CHECK (> 0)` |
| | `status` | VARCHAR(20) | `PENDENTE`, `CONFIRMADA`, `CANCELADA`, `EXPIRADA` |
| | `expires_at` | TIMESTAMPTZ | prazo para confirmação |
| | `criado_em` | TIMESTAMPTZ | |

Índice `idx_reserva_status_expires_at` acelera a varredura de expiração (`status = PENDENTE AND expires_at < agora`).

## Migrations

- `V1__cria_produto_e_reserva.sql`: cria tabelas, constraints e índice.
- `V2__dados_iniciais.sql`: três produtos de exemplo. Um deles tem estoque 1, útil para testar disputa.

## Entidades

- `Produto` e `Reserva` ficam em pacotes separados (`produto`, `reserva`).
- `Produto.version` é o ponto da proteção contra overbooking (ver [04-concorrencia.md](04-concorrencia.md)).
