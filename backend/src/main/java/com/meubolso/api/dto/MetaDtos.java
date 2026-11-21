package com.meubolso.api.dto;

import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.time.LocalDate;

public final class MetaDtos {

    private MetaDtos() {
    }

    public record MetaRequest(
            @NotBlank(message = "Informe o nome da meta")
            @Size(max = 60, message = "O nome deve ter no máximo 60 caracteres")
            String nome,

            @NotNull(message = "Informe o valor da meta")
            @Positive(message = "O valor da meta deve ser maior que zero")
            @Digits(integer = 10, fraction = 2, message = "Use no máximo 2 casas decimais")
            BigDecimal valorAlvo,

            LocalDate prazo) {
    }

    // Valor positivo guarda dinheiro na meta; negativo retira.
    public record MovimentoRequest(
            @NotNull(message = "Informe o valor")
            @Digits(integer = 10, fraction = 2, message = "Use no máximo 2 casas decimais")
            BigDecimal valor) {
    }

    public record MetaResponse(
            Long id,
            String nome,
            BigDecimal valorAlvo,
            BigDecimal valorGuardado,
            BigDecimal valorFaltante,
            double percentual,
            LocalDate prazo,
            BigDecimal valorMensalSugerido,
            boolean concluida) {
    }
}
