package com.estoque.reserva.reserva;

import com.estoque.reserva.dominio.OperacaoInvalidaException;
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

    public void confirmar(Instant agora) {
        if (status != StatusReserva.PENDENTE) {
            throw new OperacaoInvalidaException("só reserva PENDENTE pode ser confirmada");
        }
        if (!agora.isBefore(expiresAt)) {
            throw new OperacaoInvalidaException("reserva expirada não pode ser confirmada");
        }
        status = StatusReserva.CONFIRMADA;
    }

    /** Cancela a reserva e devolve a quantidade ao estoque do produto. */
    public void cancelar() {
        if (status != StatusReserva.PENDENTE && status != StatusReserva.CONFIRMADA) {
            throw new OperacaoInvalidaException("reserva " + status + " não pode ser cancelada");
        }
        status = StatusReserva.CANCELADA;
        produto.devolver(quantidade);
    }

    /** Expira reserva pendente vencida e devolve o estoque. Retorna true se expirou. */
    public boolean expirar(Instant agora) {
        if (status != StatusReserva.PENDENTE || agora.isBefore(expiresAt)) {
            return false;
        }
        status = StatusReserva.EXPIRADA;
        produto.devolver(quantidade);
        return true;
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
