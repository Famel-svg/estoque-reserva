package com.estoque.reserva.api;

import com.estoque.reserva.dominio.EstoqueInsuficienteException;
import com.estoque.reserva.reserva.ReservaService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ReservaController.class)
class ReservaControllerTest {

    @Autowired
    private MockMvc mvc;

    @MockBean
    private ReservaService reservaService;

    @Test
    void estoqueInsuficienteRetorna409() throws Exception {
        when(reservaService.reservar(anyLong(), anyInt()))
                .thenThrow(new EstoqueInsuficienteException("sem estoque"));

        mvc.perform(post("/reservas")
                        .contentType("application/json")
                        .content("{\"produtoId\": 1, \"quantidade\": 1}"))
                .andExpect(status().isConflict());
    }

    @Test
    void quantidadeInvalidaRetorna400() throws Exception {
        mvc.perform(post("/reservas")
                        .contentType("application/json")
                        .content("{\"produtoId\": 1, \"quantidade\": 0}"))
                .andExpect(status().isBadRequest());
    }
}
