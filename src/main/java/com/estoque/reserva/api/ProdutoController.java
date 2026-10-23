package com.estoque.reserva.api;

import com.estoque.reserva.reserva.ReservaService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/produtos")
public class ProdutoController {

    private final ReservaService reservaService;

    public ProdutoController(ReservaService reservaService) {
        this.reservaService = reservaService;
    }

    @GetMapping
    public List<ProdutoResponse> listar() {
        return reservaService.listarProdutos().stream()
                .map(ProdutoResponse::de)
                .toList();
    }
}
