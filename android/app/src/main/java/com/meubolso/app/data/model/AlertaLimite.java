package com.meubolso.app.data.model;

import java.io.Serializable;
import java.math.BigDecimal;

public class AlertaLimite implements Serializable {

    private String categoria;
    private BigDecimal totalGasto;
    private BigDecimal limiteMensal;
    private double percentual;
    private boolean excedido;

    public String getCategoria() {
        return categoria;
    }

    public BigDecimal getTotalGasto() {
        return totalGasto;
    }

    public BigDecimal getLimiteMensal() {
        return limiteMensal;
    }

    public double getPercentual() {
        return percentual;
    }

    public boolean isExcedido() {
        return excedido;
    }
}
