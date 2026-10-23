package com.estoque.reserva.reserva;

import org.springframework.data.jpa.repository.JpaRepository;

import java.time.Instant;
import java.util.List;

public interface ReservaRepository extends JpaRepository<Reserva, Long> {

    List<Reserva> findByStatusAndExpiresAtBefore(StatusReserva status, Instant limite);
}
