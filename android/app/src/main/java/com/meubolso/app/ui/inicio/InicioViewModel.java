package com.meubolso.app.ui.inicio;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.meubolso.app.R;
import com.meubolso.app.data.model.ResumoMensal;
import com.meubolso.app.data.model.TipoTransacao;
import com.meubolso.app.data.model.Transacao;
import com.meubolso.app.data.repository.Resultado;
import com.meubolso.app.data.repository.ResumoRepository;
import com.meubolso.app.data.repository.TransacaoRepository;
import com.meubolso.app.util.Evento;

import java.time.YearMonth;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class InicioViewModel extends AndroidViewModel {

    private final TransacaoRepository transacaoRepository;
    private final ResumoRepository resumoRepository;

    private final MutableLiveData<Resultado<List<Transacao>>> transacoes = new MutableLiveData<>();
    private final MutableLiveData<Resultado<ResumoMensal>> resumo = new MutableLiveData<>();
    private final MutableLiveData<Evento<Transacao>> excluida = new MutableLiveData<>();
    private final MutableLiveData<Evento<String>> mensagem = new MutableLiveData<>();

    private TipoTransacao filtro = null;
    private String busca = null;
    private YearMonth mesAtual = YearMonth.now();
    // Ignora respostas antigas quando a pessoa troca de mês rápido.
    private int requisicaoAtual = 0;

    public InicioViewModel(@NonNull Application application) {
        super(application);
        transacaoRepository = new TransacaoRepository(application);
        resumoRepository = new ResumoRepository(application);
    }

    public LiveData<Resultado<List<Transacao>>> getTransacoes() {
        return transacoes;
    }

    public LiveData<Resultado<ResumoMensal>> getResumo() {
        return resumo;
    }

    public LiveData<Evento<Transacao>> getExcluida() {
        return excluida;
    }

    public LiveData<Evento<String>> getMensagem() {
        return mensagem;
    }

    public void setFiltro(TipoTransacao filtro) {
        this.filtro = filtro;
    }

    public void setBusca(String busca) {
        this.busca = busca;
    }

    public boolean temBusca() {
        return busca != null && !busca.trim().isEmpty();
    }

    public void carregar(YearMonth mes) {
        this.mesAtual = mes;
        final int requisicao = ++requisicaoAtual;
        transacoes.setValue(Resultado.carregando());

        transacaoRepository.listar(mes, filtro, busca, resultado -> {
            if (requisicao == requisicaoAtual) {
                transacoes.setValue(resultado);
            }
        });
        carregarResumo(requisicao);
    }

    private void carregarResumo(int requisicao) {
        resumoRepository.buscar(mesAtual, resultado -> {
            if (requisicao == requisicaoAtual) {
                resumo.setValue(resultado);
            }
        });
    }

    // Tira o item da lista na hora, sem esperar o servidor, e oferece "Desfazer".
    public void excluir(Transacao transacao) {
        List<Transacao> semOItem = new ArrayList<>();
        for (Transacao t : listaAtual()) {
            if (!Objects.equals(t.getId(), transacao.getId())) {
                semOItem.add(t);
            }
        }
        transacoes.setValue(Resultado.sucesso(semOItem));

        transacaoRepository.excluir(transacao.getId(), resultado -> {
            if (resultado.isSucesso()) {
                excluida.setValue(new Evento<>(transacao));
                carregarResumo(requisicaoAtual);
            } else {
                mensagem.setValue(new Evento<>(resultado.getMensagem()));
                carregar(mesAtual);
            }
        });
    }

    // "Desfazer" recria o lançamento com os mesmos dados.
    public void desfazerExclusao(Transacao transacao) {
        transacaoRepository.criar(transacao.paraRequest(), resultado -> {
            if (resultado.isSucesso()) {
                mensagem.setValue(new Evento<>(getApplication().getString(R.string.exclusao_desfeita)));
            } else {
                mensagem.setValue(new Evento<>(resultado.getMensagem()));
            }
            carregar(mesAtual);
        });
    }

    private List<Transacao> listaAtual() {
        Resultado<List<Transacao>> atual = transacoes.getValue();
        if (atual != null && atual.isSucesso() && atual.getDados() != null) {
            return atual.getDados();
        }
        return new ArrayList<>();
    }
}
