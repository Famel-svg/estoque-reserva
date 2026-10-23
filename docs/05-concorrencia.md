# 05 · Concorrência

## O problema

Com estoque 1 e dois clientes lendo `estoque_disponivel = 1` ao mesmo tempo, ambos fazem `UPDATE ... SET estoque = 0`. As duas reservas são aceitas: overbooking.

## A proteção

`Produto` tem `@Version`. Cada `UPDATE` do Hibernate vira:

```sql
UPDATE produto SET estoque_disponivel = ?, version = version + 1
WHERE id = ? AND version = ?
```

Se nenhuma linha for afetada, outra transação alterou o produto antes. O Hibernate lança `OptimisticLockException`, que o Spring traduz para `OptimisticLockingFailureException`, e a API responde `409`.

O `saveAndFlush` em `ReservaService.reservar` força esse `UPDATE` dentro do método, então o conflito aparece na chamada, não só no commit.

## Teste de concorrência

`ReservaConcorrenciaTest` (Testcontainers com PostgreSQL 16):

1. Cria um produto com estoque 1.
2. Cria um `ExecutorService` com 50 threads.
3. Todas esperam num `CountDownLatch` e disparam juntas.
4. Cada thread chama `reservaService.reservar(produtoId, 1)`.
5. Classifica o resultado: sucesso, `EstoqueInsuficienteException` ou `OptimisticLockingFailureException`.

Asserções:

- exatamente **1** sucesso;
- soma das três categorias = 50 (nenhuma thread some);
- estoque final = 0;
- exatamente 1 linha de reserva para o produto.

Um segundo teste da mesma classe cobre a expiração real no banco: reserva vencida vira `EXPIRADA` e o estoque volta.

## Por que `@Version` e não lock distribuído

- O recurso é um único banco. Um lock distribuído (Redis, ZooKeeper) adiciona um ponto de falha e a questão de TTL do lock: se o processo morre segurando o lock, ou o TTL expira antes do fim da operação, a exclusão deixa de valer.
- `@Version` não bloqueia: a disputa vira um conflito detectado no `UPDATE`. Quem perde recebe `409` e pode tentar de novo.
- Fica numa única transação do banco. Nada para sincronizar fora dele.
- Custo: sob contenção alta no mesmo produto, muitas requisições recebem `409`. Para o caso de uso (último item), é o comportamento correto: a resposta é honesta.

## Variante pessimista (não usada)

`SELECT ... FOR UPDATE` também evita overbooking e é uma opção válida. Foi descartada porque segura a linha durante toda a transação e não tem `409` para o cliente tentar de novo. Fica como alternativa documentada se o caso de uso mudar.
