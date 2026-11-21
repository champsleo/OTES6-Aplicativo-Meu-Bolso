package com.meubolso.app.ui.resumo;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.meubolso.app.data.model.HistoricoMensal;
import com.meubolso.app.data.model.ResumoMensal;
import com.meubolso.app.data.repository.Resultado;
import com.meubolso.app.data.repository.ResumoRepository;

import java.time.YearMonth;
import java.util.List;

public class ResumoViewModel extends AndroidViewModel {

    public static final int MESES_HISTORICO = 6;

    private final ResumoRepository repository;
    private final MutableLiveData<Resultado<ResumoMensal>> resumo = new MutableLiveData<>();
    private final MutableLiveData<Resultado<List<HistoricoMensal>>> historico = new MutableLiveData<>();
    private int requisicaoAtual = 0;

    public ResumoViewModel(@NonNull Application application) {
        super(application);
        repository = new ResumoRepository(application);
    }

    public LiveData<Resultado<ResumoMensal>> getResumo() {
        return resumo;
    }

    public LiveData<Resultado<List<HistoricoMensal>>> getHistorico() {
        return historico;
    }

    public void carregar(YearMonth mes) {
        final int requisicao = ++requisicaoAtual;
        resumo.setValue(Resultado.carregando());
        repository.buscar(mes, resultado -> {
            if (requisicao == requisicaoAtual) {
                resumo.setValue(resultado);
            }
        });
        repository.historico(MESES_HISTORICO, mes, resultado -> {
            if (requisicao == requisicaoAtual) {
                historico.setValue(resultado);
            }
        });
    }
}
