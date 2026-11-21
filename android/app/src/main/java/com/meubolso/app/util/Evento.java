package com.meubolso.app.util;

// Algo que deve ser tratado uma vez só. Sem isso, girar a tela mostraria a mesma mensagem de novo.
public class Evento<T> {

    private final T conteudo;
    private boolean tratado = false;

    public Evento(T conteudo) {
        this.conteudo = conteudo;
    }

    public T consumir() {
        if (tratado) {
            return null;
        }
        tratado = true;
        return conteudo;
    }
}
