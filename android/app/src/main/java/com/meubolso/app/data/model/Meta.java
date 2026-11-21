package com.meubolso.app.data.model;

import java.io.Serializable;
import java.math.BigDecimal;

public class Meta implements Serializable {

    private Long id;
    private String nome;
    private BigDecimal valorAlvo;
    private BigDecimal valorGuardado;
    private BigDecimal valorFaltante;
    private double percentual;
    private String prazo;
    private BigDecimal valorMensalSugerido;
    private boolean concluida;

    public Long getId() {
        return id;
    }

    public String getNome() {
        return nome;
    }

    public BigDecimal getValorAlvo() {
        return valorAlvo;
    }

    public BigDecimal getValorGuardado() {
        return valorGuardado;
    }

    public BigDecimal getValorFaltante() {
        return valorFaltante;
    }

    public double getPercentual() {
        return percentual;
    }

    public String getPrazo() {
        return prazo;
    }

    public BigDecimal getValorMensalSugerido() {
        return valorMensalSugerido;
    }

    public boolean isConcluida() {
        return concluida;
    }
}
