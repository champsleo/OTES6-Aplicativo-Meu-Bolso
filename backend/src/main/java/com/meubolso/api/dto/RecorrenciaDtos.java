package com.meubolso.api.dto;

import com.meubolso.api.model.Recorrencia;
import com.meubolso.api.model.TipoTransacao;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.time.LocalDate;

public final class RecorrenciaDtos {

    private RecorrenciaDtos() {
    }

    public record RecorrenciaRequest(
            @NotBlank(message = "Informe a descrição")
            @Size(max = 100, message = "A descrição deve ter no máximo 100 caracteres")
            String descricao,

            @NotNull(message = "Informe o valor")
            @Positive(message = "O valor deve ser maior que zero")
            @Digits(integer = 10, fraction = 2, message = "Use no máximo 2 casas decimais")
            BigDecimal valor,

            @NotNull(message = "Informe a data do primeiro lançamento")
            LocalDate dataInicio,

            @NotNull(message = "Informe a categoria")
            Long categoriaId,

            @NotNull(message = "Informe a conta")
            Long contaId) {
    }

    public record StatusRequest(
            @NotNull(message = "Informe se a recorrência está ativa")
            Boolean ativa) {
    }

    public record RecorrenciaResponse(
            Long id,
            String descricao,
            BigDecimal valor,
            TipoTransacao tipo,
            int diaDoMes,
            LocalDate dataInicio,
            boolean ativa,
            Long categoriaId,
            String categoriaNome,
            String categoriaCor,
            Long contaId,
            String contaNome,
            LocalDate proximaData) {

        public static RecorrenciaResponse de(Recorrencia r, LocalDate proximaData) {
            return new RecorrenciaResponse(
                    r.getId(),
                    r.getDescricao(),
                    r.getValor(),
                    r.getCategoria().getTipo(),
                    r.getDiaDoMes(),
                    r.getDataInicio(),
                    r.isAtiva(),
                    r.getCategoria().getId(),
                    r.getCategoria().getNome(),
                    r.getCategoria().getCor(),
                    r.getConta().getId(),
                    r.getConta().getNome(),
                    proximaData);
        }
    }
}
