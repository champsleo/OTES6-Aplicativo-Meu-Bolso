package com.meubolso.app.data.model;

import java.math.BigDecimal;

public class TransacaoRequest {

    private final String descricao;
    private final BigDecimal valor;
    private final String data;
    private final Long categoriaId;
    private final Long contaId;

    public TransacaoRequest(String descricao, BigDecimal valor, String data, Long categoriaId, Long contaId) {
        this.descricao = descricao;
        this.valor = valor;
        this.data = data;
        this.categoriaId = categoriaId;
        this.contaId = contaId;
    }

    public String getDescricao() {
        return descricao;
    }

    public BigDecimal getValor() {
        return valor;
    }

    public String getData() {
        return data;
    }

    public Long getCategoriaId() {
        return categoriaId;
    }

    public Long getContaId() {
        return contaId;
    }
}
