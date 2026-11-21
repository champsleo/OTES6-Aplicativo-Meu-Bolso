package com.meubolso.app.ui.main;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.meubolso.app.R;
import com.meubolso.app.data.model.ResumoMensal;
import com.meubolso.app.data.model.Transacao;
import com.meubolso.app.data.repository.ExportacaoRepository;
import com.meubolso.app.data.repository.Resultado;
import com.meubolso.app.data.repository.ResumoRepository;
import com.meubolso.app.data.repository.TransacaoRepository;
import com.meubolso.app.util.Compartilhamento;
import com.meubolso.app.util.Evento;
import com.meubolso.app.util.RelatorioPdf;

import java.io.File;
import java.io.IOException;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import okhttp3.ResponseBody;

public class MainViewModel extends AndroidViewModel {

    public static class ArquivoExportado {
        public final File arquivo;
        public final String tipoMime;

        ArquivoExportado(File arquivo, String tipoMime) {
            this.arquivo = arquivo;
            this.tipoMime = tipoMime;
        }
    }

    private final MutableLiveData<YearMonth> mes = new MutableLiveData<>(YearMonth.now());
    private final MutableLiveData<Evento<Resultado<ArquivoExportado>>> exportacao = new MutableLiveData<>();

    private final ExportacaoRepository exportacaoRepository;
    private final ResumoRepository resumoRepository;
    private final TransacaoRepository transacaoRepository;

    public MainViewModel(@NonNull Application application) {
        super(application);
        exportacaoRepository = new ExportacaoRepository(application);
        resumoRepository = new ResumoRepository(application);
        transacaoRepository = new TransacaoRepository(application);
    }

    public LiveData<YearMonth> getMes() {
        return mes;
    }

    public LiveData<Evento<Resultado<ArquivoExportado>>> getExportacao() {
        return exportacao;
    }

    public YearMonth mesAtual() {
        YearMonth valor = mes.getValue();
        return valor != null ? valor : YearMonth.now();
    }

    public void mesAnterior() {
        mes.setValue(mesAtual().minusMonths(1));
    }

    public void proximoMes() {
        mes.setValue(mesAtual().plusMonths(1));
    }

    // O CSV vem pronto do servidor; o PDF é montado no próprio celular.
    public void exportarCsv() {
        YearMonth mesExportado = mesAtual();
        exportacao.setValue(new Evento<>(Resultado.carregando()));
        exportacaoRepository.baixarCsv(mesExportado, resultado -> {
            if (!resultado.isSucesso() || resultado.getDados() == null) {
                falhar(resultado.getMensagem());
                return;
            }
            try (ResponseBody corpo = resultado.getDados()) {
                File arquivo = Compartilhamento.salvar(getApplication(),
                        "meu-bolso-" + mesExportado + ".csv", corpo.bytes());
                exportacao.setValue(new Evento<>(Resultado.sucesso(new ArquivoExportado(arquivo, "text/csv"))));
            } catch (IOException e) {
                falhar(getApplication().getString(R.string.erro_exportar));
            }
        });
    }

    public void exportarPdf() {
        YearMonth mesExportado = mesAtual();
        exportacao.setValue(new Evento<>(Resultado.carregando()));
        resumoRepository.buscar(mesExportado, resumo -> {
            if (!resumo.isSucesso() || resumo.getDados() == null) {
                falhar(resumo.getMensagem());
                return;
            }
            transacaoRepository.listar(mesExportado, null, null, lista -> {
                if (!lista.isSucesso()) {
                    falhar(lista.getMensagem());
                    return;
                }
                gerarPdf(mesExportado, resumo.getDados(),
                        lista.getDados() != null ? lista.getDados() : Collections.emptyList());
            });
        });
    }

    private void gerarPdf(YearMonth mesExportado, ResumoMensal resumo, List<Transacao> transacoes) {
        List<Transacao> emOrdem = new ArrayList<>(transacoes);
        // A API devolve do mais recente para o mais antigo; no relatório fica melhor em ordem de data.
        Collections.reverse(emOrdem);
        try {
            File arquivo = RelatorioPdf.gerar(getApplication(), mesExportado, resumo, emOrdem);
            exportacao.setValue(new Evento<>(Resultado.sucesso(new ArquivoExportado(arquivo, "application/pdf"))));
        } catch (IOException e) {
            falhar(getApplication().getString(R.string.erro_exportar));
        }
    }

    private void falhar(String mensagem) {
        exportacao.setValue(new Evento<>(Resultado.erro(mensagem)));
    }
}
