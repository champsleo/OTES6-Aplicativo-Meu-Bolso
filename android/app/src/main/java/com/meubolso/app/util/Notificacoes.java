package com.meubolso.app.util;

import android.Manifest;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;

import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.core.content.ContextCompat;

import com.meubolso.app.R;
import com.meubolso.app.data.model.AlertaLimite;
import com.meubolso.app.ui.main.MainActivity;

public final class Notificacoes {

    private static final String CANAL_LIMITES = "limites";

    private Notificacoes() {
    }

    // Desde o Android 8, toda notificação precisa de um canal.
    public static void criarCanais(Context context) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) {
            return;
        }
        NotificationChannel canal = new NotificationChannel(CANAL_LIMITES,
                context.getString(R.string.canal_limites), NotificationManager.IMPORTANCE_DEFAULT);
        canal.setDescription(context.getString(R.string.canal_limites_descricao));
        NotificationManager gerenciador = context.getSystemService(NotificationManager.class);
        if (gerenciador != null) {
            gerenciador.createNotificationChannel(canal);
        }
    }

    public static boolean temPermissao(Context context) {
        return Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU
                || ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS)
                == PackageManager.PERMISSION_GRANTED;
    }

    public static void alertaDeLimite(Context context, AlertaLimite alerta) {
        if (alerta == null || !temPermissao(context)) {
            return;
        }
        Context app = context.getApplicationContext();
        criarCanais(app);

        String titulo = alerta.isExcedido()
                ? app.getString(R.string.notificacao_limite_excedido, alerta.getCategoria())
                : app.getString(R.string.notificacao_limite_proximo, alerta.getCategoria(),
                Formatadores.percentual(alerta.getPercentual()));
        String texto = app.getString(R.string.notificacao_limite_texto,
                Formatadores.moeda(alerta.getTotalGasto()), Formatadores.moeda(alerta.getLimiteMensal()));

        Intent abrirApp = new Intent(app, MainActivity.class);
        abrirApp.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent aoTocar = PendingIntent.getActivity(app, 0, abrirApp,
                PendingIntent.FLAG_IMMUTABLE | PendingIntent.FLAG_UPDATE_CURRENT);

        NotificationCompat.Builder notificacao = new NotificationCompat.Builder(app, CANAL_LIMITES)
                .setSmallIcon(R.drawable.ic_notificacao)
                .setContentTitle(titulo)
                .setContentText(texto)
                .setColor(ContextCompat.getColor(app, alerta.isExcedido() ? R.color.despesa : R.color.alerta))
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setContentIntent(aoTocar)
                .setAutoCancel(true);

        try {
            // Um id por categoria: um aviso novo da mesma categoria substitui o anterior.
            NotificationManagerCompat.from(app).notify(alerta.getCategoria().hashCode(), notificacao.build());
        // A permissão pode ter sido retirada nas configurações; aí simplesmente não mostra.
        } catch (SecurityException semPermissao) {
        }
    }
}
