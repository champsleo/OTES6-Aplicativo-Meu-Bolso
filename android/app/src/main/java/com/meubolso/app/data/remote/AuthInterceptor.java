package com.meubolso.app.data.remote;

import android.content.Context;
import android.content.Intent;

import androidx.annotation.NonNull;

import com.meubolso.app.ui.auth.LoginActivity;
import com.meubolso.app.util.SessionManager;

import java.io.IOException;

import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.Response;

public class AuthInterceptor implements Interceptor {

    private final Context context;
    private final SessionManager sessao;

    public AuthInterceptor(Context context) {
        this.context = context;
        this.sessao = new SessionManager(context);
    }

    @NonNull
    @Override
    public Response intercept(@NonNull Chain chain) throws IOException {
        Request requisicao = chain.request();
        String token = sessao.getToken();

        if (token != null) {
            requisicao = requisicao.newBuilder()
                    .header("Authorization", "Bearer " + token)
                    .build();
        }

        Response resposta = chain.proceed(requisicao);

        // Token expirado: encerra a sessão e volta ao login. Na rota de login, 401 é só senha errada, por isso fica de fora.
        boolean rotaDeLogin = requisicao.url().encodedPath().startsWith("/api/auth");
        if (resposta.code() == 401 && token != null && !rotaDeLogin) {
            sessao.encerrar();
            Intent intent = new Intent(context, LoginActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            intent.putExtra(LoginActivity.EXTRA_SESSAO_EXPIRADA, true);
            context.startActivity(intent);
        }
        return resposta;
    }
}
