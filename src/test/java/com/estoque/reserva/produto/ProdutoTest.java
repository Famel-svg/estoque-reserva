package com.estoque.reserva.produto;

import com.estoque.reserva.dominio.EstoqueInsuficienteException;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ProdutoTest {

    @Test
    void reservarDecrementaEstoqueDisponivel() {
        Produto produto = new Produto("Teclado", 5);

        produto.reservar(3);

        assertThat(produto.getEstoqueDisponivel()).isEqualTo(2);
    }

    @Test
    void reservarAcimaDoDisponivelFalha() {
        Produto produto = new Produto("Teclado", 1);

        assertThatThrownBy(() -> produto.reservar(2))
                .isInstanceOf(EstoqueInsuficienteException.class);
        assertThat(produto.getEstoqueDisponivel()).isEqualTo(1);
    }

    @Test
    void reservarQuantidadeNaoPositivaFalha() {
        Produto produto = new Produto("Teclado", 5);

        assertThatThrownBy(() -> produto.reservar(0))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void devolverRepoeEstoque() {
        Produto produto = new Produto("Teclado", 0);

        produto.devolver(4);

        assertThat(produto.getEstoqueDisponivel()).isEqualTo(4);
    }
}
