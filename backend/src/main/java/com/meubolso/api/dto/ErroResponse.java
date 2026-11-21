package com.meubolso.api.dto;

import java.time.Instant;
import java.util.List;

public record ErroResponse(
        int status,
        String erro,
        String mensagem,
        List<String> detalhes,
        Instant timestamp) {

    public static ErroResponse de(int status, String erro, String mensagem, List<String> detalhes) {
        return new ErroResponse(status, erro, mensagem, detalhes, Instant.now());
    }
}
