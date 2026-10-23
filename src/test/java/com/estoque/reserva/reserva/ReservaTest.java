package com.estoque.reserva.reserva;

import com.estoque.reserva.dominio.OperacaoInvalidaException;
import com.estoque.reserva.produto.Produto;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ReservaTest {

    private static final Instant AGORA = Instant.parse("2026-10-23T12:00:00Z");
    private static final Instant PRAZO = AGORA.plusSeconds(900);

    private Produto produto;

    @BeforeEach
    void setUp() {
        produto = new Produto("Mouse", 5);
        produto.reservar(2);
    }

    private Reserva pendente() {
        return new Reserva(produto, 2, StatusReserva.PENDENTE, AGORA, PRAZO);
    }

    @Test
    void confirmarReservaPendenteDentroDoPrazo() {
        Reserva reserva = pendente();

        reserva.confirmar(AGORA.plusSeconds(60));

        assertThat(reserva.getStatus()).isEqualTo(StatusReserva.CONFIRMADA);
    }

    @Test
    void confirmarReservaVencidaFalha() {
        Reserva reserva = pendente();

        assertThatThrownBy(() -> reserva.confirmar(PRAZO))
                .isInstanceOf(OperacaoInvalidaException.class);
    }

    @Test
    void cancelarDevolveEstoque() {
        Reserva reserva = pendente();

        reserva.cancelar();

        assertThat(reserva.getStatus()).isEqualTo(StatusReserva.CANCELADA);
        assertThat(produto.getEstoqueDisponivel()).isEqualTo(5);
    }

    @Test
    void cancelarReservaJaCanceladaFalha() {
        Reserva reserva = pendente();
        reserva.cancelar();

        assertThatThrownBy(reserva::cancelar)
                .isInstanceOf(OperacaoInvalidaException.class);
    }

    @Test
    void expirarReservaVencidaDevolveEstoque() {
        Reserva reserva = pendente();

        boolean expirou = reserva.expirar(PRAZO.plusSeconds(1));

        assertThat(expirou).isTrue();
        assertThat(reserva.getStatus()).isEqualTo(StatusReserva.EXPIRADA);
        assertThat(produto.getEstoqueDisponivel()).isEqualTo(5);
    }

    @Test
    void naoExpiraReservaAindaNoPrazo() {
        Reserva reserva = pendente();

        assertThat(reserva.expirar(AGORA)).isFalse();
        assertThat(reserva.getStatus()).isEqualTo(StatusReserva.PENDENTE);
        assertThat(produto.getEstoqueDisponivel()).isEqualTo(3);
    }

    @Test
    void naoExpiraReservaConfirmada() {
        Reserva reserva = pendente();
        reserva.confirmar(AGORA);

        assertThat(reserva.expirar(PRAZO.plusSeconds(1))).isFalse();
        assertThat(reserva.getStatus()).isEqualTo(StatusReserva.CONFIRMADA);
    }
}
