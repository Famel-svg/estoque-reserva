package com.estoque.reserva.api;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record ReservaRequest(@NotNull Long produtoId, @Positive int quantidade) {
}
