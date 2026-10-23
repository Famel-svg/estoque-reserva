package com.estoque.reserva;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class EstoqueReservaApplication {

    public static void main(String[] args) {
        SpringApplication.run(EstoqueReservaApplication.class, args);
    }
}
