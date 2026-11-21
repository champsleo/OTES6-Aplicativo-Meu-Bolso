package com.meubolso.app.data.model;

import java.math.BigDecimal;

public class TransferenciaRequest {

    private final Long contaOrigemId;
    private final Long contaDestinoId;
    private final BigDecimal valor;
    private final String data;
    private final String descricao;

    public TransferenciaRequest(Long contaOrigemId, Long contaDestinoId, BigDecimal valor, String data, String descricao) {
        this.contaOrigemId = contaOrigemId;
        this.contaDestinoId = contaDestinoId;
        this.valor = valor;
        this.data = data;
        this.descricao = descricao;
    }

    public Long getContaOrigemId() {
        return contaOrigemId;
    }

    public Long getContaDestinoId() {
        return contaDestinoId;
    }

    public BigDecimal getValor() {
        return valor;
    }

    public String getData() {
        return data;
    }

    public String getDescricao() {
        return descricao;
    }
}
