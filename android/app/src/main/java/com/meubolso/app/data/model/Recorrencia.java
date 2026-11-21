package com.meubolso.app.data.model;

import java.math.BigDecimal;

public class Recorrencia {

    private Long id;
    private String descricao;
    private BigDecimal valor;
    private TipoTransacao tipo;
    private int diaDoMes;
    private String dataInicio;
    private boolean ativa;
    private Long categoriaId;
    private String categoriaNome;
    private String categoriaCor;
    private Long contaId;
    private String contaNome;
    private String proximaData;

    public Long getId() {
        return id;
    }

    public String getDescricao() {
        return descricao;
    }

    public BigDecimal getValor() {
        return valor;
    }

    public TipoTransacao getTipo() {
        return tipo;
    }

    public int getDiaDoMes() {
        return diaDoMes;
    }

    public String getDataInicio() {
        return dataInicio;
    }

    public boolean isAtiva() {
        return ativa;
    }

    public Long getCategoriaId() {
        return categoriaId;
    }

    public String getCategoriaNome() {
        return categoriaNome;
    }

    public String getCategoriaCor() {
        return categoriaCor;
    }

    public Long getContaId() {
        return contaId;
    }

    public String getContaNome() {
        return contaNome;
    }

    public String getProximaData() {
        return proximaData;
    }
}
