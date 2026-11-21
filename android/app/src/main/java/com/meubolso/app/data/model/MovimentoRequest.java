package com.meubolso.app.data.model;

import java.math.BigDecimal;

public class MovimentoRequest {

    private final BigDecimal valor;

    public MovimentoRequest(BigDecimal valor) {
        this.valor = valor;
    }

    public BigDecimal getValor() {
        return valor;
    }
}
