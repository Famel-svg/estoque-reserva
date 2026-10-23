# 04 · API REST

Base: `http://localhost:8080`. Respostas de erro usam `ProblemDetail` (RFC 9457).

## Endpoints

| Método | Caminho | Corpo | Resposta |
|---|---|---|---|
| GET | `/produtos` | — | `200` lista de `{id, nome, estoqueDisponivel}` |
| POST | `/reservas` | `{produtoId, quantidade}` | `201` `{id, produtoId, quantidade, status, expiresAt}` |
| POST | `/reservas/{id}/confirmar` | — | `200` reserva com status `CONFIRMADA` |
| POST | `/reservas/{id}/cancelar` | — | `200` reserva com status `CANCELADA` |

## Erros

| Situação | Status |
|---|---|
| Produto ou reserva inexistente | `404` |
| Estoque insuficiente | `409` |
| Transição de status inválida (ex.: confirmar reserva expirada) | `409` |
| Conflito de `@Version` (outra requisição alterou o produto) | `409` |
| Corpo inválido (`quantidade` não positiva, `produtoId` ausente) | `400` |

## Exemplo

```bash
curl -s http://localhost:8080/produtos

curl -s -X POST http://localhost:8080/reservas \
  -H 'Content-Type: application/json' \
  -d '{"produtoId": 3, "quantidade": 1}'

curl -s -X POST http://localhost:8080/reservas/1/confirmar
```

## Código

- `api/ProdutoController`, `api/ReservaController`: só traduzem HTTP para o serviço.
- `api/TratadorDeErros`: mapeia exceções para status HTTP. Conflito de versão, de Spring ou de JPA, vira `409`.
- `api/*Request` e `api/*Response`: records. As entidades não saem da camada de serviço.
