package com.meubolso.app.data.repository;

import android.content.Context;

import com.meubolso.app.data.model.Conta;
import com.meubolso.app.data.model.ContaRequest;
import com.meubolso.app.data.model.Transferencia;
import com.meubolso.app.data.model.TransferenciaRequest;
import com.meubolso.app.data.remote.ApiClient;
import com.meubolso.app.data.remote.ApiService;

import java.time.YearMonth;
import java.util.List;

public class ContaRepository {

    private final ApiService api;

    public ContaRepository(Context context) {
        this.api = ApiClient.getService(context);
    }

    public void listar(ResultadoCallback<List<Conta>> callback) {
        api.listarContas().enqueue(new ApiCallback<>(callback));
    }

    public void criar(ContaRequest request, ResultadoCallback<Conta> callback) {
        api.criarConta(request).enqueue(new ApiCallback<>(callback));
    }

    public void atualizar(long id, ContaRequest request, ResultadoCallback<Conta> callback) {
        api.atualizarConta(id, request).enqueue(new ApiCallback<>(callback));
    }

    public void excluir(long id, ResultadoCallback<Void> callback) {
        api.excluirConta(id).enqueue(new ApiCallback<>(callback));
    }

    public void listarTransferencias(YearMonth mes, ResultadoCallback<List<Transferencia>> callback) {
        api.listarTransferencias(mes.toString()).enqueue(new ApiCallback<>(callback));
    }

    public void transferir(TransferenciaRequest request, ResultadoCallback<Transferencia> callback) {
        api.criarTransferencia(request).enqueue(new ApiCallback<>(callback));
    }

    public void excluirTransferencia(long id, ResultadoCallback<Void> callback) {
        api.excluirTransferencia(id).enqueue(new ApiCallback<>(callback));
    }
}
