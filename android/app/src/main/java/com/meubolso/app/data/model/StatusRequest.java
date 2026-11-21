package com.meubolso.app.data.model;

public class StatusRequest {

    private final Boolean ativa;

    public StatusRequest(Boolean ativa) {
        this.ativa = ativa;
    }

    public Boolean getAtiva() {
        return ativa;
    }
}
