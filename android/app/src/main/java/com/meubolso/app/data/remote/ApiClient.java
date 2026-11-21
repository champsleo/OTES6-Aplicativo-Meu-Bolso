package com.meubolso.app.data.remote;

import android.content.Context;

import com.meubolso.app.R;

import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public final class ApiClient {

    // Uma única instância para o app todo: criar o Retrofit é caro.
    private static volatile ApiService instancia;

    private ApiClient() {
    }

    public static ApiService getService(Context context) {
        if (instancia == null) {
            synchronized (ApiClient.class) {
                if (instancia == null) {
                    instancia = criar(context.getApplicationContext());
                }
            }
        }
        return instancia;
    }

    private static ApiService criar(Context context) {
        // Mostra as requisições no Logcat, ótimo para depurar. Num app publicado, deve ficar desligado (expõe senha e token).
        HttpLoggingInterceptor logging = new HttpLoggingInterceptor();
        logging.setLevel(HttpLoggingInterceptor.Level.BODY);

        OkHttpClient cliente = new OkHttpClient.Builder()
                .addInterceptor(new AuthInterceptor(context))
                .addInterceptor(logging)
                .connectTimeout(10, TimeUnit.SECONDS)
                .readTimeout(15, TimeUnit.SECONDS)
                .build();

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(context.getString(R.string.api_base_url))
                .client(cliente)
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        return retrofit.create(ApiService.class);
    }
}
