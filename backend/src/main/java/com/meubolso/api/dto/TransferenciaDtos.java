package com.meubolso.api.dto;

import com.meubolso.api.model.Transferencia;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.time.LocalDate;

public final class TransferenciaDtos {

    private TransferenciaDtos() {
    }

    public record TransferenciaRequest(
            @NotNull(message = "Informe a conta de origem")
            Long contaOrigemId,

            @NotNull(message = "Informe a conta de destino")
            Long contaDestinoId,

            @NotNull(message = "Informe o valor")
            @Positive(message = "O valor deve ser maior que zero")
            @Digits(integer = 10, fraction = 2, message = "Use no máximo 2 casas decimais")
            BigDecimal valor,

            @NotNull(message = "Informe a data")
            LocalDate data,

            @Size(max = 100, message = "A descrição deve ter no máximo 100 caracteres")
            String descricao) {
    }

    public record TransferenciaResponse(
            Long id,
            Long contaOrigemId,
            String contaOrigemNome,
            Long contaDestinoId,
            String contaDestinoNome,
            BigDecimal valor,
            LocalDate data,
            String descricao) {

        public static TransferenciaResponse de(Transferencia t) {
            return new TransferenciaResponse(
                    t.getId(),
                    t.getContaOrigem().getId(),
                    t.getContaOrigem().getNome(),
                    t.getContaDestino().getId(),
                    t.getContaDestino().getNome(),
                    t.getValor(),
                    t.getData(),
                    t.getDescricao());
        }
    }
}
