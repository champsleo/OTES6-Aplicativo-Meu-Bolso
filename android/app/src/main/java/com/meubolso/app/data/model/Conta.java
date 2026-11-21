package com.meubolso.app.data.model;

import java.io.Serializable;
import java.math.BigDecimal;

public class Conta implements Serializable {

    private Long id;
    private String nome;
    private TipoConta tipo;
    private BigDecimal saldoInicial;
    private BigDecimal saldoAtual;

    public Long getId() {
        return id;
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

    public BigDecimal getSaldoAtual() {
        return saldoAtual;
    }

    @Override
    // O dropdown usa o toString para exibir o nome.
    public String toString() {
        return nome;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Conta)) return false;
        Conta outra = (Conta) o;
        return java.util.Objects.equals(id, outra.id)
                && java.util.Objects.equals(nome, outra.nome)
                && tipo == outra.tipo
                && java.util.Objects.equals(saldoInicial, outra.saldoInicial)
                && java.util.Objects.equals(saldoAtual, outra.saldoAtual);
    }

    @Override
    public int hashCode() {
        return java.util.Objects.hash(id, nome, tipo, saldoAtual);
    }
}
