package com.meubolso.app.data.repository;

import android.content.Context;

import com.meubolso.app.data.model.Meta;
import com.meubolso.app.data.model.MetaRequest;
import com.meubolso.app.data.model.MovimentoRequest;
import com.meubolso.app.data.remote.ApiClient;
import com.meubolso.app.data.remote.ApiService;

import java.math.BigDecimal;
import java.util.List;

public class MetaRepository {

    private final ApiService api;

    public MetaRepository(Context context) {
        this.api = ApiClient.getService(context);
    }

    public void listar(ResultadoCallback<List<Meta>> callback) {
        api.listarMetas().enqueue(new ApiCallback<>(callback));
    }

    public void criar(MetaRequest request, ResultadoCallback<Meta> callback) {
        api.criarMeta(request).enqueue(new ApiCallback<>(callback));
    }

    public void atualizar(long id, MetaRequest request, ResultadoCallback<Meta> callback) {
        api.atualizarMeta(id, request).enqueue(new ApiCallback<>(callback));
    }

    public void movimentar(long id, BigDecimal valor, ResultadoCallback<Meta> callback) {
        api.movimentarMeta(id, new MovimentoRequest(valor)).enqueue(new ApiCallback<>(callback));
    }

    public void excluir(long id, ResultadoCallback<Void> callback) {
        api.excluirMeta(id).enqueue(new ApiCallback<>(callback));
    }
}
