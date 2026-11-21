package com.meubolso.app.data.model;

import java.math.BigDecimal;

public class ContaRequest {

    private final String nome;
    private final TipoConta tipo;
    private final BigDecimal saldoInicial;

    public ContaRequest(String nome, TipoConta tipo, BigDecimal saldoInicial) {
        this.nome = nome;
        this.tipo = tipo;
        this.saldoInicial = saldoInicial;
    }

    public String getNome() {
        return nome;
    }

    public TipoConta getTipo() {
        return tipo;
    }

    public BigDecimal getSaldoInicial() {
        return saldoInicial;
    }
}
