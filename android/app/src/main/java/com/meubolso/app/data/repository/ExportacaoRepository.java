package com.meubolso.app.data.repository;

import android.content.Context;

import com.meubolso.app.data.remote.ApiClient;
import com.meubolso.app.data.remote.ApiService;

import java.time.YearMonth;

import okhttp3.ResponseBody;

public class ExportacaoRepository {

    private final ApiService api;

    public ExportacaoRepository(Context context) {
        this.api = ApiClient.getService(context);
    }

    public void baixarCsv(YearMonth mes, ResultadoCallback<ResponseBody> callback) {
        api.exportarCsv(mes.toString()).enqueue(new ApiCallback<>(callback));
    }
}
