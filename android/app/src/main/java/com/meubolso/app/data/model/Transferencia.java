package com.meubolso.app.data.model;

import java.io.Serializable;
import java.math.BigDecimal;

public class Transferencia implements Serializable {

    private Long id;
    private Long contaOrigemId;
    private String contaOrigemNome;
    private Long contaDestinoId;
    private String contaDestinoNome;
    private BigDecimal valor;
    private String data;
    private String descricao;

    public Long getId() {
        return id;
    }

    public Long getContaOrigemId() {
        return contaOrigemId;
    }

    public String getContaOrigemNome() {
        return contaOrigemNome;
    }

    public Long getContaDestinoId() {
        return contaDestinoId;
    }

    public String getContaDestinoNome() {
        return contaDestinoNome;
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

    public TransferenciaRequest paraRequest() {
        return new TransferenciaRequest(contaOrigemId, contaDestinoId, valor, data, descricao);
    }
}
