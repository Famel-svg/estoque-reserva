package com.estoque.reserva.reserva;

import com.estoque.reserva.dominio.EstoqueInsuficienteException;
import com.estoque.reserva.produto.Produto;
import com.estoque.reserva.produto.ProdutoRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.transaction.annotation.Transactional;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Testcontainers
class ReservaConcorrenciaTest {

    private static final int THREADS = 50;

    @Container
    @ServiceConnection
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16-alpine");

    @Autowired
    private ReservaService reservaService;

    @Autowired
    private ProdutoRepository produtos;

    @Autowired
    private ReservaRepository reservas;

    @Test
    void exatamenteUmaReservaVenceQuandoCinquentaThreadsDisputamUmaUnidade() throws Exception {
        Produto produto = produtos.save(new Produto("Último item", 1));
        Long produtoId = produto.getId();

        ExecutorService executor = Executors.newFixedThreadPool(THREADS);
        CountDownLatch largada = new CountDownLatch(1);
        AtomicInteger sucessos = new AtomicInteger();
        AtomicInteger semEstoque = new AtomicInteger();
        AtomicInteger conflitoDeVersao = new AtomicInteger();
        List<Future<?>> tarefas = new ArrayList<>();

        for (int i = 0; i < THREADS; i++) {
            tarefas.add(executor.submit(() -> {
                largada.await();
                try {
                    reservaService.reservar(produtoId, 1);
                    sucessos.incrementAndGet();
                } catch (EstoqueInsuficienteException e) {
                    semEstoque.incrementAndGet();
                } catch (OptimisticLockingFailureException e) {
                    conflitoDeVersao.incrementAndGet();
                }
                return null;
            }));
        }

        largada.countDown();
        for (Future<?> tarefa : tarefas) {
            tarefa.get(30, TimeUnit.SECONDS);
        }
        executor.shutdown();
        assertThat(executor.awaitTermination(10, TimeUnit.SECONDS)).isTrue();

        assertThat(sucessos.get()).isEqualTo(1);
        assertThat(sucessos.get() + semEstoque.get() + conflitoDeVersao.get()).isEqualTo(THREADS);

        Produto depois = produtos.findById(produtoId).orElseThrow();
        assertThat(depois.getEstoqueDisponivel()).isZero();
        assertThat(reservas.findAll().stream().filter(r -> r.getProduto().getId().equals(produtoId)).count())
                .isEqualTo(1);
    }

    @Test
    @Transactional
    void reservaVencidaEhExpiradaEDevolveEstoque() {
        Produto produto = produtos.save(new Produto("Expira", 0));
        Instant agora = Instant.now();
        Reserva vencida = reservas.save(new Reserva(produto, 1, StatusReserva.PENDENTE,
                agora.minusSeconds(1200), agora.minusSeconds(300)));

        int expiradas = reservaService.expirarVencidas();

        assertThat(expiradas).isGreaterThanOrEqualTo(1);
        assertThat(vencida.getStatus()).isEqualTo(StatusReserva.EXPIRADA);
        assertThat(produto.getEstoqueDisponivel()).isEqualTo(1);
    }
}
