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
import com.meubolso.app.data.model.HistoricoMensal;
import com.meubolso.app.util.Formatadores;

import java.math.BigDecimal;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.List;

// Receitas e despesas lado a lado por mês, desenhadas com Canvas. A altura é proporcional ao maior valor do período.
public class GraficoBarrasView extends View {

    private final Paint pincelReceita = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint pincelDespesa = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint pincelEixo = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint pincelRotulo = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint pincelRotuloDestaque = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final RectF barra = new RectF();

    private final List<HistoricoMensal> meses = new ArrayList<>();
    private YearMonth mesDestacado;

    public GraficoBarrasView(Context context) {
        this(context, null);
    }

    public GraficoBarrasView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        pincelReceita.setColor(ContextCompat.getColor(context, R.color.receita));
        pincelDespesa.setColor(ContextCompat.getColor(context, R.color.despesa));
        pincelEixo.setColor(ContextCompat.getColor(context, R.color.anel_vazio));
        pincelEixo.setStrokeWidth(dp(1));

        pincelRotulo.setColor(ContextCompat.getColor(context, R.color.texto_secundario));
        pincelRotulo.setTextSize(sp(12));
        pincelRotulo.setTextAlign(Paint.Align.CENTER);

        android.graphics.Typeface fonte = androidx.core.content.res.ResourcesCompat.getFont(context, R.font.montserrat);
        if (fonte != null) {
            pincelRotulo.setTypeface(fonte);
        }
        pincelRotuloDestaque.set(pincelRotulo);
        pincelRotuloDestaque.setColor(ContextCompat.getColor(context, R.color.texto_principal));
        pincelRotuloDestaque.setFakeBoldText(true);
    }

    public void setDados(List<HistoricoMensal> historico, YearMonth mesDestacado) {
        meses.clear();
        if (historico != null) {
            meses.addAll(historico);
        }
        this.mesDestacado = mesDestacado;
        invalidate();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (meses.isEmpty()) {
            return;
        }

        float esquerda = getPaddingLeft();
        float direita = getWidth() - getPaddingRight();
        float alturaRotulo = sp(12) + dp(8);
        float base = getHeight() - getPaddingBottom() - alturaRotulo;
        float topo = getPaddingTop();
        float alturaUtil = base - topo;

        float maior = 0f;
        for (HistoricoMensal mes : meses) {
            maior = Math.max(maior, valor(mes.getTotalReceitas()));
            maior = Math.max(maior, valor(mes.getTotalDespesas()));
        }

        float larguraGrupo = (direita - esquerda) / meses.size();
        float larguraBarra = Math.min(dp(16), larguraGrupo * 0.3f);
        float raio = dp(3);

        canvas.drawLine(esquerda, base, direita, base, pincelEixo);

        for (int i = 0; i < meses.size(); i++) {
            HistoricoMensal mes = meses.get(i);
            float centro = esquerda + larguraGrupo * i + larguraGrupo / 2f;

            float alturaReceita = maior <= 0 ? 0 : valor(mes.getTotalReceitas()) / maior * alturaUtil;
            float alturaDespesa = maior <= 0 ? 0 : valor(mes.getTotalDespesas()) / maior * alturaUtil;

            barra.set(centro - larguraBarra - dp(1), base - alturaReceita, centro - dp(1), base);
            canvas.drawRoundRect(barra, raio, raio, pincelReceita);
            barra.set(centro + dp(1), base - alturaDespesa, centro + larguraBarra + dp(1), base);
            canvas.drawRoundRect(barra, raio, raio, pincelDespesa);

            YearMonth ym = YearMonth.parse(mes.getMes());
            boolean destaque = ym.equals(mesDestacado);
            canvas.drawText(Formatadores.mesCurto(ym), centro, getHeight() - getPaddingBottom() - dp(2),
                    destaque ? pincelRotuloDestaque : pincelRotulo);
        }
    }

    private static float valor(BigDecimal numero) {
        return numero == null ? 0f : numero.floatValue();
    }

    private float dp(float valor) {
        return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, valor, getResources().getDisplayMetrics());
    }

    private float sp(float valor) {
        return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, valor, getResources().getDisplayMetrics());
    }
}
