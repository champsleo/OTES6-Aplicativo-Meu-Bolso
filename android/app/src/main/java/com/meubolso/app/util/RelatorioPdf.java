package com.meubolso.app.util;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.graphics.pdf.PdfDocument;

import androidx.core.content.res.ResourcesCompat;

import com.meubolso.app.R;
import com.meubolso.app.data.model.GastoCategoria;
import com.meubolso.app.data.model.ResumoMensal;
import com.meubolso.app.data.model.TipoTransacao;
import com.meubolso.app.data.model.Transacao;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.time.YearMonth;
import java.util.List;

// O PdfDocument já vem no Android: cada página é um Canvas onde desenhamos o texto.
public final class RelatorioPdf {

    // A4 em pontos (1 ponto = 1/72 de polegada).
    private static final int LARGURA = 595;
    private static final int ALTURA = 842;
    private static final int MARGEM = 40;
    private static final int LINHA = 18;

    private static final int VINHO = Color.rgb(127, 37, 37);
    private static final int VERDE = Color.rgb(46, 125, 50);
    private static final int VERMELHO = Color.rgb(198, 40, 40);
    private static final int CINZA = Color.rgb(102, 115, 107);

    private final PdfDocument documento = new PdfDocument();
    private final Paint titulo = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint subtitulo = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint texto = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint direita = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint linha = new Paint();

    private PdfDocument.Page pagina;
    private Canvas canvas;
    private int numeroPagina = 0;
    private float y;

    private RelatorioPdf() {
        titulo.setTextSize(20);
        titulo.setTypeface(Typeface.DEFAULT_BOLD);
        titulo.setColor(VINHO);
        subtitulo.setTextSize(13);
        subtitulo.setTypeface(Typeface.DEFAULT_BOLD);
        texto.setTextSize(10.5f);
        direita.setTextSize(10.5f);
        direita.setTextAlign(Paint.Align.RIGHT);
        linha.setColor(Color.LTGRAY);
        linha.setStrokeWidth(0.5f);
    }

    public static File gerar(Context context, YearMonth mes, ResumoMensal resumo,
                             List<Transacao> transacoes) throws IOException {
        RelatorioPdf relatorio = new RelatorioPdf();
        relatorio.usarFonte(context);
        relatorio.escrever(mes, resumo, transacoes);
        File arquivo = Compartilhamento.novoArquivo(context, "meu-bolso-" + mes + ".pdf");
        try (FileOutputStream saida = new FileOutputStream(arquivo)) {
            relatorio.documento.writeTo(saida);
        } finally {
            relatorio.documento.close();
        }
        return arquivo;
    }

    private void usarFonte(Context context) {
        Typeface normal = ResourcesCompat.getFont(context, R.font.montserrat_regular);
        Typeface negrito = ResourcesCompat.getFont(context, R.font.montserrat_bold);
        if (normal != null) {
            texto.setTypeface(normal);
            direita.setTypeface(normal);
        }
        if (negrito != null) {
            titulo.setTypeface(negrito);
            subtitulo.setTypeface(negrito);
        }
    }

    private void escrever(YearMonth mes, ResumoMensal resumo, List<Transacao> transacoes) {
        novaPagina();
        canvas.drawText("Meu Bolso — Relatório de " + Formatadores.mesAno(mes), MARGEM, y, titulo);
        y += LINHA * 2;

        secao("Resumo do mês");
        valor("Receitas", Formatadores.moeda(resumo.getTotalReceitas()), VERDE);
        valor("Despesas", Formatadores.moeda(resumo.getTotalDespesas()), VERMELHO);
        boolean negativo = resumo.getSaldo() != null && resumo.getSaldo().signum() < 0;
        valor("Saldo", Formatadores.moeda(resumo.getSaldo()), negativo ? VERMELHO : VERDE);
        valor("Quantidade de lançamentos", String.valueOf(resumo.getQuantidadeLancamentos()), Color.BLACK);
        y += LINHA;

        List<GastoCategoria> gastos = resumo.getDespesasPorCategoria();
        if (gastos != null && !gastos.isEmpty()) {
            secao("Despesas por categoria");
            for (GastoCategoria gasto : gastos) {
                String detalhe = Formatadores.moeda(gasto.getTotal())
                        + "  (" + Formatadores.percentual(gasto.getPercentualDoTotal()) + ")";
                if (gasto.isLimiteExcedido()) {
                    detalhe += "  — limite excedido";
                }
                valor(gasto.getNome(), detalhe, gasto.isLimiteExcedido() ? VERMELHO : Color.BLACK);
            }
            y += LINHA;
        }

        secao("Lançamentos");
        if (transacoes.isEmpty()) {
            texto.setColor(CINZA);
            canvas.drawText("Nenhum lançamento neste mês.", MARGEM, y, texto);
            return;
        }
        for (Transacao t : transacoes) {
            garantirEspaco();
            boolean receita = t.getTipo() == TipoTransacao.RECEITA;
            texto.setColor(Color.BLACK);
            canvas.drawText(Formatadores.data(t.getData()), MARGEM, y, texto);
            canvas.drawText(cortar(t.getDescricao(), 32), MARGEM + 70, y, texto);
            texto.setColor(CINZA);
            canvas.drawText(cortar(t.getCategoriaNome() + " • " + t.getContaNome(), 34), MARGEM + 255, y, texto);
            direita.setColor(receita ? VERDE : VERMELHO);
            canvas.drawText((receita ? "+ " : "− ") + Formatadores.moeda(t.getValor()),
                    LARGURA - MARGEM, y, direita);
            y += LINHA;
        }
        documento.finishPage(pagina);
        pagina = null;
    }

    private void secao(String nome) {
        garantirEspaco();
        canvas.drawText(nome, MARGEM, y, subtitulo);
        y += 6;
        canvas.drawLine(MARGEM, y, LARGURA - MARGEM, y, linha);
        y += LINHA;
    }

    private void valor(String rotulo, String valor, int cor) {
        garantirEspaco();
        texto.setColor(Color.BLACK);
        canvas.drawText(rotulo, MARGEM, y, texto);
        direita.setColor(cor);
        canvas.drawText(valor, LARGURA - MARGEM, y, direita);
        y += LINHA;
    }

    private void garantirEspaco() {
        if (y > ALTURA - MARGEM - LINHA) {
            documento.finishPage(pagina);
            novaPagina();
        }
    }

    private void novaPagina() {
        numeroPagina++;
        PdfDocument.PageInfo info = new PdfDocument.PageInfo.Builder(LARGURA, ALTURA, numeroPagina).create();
        pagina = documento.startPage(info);
        canvas = pagina.getCanvas();
        y = MARGEM + 10;
        texto.setColor(CINZA);
        canvas.drawText("Página " + numeroPagina, LARGURA - MARGEM - 45, ALTURA - 20, texto);
    }

    private static String cortar(String texto, int maximo) {
        if (texto == null) {
            return "";
        }
        return texto.length() <= maximo ? texto : texto.substring(0, maximo - 1) + "…";
    }
}
