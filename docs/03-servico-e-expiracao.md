# 03 · Serviço e expiração

## ReservaService

| Método | Transação | Comportamento |
|---|---|---|
| `listarProdutos()` | somente leitura | Lista todos os produtos. |
| `reservar(produtoId, qtd)` | escrita | Busca o produto, chama `reservar(qtd)` e faz `saveAndFlush`. A checagem de `@Version` acontece ali, dentro do método. Cria a reserva `PENDENTE` com `expiresAt = agora + RESERVA_TTL_MINUTOS`. |
| `confirmar(id)` | escrita | Busca a reserva e chama `confirmar(agora)`. |
| `cancelar(id)` | escrita | Busca a reserva e chama `cancelar()`, que devolve o estoque. |
| `expirarVencidas()` | escrita | Busca reservas `PENDENTE` com `expiresAt < agora`, expira cada uma e devolve o total. |

O relógio vem de um bean `Clock` (`ClockConfig`). Assim o teste controla o tempo sem `Thread.sleep`.

## Expiração agendada

`ReservaExpiracaoJob` chama `expirarVencidas()` com `@Scheduled(fixedDelayString = "${reserva.expiracao.intervalo-ms}")`. O padrão é 60 segundos. `@EnableScheduling` fica na classe principal.

Atraso máximo para uma reserva vencida ser expirada: TTL + intervalo do agendador.

## Configuração (variáveis de ambiente)

| Variável | Padrão | Uso |
|---|---|---|
| `RESERVA_TTL_MINUTOS` | 15 | Prazo para confirmar a reserva |
| `RESERVA_EXPIRACAO_INTERVALO_MS` | 60000 | Intervalo do agendador de expiração |

## Limitação conhecida

`expirarVencidas()` roda num único lote transacional. Se um produto tiver reserva concorrente no mesmo instante, o `@Version` gera conflito e o lote inteiro é desfeito. A próxima execução do agendador tenta de novo. Para volumes altos, o caminho é expirar em lotes menores, com transação por reserva.
