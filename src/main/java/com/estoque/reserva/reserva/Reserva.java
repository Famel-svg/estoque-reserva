package com.estoque.reserva.reserva;

import com.estoque.reserva.produto.Produto;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

import java.time.Instant;

@Entity
@Table(name = "reserva")
public class Reserva {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "produto_id", nullable = false)
    private Produto produto;

    @Column(nullable = false)
    private int quantidade;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private StatusReserva status;

    @Column(name = "expires_at", nullable = false)
    private Instant expiresAt;

    @Column(name = "criado_em", nullable = false)
    private Instant criadoEm;

    protected Reserva() {
    }

    public Reserva(Produto produto, int quantidade, StatusReserva status, Instant criadoEm, Instant expiresAt) {
        this.produto = produto;
        this.quantidade = quantidade;
        this.status = status;
        this.criadoEm = criadoEm;
        this.expiresAt = expiresAt;
    }

    public Long getId() {
        return id;
    }

    public Produto getProduto() {
        return produto;
    }

    public int getQuantidade() {
        return quantidade;
    }

    public StatusReserva getStatus() {
        return status;
    }

    public Instant getExpiresAt() {
        return expiresAt;
    }

    public Instant getCriadoEm() {
        return criadoEm;
    }
}
