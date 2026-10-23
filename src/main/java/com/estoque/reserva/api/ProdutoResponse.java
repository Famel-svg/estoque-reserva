package com.estoque.reserva.api;

import com.estoque.reserva.produto.Produto;

public record ProdutoResponse(Long id, String nome, int estoqueDisponivel) {

    public static ProdutoResponse de(Produto produto) {
        return new ProdutoResponse(produto.getId(), produto.getNome(), produto.getEstoqueDisponivel());
    }
}
