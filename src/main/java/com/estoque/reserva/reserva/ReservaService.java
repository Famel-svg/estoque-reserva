package com.estoque.reserva.reserva;

import com.estoque.reserva.dominio.RecursoNaoEncontradoException;
import com.estoque.reserva.produto.Produto;
import com.estoque.reserva.produto.ProdutoRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.List;

@Service
public class ReservaService {

    private final ProdutoRepository produtos;
    private final ReservaRepository reservas;
    private final Clock clock;
    private final Duration ttl;

    public ReservaService(ProdutoRepository produtos,
                          ReservaRepository reservas,
                          Clock clock,
                          @Value("${reserva.ttl-minutos}") long ttlMinutos) {
        this.produtos = produtos;
        this.reservas = reservas;
        this.clock = clock;
        this.ttl = Duration.ofMinutes(ttlMinutos);
    }

    @Transactional(readOnly = true)
    public List<Produto> listarProdutos() {
        return produtos.findAll();
    }

    @Transactional
    public Reserva reservar(Long produtoId, int quantidade) {
        Produto produto = produtos.findById(produtoId)
                .orElseThrow(() -> new RecursoNaoEncontradoException("produto " + produtoId + " não encontrado"));
        produto.reservar(quantidade);
        // flush aqui: a checagem de @Version acontece dentro deste método, não só no commit
        produtos.saveAndFlush(produto);

        Instant agora = clock.instant();
        return reservas.save(new Reserva(produto, quantidade, StatusReserva.PENDENTE, agora, agora.plus(ttl)));
    }

    @Transactional
    public Reserva confirmar(Long reservaId) {
        Reserva reserva = buscar(reservaId);
        reserva.confirmar(clock.instant());
        return reserva;
    }

    @Transactional
    public Reserva cancelar(Long reservaId) {
        Reserva reserva = buscar(reservaId);
        reserva.cancelar();
        return reserva;
    }

    /**
     * Expira reservas pendentes vencidas e devolve o estoque.
     * Um conflito de versão desfaz o lote inteiro; a próxima execução do agendador tenta de novo.
     */
    @Transactional
    public int expirarVencidas() {
        Instant agora = clock.instant();
        List<Reserva> vencidas = reservas.findByStatusAndExpiresAtBefore(StatusReserva.PENDENTE, agora);
        int expiradas = 0;
        for (Reserva reserva : vencidas) {
            if (reserva.expirar(agora)) {
                expiradas++;
            }
        }
        return expiradas;
    }

    private Reserva buscar(Long reservaId) {
        return reservas.findById(reservaId)
                .orElseThrow(() -> new RecursoNaoEncontradoException("reserva " + reservaId + " não encontrada"));
    }
}
