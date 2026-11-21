package com.meubolso.app.util;

import android.graphics.Color;
import android.view.View;
import android.widget.TextView;

import androidx.activity.ComponentActivity;
import androidx.activity.EdgeToEdge;
import androidx.activity.SystemBarStyle;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public final class Telas {

    private Telas() {
    }

    // Chamar antes do setContentView.
    public static void ativarTelaCheia(ComponentActivity activity) {
        EdgeToEdge.enable(activity,
                SystemBarStyle.auto(Color.TRANSPARENT, Color.TRANSPARENT),
                SystemBarStyle.auto(Color.TRANSPARENT, Color.TRANSPARENT));
    }

    // O Android desenha o app atrás das barras do sistema; o espaçamento evita que o conteúdo fique escondido.
    public static void aplicarEspacamento(View raiz) {
        ViewCompat.setOnApplyWindowInsetsListener(raiz, (view, insets) -> {
            Insets barras = insets.getInsets(
                    WindowInsetsCompat.Type.systemBars() | WindowInsetsCompat.Type.ime());
            view.setPadding(barras.left, barras.top, barras.right, barras.bottom);
            return WindowInsetsCompat.CONSUMED;
        });
    }

    public static String texto(TextView campo) {
        CharSequence conteudo = campo.getText();
        return conteudo == null ? "" : conteudo.toString().trim();
    }
}
