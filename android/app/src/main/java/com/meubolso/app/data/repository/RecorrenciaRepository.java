package com.meubolso.app.data.repository;

import android.content.Context;

import com.meubolso.app.data.model.Recorrencia;
import com.meubolso.app.data.model.RecorrenciaRequest;
import com.meubolso.app.data.model.StatusRequest;
import com.meubolso.app.data.remote.ApiClient;
import com.meubolso.app.data.remote.ApiService;

import java.util.List;

public class RecorrenciaRepository {

    private final ApiService api;

    public RecorrenciaRepository(Context context) {
        this.api = ApiClient.getService(context);
    }

    public void listar(ResultadoCallback<List<Recorrencia>> callback) {
        api.listarRecorrencias().enqueue(new ApiCallback<>(callback));
    }

    public void criar(RecorrenciaRequest request, ResultadoCallback<Recorrencia> callback) {
        api.criarRecorrencia(request).enqueue(new ApiCallback<>(callback));
    }

    public void alterarStatus(long id, boolean ativa, ResultadoCallback<Recorrencia> callback) {
        api.alterarStatusRecorrencia(id, new StatusRequest(ativa)).enqueue(new ApiCallback<>(callback));
    }

    public void excluir(long id, ResultadoCallback<Void> callback) {
        api.excluirRecorrencia(id).enqueue(new ApiCallback<>(callback));
    }
}
