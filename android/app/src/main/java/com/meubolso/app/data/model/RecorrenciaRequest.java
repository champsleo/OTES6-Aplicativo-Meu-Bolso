package com.meubolso.app.data.model;

import java.math.BigDecimal;

public class RecorrenciaRequest {

    private final String descricao;
    private final BigDecimal valor;
    private final String dataInicio;
    private final Long categoriaId;
    private final Long contaId;

    public RecorrenciaRequest(String descricao, BigDecimal valor, String dataInicio, Long categoriaId, Long contaId) {
        this.descricao = descricao;
        this.valor = valor;
        this.dataInicio = dataInicio;
        this.categoriaId = categoriaId;
        this.contaId = contaId;
    }

    public String getDescricao() {
        return descricao;
    }

    public BigDecimal getValor() {
        return valor;
    }

    public String getDataInicio() {
        return dataInicio;
    }

    public Long getCategoriaId() {
        return categoriaId;
    }

    public Long getContaId() {
        return contaId;
    }
}
