package com.meubolso.api.dto;

import com.meubolso.api.model.Conta;
import com.meubolso.api.model.TipoConta;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

public final class ContaDtos {

    private ContaDtos() {
    }

    public record ContaRequest(
            @NotBlank(message = "Informe o nome da conta")
            @Size(max = 50, message = "O nome deve ter no máximo 50 caracteres")
            String nome,

            @NotNull(message = "Informe o tipo (CARTEIRA, BANCO ou CARTAO_CREDITO)")
            TipoConta tipo,

            // Pode ser negativo (ex.: fatura de cartão já em aberto).
            @NotNull(message = "Informe o saldo inicial (pode ser 0)")
            @Digits(integer = 10, fraction = 2, message = "Valor inválido")
            BigDecimal saldoInicial) {
    }

    public record ContaResponse(
            Long id,
            String nome,
            TipoConta tipo,
            BigDecimal saldoInicial,
            BigDecimal saldoAtual) {

        public static ContaResponse de(Conta conta, BigDecimal saldoAtual) {
            return new ContaResponse(conta.getId(), conta.getNome(), conta.getTipo(),
                    conta.getSaldoInicial(), saldoAtual);
        }
    }
}
