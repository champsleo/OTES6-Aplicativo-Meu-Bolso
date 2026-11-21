package com.meubolso.api.dto;

import java.math.BigDecimal;
import java.util.List;

public final class ResumoDtos {

    private ResumoDtos() {
    }

    public record ResumoMensalResponse(
            String mes,
            BigDecimal totalReceitas,
            BigDecimal totalDespesas,
            BigDecimal saldo,
            int quantidadeLancamentos,
            List<GastoPorCategoria> despesasPorCategoria) {
    }

    public record GastoPorCategoria(
            Long categoriaId,
            String nome,
            String cor,
            BigDecimal total,
            double percentualDoTotal,
            BigDecimal limiteMensal,
            Double percentualDoLimite,
            boolean limiteExcedido) {
    }

    public record HistoricoMensal(
            String mes,
            BigDecimal totalReceitas,
            BigDecimal totalDespesas,
            BigDecimal saldo) {
    }
}
