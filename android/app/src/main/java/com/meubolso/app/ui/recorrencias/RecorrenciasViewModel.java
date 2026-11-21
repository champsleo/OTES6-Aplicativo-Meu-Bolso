package com.meubolso.app.ui.recorrencias;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.annotation.StringRes;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.meubolso.app.R;
import com.meubolso.app.data.model.Recorrencia;
import com.meubolso.app.data.repository.RecorrenciaRepository;
import com.meubolso.app.data.repository.Resultado;
import com.meubolso.app.util.Evento;

import java.util.List;

public class RecorrenciasViewModel extends AndroidViewModel {

    private final RecorrenciaRepository repository;
    private final MutableLiveData<Resultado<List<Recorrencia>>> recorrencias = new MutableLiveData<>();
    private final MutableLiveData<Evento<String>> mensagem = new MutableLiveData<>();

    public RecorrenciasViewModel(@NonNull Application application) {
        super(application);
        repository = new RecorrenciaRepository(application);
    }

    public LiveData<Resultado<List<Recorrencia>>> getRecorrencias() {
        return recorrencias;
    }

    public LiveData<Evento<String>> getMensagem() {
        return mensagem;
    }

    public void carregar() {
        recorrencias.setValue(Resultado.carregando());
        repository.listar(recorrencias::setValue);
    }

    public void alterarStatus(Recorrencia recorrencia, boolean ativa) {
        repository.alterarStatus(recorrencia.getId(), ativa,
                r -> aoConcluir(r, ativa ? R.string.recorrencia_retomada : R.string.recorrencia_pausada));
    }

    public void excluir(Recorrencia recorrencia) {
        repository.excluir(recorrencia.getId(), r -> aoConcluir(r, R.string.recorrencia_excluida));
    }

    private void aoConcluir(Resultado<?> resultado, @StringRes int mensagemSucesso) {
        if (resultado.isSucesso()) {
            mensagem.setValue(new Evento<>(getApplication().getString(mensagemSucesso)));
        } else if (resultado.isErro()) {
            mensagem.setValue(new Evento<>(resultado.getMensagem()));
        }
        // Recarrega também em caso de erro, para o interruptor voltar ao estado real.
        carregar();
    }
}
