# 02 · Regras de reserva

Regras de domínio ficam nas entidades, não nos controllers. Os testes unitários (JUnit 5 + AssertJ, sem Spring) cobrem cada regra.

## Produto

| Método | Regra |
|---|---|
| `reservar(qtd)` | `qtd > 0` senão `IllegalArgumentException`. `qtd > disponível` lança `EstoqueInsuficienteException`. Senão decrementa o disponível. |
| `devolver(qtd)` | Soma `qtd` ao disponível. Usado por cancelamento e expiração. |

## Reserva

| Método | Regra |
|---|---|
| `confirmar(agora)` | Só `PENDENTE`, e só antes de `expiresAt`. Senão `OperacaoInvalidaException`. Não mexe no estoque: ele já foi decrementado na criação. |
| `cancelar()` | Só `PENDENTE` ou `CONFIRMADA`. Vai para `CANCELADA` e devolve o estoque. |
| `expirar(agora)` | Só `PENDENTE` com `agora >= expiresAt`. Vai para `EXPIRADA`, devolve o estoque e retorna `true`. Caso contrário retorna `false` sem efeito. |

## Ciclo de vida

```
            reservar
  (estoque) ------> PENDENTE --confirmar--> CONFIRMADA
                       |   \                    |
                       |    \--cancelar-----\   |--cancelar--> CANCELADA
                       |                     \--+
                       +--- prazo vence -----> EXPIRADA
                                 (scheduler)
```

Cancelar e expirar devolvem o estoque. Confirmar não.

## Testes

- `ProdutoTest`: 4 casos (reservar, estoque insuficiente, quantidade inválida, devolver).
- `ReservaTest`: 7 casos (confirmar no prazo e fora, cancelar, cancelar duas vezes, expirar vencida, expirar no prazo, não expirar confirmada).
