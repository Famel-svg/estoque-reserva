package com.estoque.reserva.reserva;

import com.estoque.reserva.dominio.EstoqueInsuficienteException;
import com.estoque.reserva.dominio.RecursoNaoEncontradoException;
import com.estoque.reserva.produto.Produto;
import com.estoque.reserva.produto.ProdutoRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ReservaServiceTest {

    private static final Instant AGORA = Instant.parse("2026-10-23T12:00:00Z");

    @Mock
    private ProdutoRepository produtos;

    @Mock
    private ReservaRepository reservas;

    private ReservaService service;

    @BeforeEach
    void setUp() {
        Clock clock = Clock.fixed(AGORA, ZoneOffset.UTC);
        service = new ReservaService(produtos, reservas, clock, 15);
    }

    @Test
    void reservarDecrementaEstoqueERegistraPrazo() {
        Produto produto = new Produto("Teclado", 3);
        when(produtos.findById(1L)).thenReturn(Optional.of(produto));
        when(reservas.save(any(Reserva.class))).thenAnswer(inv -> inv.getArgument(0));

        Reserva reserva = service.reservar(1L, 2);

        assertThat(produto.getEstoqueDisponivel()).isEqualTo(1);
        assertThat(reserva.getStatus()).isEqualTo(StatusReserva.PENDENTE);
        assertThat(reserva.getExpiresAt()).isEqualTo(AGORA.plusSeconds(900));
    }

    @Test
    void reservarSemEstoqueNaoPersisteReserva() {
        Produto produto = new Produto("Teclado", 1);
        when(produtos.findById(1L)).thenReturn(Optional.of(produto));

        assertThatThrownBy(() -> service.reservar(1L, 2))
                .isInstanceOf(EstoqueInsuficienteException.class);
        verify(reservas, never()).save(any());
    }

    @Test
    void reservarProdutoInexistenteFalha() {
        when(produtos.findById(9L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.reservar(9L, 1))
                .isInstanceOf(RecursoNaoEncontradoException.class);
    }

    @Test
    void confirmarReservaInexistenteFalha() {
        when(reservas.findById(7L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.confirmar(7L))
                .isInstanceOf(RecursoNaoEncontradoException.class);
    }

    @Test
    void expirarVencidasDevolveEstoqueDasReservasVencidas() {
        Produto produto = new Produto("Teclado", 0);
        Reserva vencida = new Reserva(produto, 2, StatusReserva.PENDENTE,
                AGORA.minusSeconds(1000), AGORA.minusSeconds(100));
        when(reservas.findByStatusAndExpiresAtBefore(StatusReserva.PENDENTE, AGORA))
                .thenReturn(List.of(vencida));

        int expiradas = service.expirarVencidas();

        assertThat(expiradas).isEqualTo(1);
        assertThat(produto.getEstoqueDisponivel()).isEqualTo(2);
        assertThat(vencida.getStatus()).isEqualTo(StatusReserva.EXPIRADA);
    }
}
