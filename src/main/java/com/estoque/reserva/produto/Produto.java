package com.estoque.reserva.produto;

import com.estoque.reserva.dominio.EstoqueInsuficienteException;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Version;

@Entity
@Table(name = "produto")
public class Produto {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 120)
    private String nome;

    @Column(name = "estoque_disponivel", nullable = false)
    private int estoqueDisponivel;

    @Version
    private long version;

    protected Produto() {
    }

    public Produto(String nome, int estoqueDisponivel) {
        this.nome = nome;
        this.estoqueDisponivel = estoqueDisponivel;
    }

    public void reservar(int quantidade) {
        if (quantidade <= 0) {
            throw new IllegalArgumentException("quantidade deve ser positiva");
        }
        if (quantidade > estoqueDisponivel) {
            throw new EstoqueInsuficienteException(
                    "estoque insuficiente para " + nome + ": disponível " + estoqueDisponivel);
        }
        estoqueDisponivel -= quantidade;
    }

    public void devolver(int quantidade) {
        estoqueDisponivel += quantidade;
    }

    public Long getId() {
        return id;
    }

    public String getNome() {
        return nome;
    }

    public int getEstoqueDisponivel() {
        return estoqueDisponivel;
    }

    public long getVersion() {
        return version;
    }
}
