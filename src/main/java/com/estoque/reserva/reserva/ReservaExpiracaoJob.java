package com.estoque.reserva.reserva;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class ReservaExpiracaoJob {

    private static final Logger log = LoggerFactory.getLogger(ReservaExpiracaoJob.class);

    private final ReservaService reservaService;

    public ReservaExpiracaoJob(ReservaService reservaService) {
        this.reservaService = reservaService;
    }

    @Scheduled(fixedDelayString = "${reserva.expiracao.intervalo-ms}")
    public void expirarVencidas() {
        int expiradas = reservaService.expirarVencidas();
        if (expiradas > 0) {
            log.info("{} reserva(s) expirada(s), estoque devolvido", expiradas);
        }
    }
}
