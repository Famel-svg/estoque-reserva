package com.estoque.reserva.api;

import com.estoque.reserva.reserva.Reserva;
import com.estoque.reserva.reserva.StatusReserva;

import java.time.Instant;

public record ReservaResponse(Long id, Long produtoId, int quantidade, StatusReserva status, Instant expiresAt) {

    public static ReservaResponse de(Reserva reserva) {
        return new ReservaResponse(
                reserva.getId(),
                reserva.getProduto().getId(),
                reserva.getQuantidade(),
                reserva.getStatus(),
                reserva.getExpiresAt());
    }
}
