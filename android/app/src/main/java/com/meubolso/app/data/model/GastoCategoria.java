package com.meubolso.app.data.model;

import java.math.BigDecimal;

public class GastoCategoria {

    private Long categoriaId;
    private String nome;
    private String cor;
    private BigDecimal total;
    private double percentualDoTotal;
    private BigDecimal limiteMensal;
    private Double percentualDoLimite;
    private boolean limiteExcedido;

    public Long getCategoriaId() {
        return categoriaId;
    }

    public String getNome() {
        return nome;
    }

    public String getCor() {
        return cor;
    }

    public BigDecimal getTotal() {
        return total;
    }

    public double getPercentualDoTotal() {
        return percentualDoTotal;
    }

    public BigDecimal getLimiteMensal() {
        return limiteMensal;
    }

    public Double getPercentualDoLimite() {
        return percentualDoLimite;
    }

    public boolean isLimiteExcedido() {
        return limiteExcedido;
    }
}
