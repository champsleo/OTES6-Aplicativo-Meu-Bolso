package com.meubolso.app.data.model;

import java.math.BigDecimal;

public class MetaRequest {

    private final String nome;
    private final BigDecimal valorAlvo;
    private final String prazo;

    public MetaRequest(String nome, BigDecimal valorAlvo, String prazo) {
        this.nome = nome;
        this.valorAlvo = valorAlvo;
        this.prazo = prazo;
    }

    public String getNome() {
        return nome;
    }

    public BigDecimal getValorAlvo() {
        return valorAlvo;
    }

    public String getPrazo() {
        return prazo;
    }
}
