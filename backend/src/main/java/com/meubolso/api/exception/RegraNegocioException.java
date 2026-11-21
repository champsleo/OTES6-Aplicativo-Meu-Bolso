package com.meubolso.api.exception;

// Vira 422: os dados estão no formato certo, mas quebram uma regra do sistema.
public class RegraNegocioException extends RuntimeException {

    public RegraNegocioException(String mensagem) {
        super(mensagem);
    }
}
