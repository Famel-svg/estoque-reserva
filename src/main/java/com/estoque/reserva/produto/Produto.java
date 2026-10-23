package com.estoque.reserva.produto;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Version;

@Entity
@Table(name = "produto")
public class Produto {

    @Id
    private Long id;

    @Column(nullable = false, length = 120)
    private String nome;

    @Column(name = "estoque_disponivel", nullable = false)
    private int estoqueDisponivel;

    @Version
    private long version;

    protected Produto() {
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
