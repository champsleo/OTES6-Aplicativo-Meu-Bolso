package com.meubolso.app.data.model;

import java.util.List;

public class ErroApi {

    private int status;
    private String mensagem;
    private List<String> detalhes;

    public int getStatus() {
        return status;
    }

    public String getMensagem() {
        return mensagem;
    }

    public List<String> getDetalhes() {
        return detalhes;
    }
}
