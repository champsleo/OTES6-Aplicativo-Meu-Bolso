package com.meubolso.app.ui.resumo;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.View;

import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;

import com.meubolso.app.R;

import com.meubolso.app.data.model.GastoCategoria;
import com.meubolso.app.util.Formatadores;

import java.util.ArrayList;
import java.util.List;

// Gráfico de rosca desenhado à mão com Canvas, sem bibliotecas.
public class GraficoPizzaView extends View {

    private static final float ESPESSURA_RELATIVA = 0.22f;
    private static final float ESPACO_ENTRE_FATIAS = 1.5f;

    private final Paint pincelFatia = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint pincelTitulo = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint pincelValor = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final RectF area = new RectF();

    private final List<Float> valores = new ArrayList<>();
    private final List<Integer> cores = new ArrayList<>();
    private float soma = 0f;
    private String titulo = "";
    private String textoCentral = "";

    public GraficoPizzaView(Context context) {
        this(context, null);
    }

    public GraficoPizzaView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        pincelFatia.setStyle(Paint.Style.STROKE);

        pincelTitulo.setTextAlign(Paint.Align.CENTER);
        pincelTitulo.setColor(ContextCompat.getColor(context, R.color.texto_secundario));
        pincelTitulo.setTextSize(sp(13));

        pincelValor.setTextAlign(Paint.Align.CENTER);
        pincelValor.setColor(ContextCompat.getColor(context, R.color.texto_principal));
        pincelValor.setFakeBoldText(true);
        pincelValor.setTextSize(sp(18));
        android.graphics.Typeface fonte = androidx.core.content.res.ResourcesCompat.getFont(context, R.font.montserrat);
        if (fonte != null) {
            pincelTitulo.setTypeface(fonte);
            pincelValor.setTypeface(fonte);
        }
    }

    private float sp(float valor) {
        return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, valor, getResources().getDisplayMetrics());
    }

    public void setDados(List<GastoCategoria> gastos, String titulo, String textoCentral) {
        valores.clear();
        cores.clear();
        soma = 0f;
        if (gastos != null) {
            for (GastoCategoria gasto : gastos) {
                float valor = gasto.getTotal() == null ? 0f : gasto.getTotal().floatValue();
                valores.add(valor);
                cores.add(Formatadores.cor(gasto.getCor()));
                soma += valor;
            }
        }
        this.titulo = titulo;
        this.textoCentral = textoCentral;
        invalidate();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        float largura = getWidth() - getPaddingLeft() - getPaddingRight();
        float altura = getHeight() - getPaddingTop() - getPaddingBottom();
        float diametro = Math.min(largura, altura);
        float espessura = diametro * ESPESSURA_RELATIVA;
        float raio = (diametro - espessura) / 2f;
        float centroX = getPaddingLeft() + largura / 2f;
        float centroY = getPaddingTop() + altura / 2f;

        area.set(centroX - raio, centroY - raio, centroX + raio, centroY + raio);
        pincelFatia.setStrokeWidth(espessura);

        if (soma <= 0f) {
            pincelFatia.setColor(ContextCompat.getColor(getContext(), R.color.anel_vazio));
            canvas.drawArc(area, 0, 360, false, pincelFatia);
        } else {
            // Começa no topo do círculo.
            float anguloInicial = -90f;
            boolean variasFatias = valores.size() > 1;
            for (int i = 0; i < valores.size(); i++) {
                float angulo = valores.get(i) / soma * 360f;
                float espaco = variasFatias ? ESPACO_ENTRE_FATIAS : 0f;
                pincelFatia.setColor(cores.get(i));
                canvas.drawArc(area, anguloInicial + espaco / 2f,
                        Math.max(angulo - espaco, 0.5f), false, pincelFatia);
                anguloInicial += angulo;
            }
        }

        canvas.drawText(titulo, centroX, centroY - pincelValor.getTextSize() * 0.6f, pincelTitulo);
        canvas.drawText(textoCentral, centroX, centroY + pincelValor.getTextSize() * 0.6f, pincelValor);
    }
}
