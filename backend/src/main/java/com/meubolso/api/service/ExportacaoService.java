package com.meubolso.api.service;

import com.meubolso.api.model.TipoTransacao;
import com.meubolso.api.model.Transacao;
import com.meubolso.api.repository.TransacaoRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.List;

@Service
public class ExportacaoService {

    private static final DateTimeFormatter DATA_BR = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    // Sem essa marca, o Excel abre o arquivo com os acentos quebrados.
    private static final String BOM = "﻿";

    private final TransacaoRepository transacaoRepository;

    public ExportacaoService(TransacaoRepository transacaoRepository) {
        this.transacaoRepository = transacaoRepository;
    }

    @Transactional(readOnly = true)
    public String gerarCsv(Long usuarioId, YearMonth mes) {
        List<Transacao> transacoes = transacaoRepository
                .findByUsuarioIdAndDataBetweenOrderByDataDescIdDesc(usuarioId, mes.atDay(1), mes.atEndOfMonth())
                .stream()
                .sorted(Comparator.comparing(Transacao::getData).thenComparing(Transacao::getId))
                .toList();

        StringBuilder csv = new StringBuilder(BOM);
        // Separador ";" e vírgula nos decimais: é o formato que o Excel em português espera.
        csv.append("Data;Descrição;Categoria;Conta;Tipo;Valor\n");
        for (Transacao t : transacoes) {
            csv.append(t.getData().format(DATA_BR)).append(';')
                    .append(campo(t.getDescricao())).append(';')
                    .append(campo(t.getCategoria().getNome())).append(';')
                    .append(campo(t.getConta().getNome())).append(';')
                    .append(t.getTipo() == TipoTransacao.RECEITA ? "Receita" : "Despesa").append(';')
                    .append(t.getValor().toPlainString().replace('.', ','))
                    .append('\n');
        }
        return csv.toString();
    }

    // Aspas evitam que um ";" dentro da descrição quebre as colunas.
    private String campo(String texto) {
        if (texto == null) {
            return "";
        }
        if (texto.contains(";") || texto.contains("\"") || texto.contains("\n")) {
            return "\"" + texto.replace("\"", "\"\"") + "\"";
        }
        return texto;
    }
}
