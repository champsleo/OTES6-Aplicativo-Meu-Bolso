package com.meubolso.app.data.repository;

import androidx.annotation.NonNull;

import com.google.gson.Gson;
import com.meubolso.app.data.model.ErroApi;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

// Traduz a resposta do Retrofit para Resultado e extrai a mensagem de erro que o servidor enviou.
public class ApiCallback<T> implements Callback<T> {

    private static final String SEM_CONEXAO =
            "Não foi possível conectar ao servidor. Verifique se o back-end está rodando.";

    private static final Gson GSON = new Gson();

    private final ResultadoCallback<T> destino;

    public ApiCallback(ResultadoCallback<T> destino) {
        this.destino = destino;
    }

    @Override
    public void onResponse(@NonNull Call<T> call, @NonNull Response<T> response) {
        if (response.isSuccessful()) {
            destino.aoConcluir(Resultado.sucesso(response.body()));
        } else {
            destino.aoConcluir(Resultado.erro(extrairMensagem(response)));
        }
    }

    @Override
    public void onFailure(@NonNull Call<T> call, @NonNull Throwable t) {
        destino.aoConcluir(Resultado.erro(SEM_CONEXAO));
    }

    private String extrairMensagem(Response<T> response) {
        try (ResponseBody corpo = response.errorBody()) {
            if (corpo != null) {
                ErroApi erro = GSON.fromJson(corpo.charStream(), ErroApi.class);
                if (erro != null && erro.getMensagem() != null) {
                    return erro.getMensagem();
                }
            }
        } catch (Exception ignorado) {
        }
        return "Erro inesperado (código " + response.code() + ")";
    }
}
