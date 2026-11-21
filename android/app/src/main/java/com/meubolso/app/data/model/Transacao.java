package com.meubolso.app.data.model;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Objects;

// Serializable para poder ser devolvida de uma tela para outra (usado no "Desfazer").
public class Transacao implements Serializable {

    private Long id;
    private String descricao;
    private BigDecimal valor;
    private String data;
    private TipoTransacao tipo;
    private Long categoriaId;
    private String categoriaNome;
    private String categoriaCor;
    private Long contaId;
    private String contaNome;
    private boolean recorrente;
    private AlertaLimite alertaLimite;

    public Long getId() {
        return id;
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

    public TipoTransacao getTipo() {
        return tipo;
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

    public boolean isRecorrente() {
        return recorrente;
    }

    public AlertaLimite getAlertaLimite() {
        return alertaLimite;
    }

    public TransacaoRequest paraRequest() {
        return new TransacaoRequest(descricao, valor, data, categoriaId, contaId);
    }

    @Override
    // Usado pelo DiffUtil da lista para saber se o item mudou.
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Transacao)) return false;
        Transacao outra = (Transacao) o;
        return Objects.equals(id, outra.id)
                && Objects.equals(descricao, outra.descricao)
                && Objects.equals(valor, outra.valor)
                && Objects.equals(data, outra.data)
                && tipo == outra.tipo
                && Objects.equals(categoriaId, outra.categoriaId)
                && Objects.equals(categoriaNome, outra.categoriaNome)
                && Objects.equals(categoriaCor, outra.categoriaCor)
                && Objects.equals(contaId, outra.contaId)
                && Objects.equals(contaNome, outra.contaNome)
                && recorrente == outra.recorrente;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, descricao, valor, data, tipo, categoriaId, contaId);
    }
}
