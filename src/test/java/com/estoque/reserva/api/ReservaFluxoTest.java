package com.estoque.reserva.api;

import com.estoque.reserva.produto.Produto;
import com.estoque.reserva.produto.ProdutoRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.test.web.servlet.MockMvc;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Testcontainers
class ReservaFluxoTest {

    @Container
    @ServiceConnection
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16-alpine");

    @Autowired
    private MockMvc mvc;

    @Autowired
    private ProdutoRepository produtos;

    @Test
    void listaProdutosComEstoque() throws Exception {
        mvc.perform(get("/produtos"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].nome").exists());
    }

    @Test
    void reservarConfirmarECancelarPeloHttp() throws Exception {
        Produto produto = produtos.save(new Produto("Fluxo", 5));
        String corpo = "{\"produtoId\": " + produto.getId() + ", \"quantidade\": 2}";

        String resposta = mvc.perform(post("/reservas").contentType("application/json").content(corpo))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value("PENDENTE"))
                .andReturn().getResponse().getContentAsString();
        long id = Long.parseLong(resposta.replaceAll(".*\"id\":(\\d+).*", "$1"));

        mvc.perform(post("/reservas/" + id + "/confirmar"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("CONFIRMADA"));

        mvc.perform(post("/reservas/" + id + "/cancelar"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("CANCELADA"));

        mvc.perform(post("/reservas/" + id + "/confirmar"))
                .andExpect(status().isConflict());
    }

    @Test
    void estoqueInsuficienteRetorna409NoBanco() throws Exception {
        Produto produto = produtos.save(new Produto("Escasso", 1));
        String corpo = "{\"produtoId\": " + produto.getId() + ", \"quantidade\": 2}";

        mvc.perform(post("/reservas").contentType("application/json").content(corpo))
                .andExpect(status().isConflict())
                .andExpect(content().contentTypeCompatibleWith("application/problem+json"));
    }

    @Test
    void reservaInexistenteRetorna404() throws Exception {
        mvc.perform(post("/reservas/999999/confirmar"))
                .andExpect(status().isNotFound());
    }
}
