package com.meubolso.app.data.repository;

import android.content.Context;

import com.meubolso.app.data.model.TipoTransacao;
import com.meubolso.app.data.model.Transacao;
import com.meubolso.app.data.model.TransacaoRequest;
import com.meubolso.app.data.remote.ApiClient;
import com.meubolso.app.data.remote.ApiService;

import java.time.YearMonth;
import java.util.List;

public class TransacaoRepository {

    private final ApiService api;

    public TransacaoRepository(Context context) {
        this.api = ApiClient.getService(context);
    }

    public void listar(YearMonth mes, TipoTransacao tipo, String busca,
                       ResultadoCallback<List<Transacao>> callback) {
        String termo = (busca == null || busca.trim().isEmpty()) ? null : busca.trim();
        api.listarTransacoes(mes.toString(), tipo, null, termo).enqueue(new ApiCallback<>(callback));
    }

    public void buscar(long id, ResultadoCallback<Transacao> callback) {
        api.buscarTransacao(id).enqueue(new ApiCallback<>(callback));
    }

    public void criar(TransacaoRequest request, ResultadoCallback<Transacao> callback) {
        api.criarTransacao(request).enqueue(new ApiCallback<>(callback));
    }

    public void atualizar(long id, TransacaoRequest request, ResultadoCallback<Transacao> callback) {
        api.atualizarTransacao(id, request).enqueue(new ApiCallback<>(callback));
    }

    public void excluir(long id, ResultadoCallback<Void> callback) {
        api.excluirTransacao(id).enqueue(new ApiCallback<>(callback));
    }
}
