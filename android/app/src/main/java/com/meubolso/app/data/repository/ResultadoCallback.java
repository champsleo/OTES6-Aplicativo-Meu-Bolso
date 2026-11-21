package com.meubolso.app.data.repository;

public interface ResultadoCallback<T> {

    void aoConcluir(Resultado<T> resultado);
}
