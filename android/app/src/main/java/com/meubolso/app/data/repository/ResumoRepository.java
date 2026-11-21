package com.meubolso.app.data.repository;

import android.content.Context;

import com.meubolso.app.data.model.HistoricoMensal;
import com.meubolso.app.data.model.ResumoMensal;
import com.meubolso.app.data.remote.ApiClient;
import com.meubolso.app.data.remote.ApiService;

import java.time.YearMonth;
import java.util.List;

public class ResumoRepository {

    private final ApiService api;

    public ResumoRepository(Context context) {
        this.api = ApiClient.getService(context);
    }

    public void buscar(YearMonth mes, ResultadoCallback<ResumoMensal> callback) {
        api.buscarResumo(mes.toString()).enqueue(new ApiCallback<>(callback));
    }

    public void historico(int meses, YearMonth ate, ResultadoCallback<List<HistoricoMensal>> callback) {
        api.buscarHistorico(meses, ate.toString()).enqueue(new ApiCallback<>(callback));
    }
}
