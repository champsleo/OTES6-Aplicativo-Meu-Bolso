package com.meubolso.app.ui.metas;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.StringRes;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.meubolso.app.R;
import com.meubolso.app.data.model.Meta;
import com.meubolso.app.data.model.MetaRequest;
import com.meubolso.app.data.repository.MetaRepository;
import com.meubolso.app.data.repository.Resultado;
import com.meubolso.app.util.Evento;

import java.math.BigDecimal;
import java.util.List;

public class MetasViewModel extends AndroidViewModel {

    private final MetaRepository repository;
    private final MutableLiveData<Resultado<List<Meta>>> metas = new MutableLiveData<>();
    private final MutableLiveData<Evento<String>> mensagem = new MutableLiveData<>();

    public MetasViewModel(@NonNull Application application) {
        super(application);
        repository = new MetaRepository(application);
    }

    public LiveData<Resultado<List<Meta>>> getMetas() {
        return metas;
    }

    public LiveData<Evento<String>> getMensagem() {
        return mensagem;
    }

    public void carregar() {
        metas.setValue(Resultado.carregando());
        repository.listar(metas::setValue);
    }

    public void salvar(@Nullable Long id, MetaRequest request) {
        if (id == null) {
            repository.criar(request, r -> aoConcluir(r, R.string.meta_criada));
        } else {
            repository.atualizar(id, request, r -> aoConcluir(r, R.string.meta_atualizada));
        }
    }

    public void guardar(Meta meta, BigDecimal valor) {
        repository.movimentar(meta.getId(), valor, r -> {
            // Comemora só no momento em que a meta é atingida.
            if (r.isSucesso() && r.getDados() != null && r.getDados().isConcluida() && !meta.isConcluida()) {
                aoConcluir(r, R.string.meta_concluida_parabens);
            } else {
                aoConcluir(r, R.string.valor_guardado);
            }
        });
    }

    public void retirar(Meta meta, BigDecimal valor) {
        repository.movimentar(meta.getId(), valor.negate(), r -> aoConcluir(r, R.string.valor_retirado));
    }

    public void excluir(Meta meta) {
        repository.excluir(meta.getId(), r -> aoConcluir(r, R.string.meta_excluida));
    }

    private void aoConcluir(Resultado<?> resultado, @StringRes int mensagemSucesso) {
        if (resultado.isSucesso()) {
            mensagem.setValue(new Evento<>(getApplication().getString(mensagemSucesso)));
            carregar();
        } else if (resultado.isErro()) {
            mensagem.setValue(new Evento<>(resultado.getMensagem()));
        }
    }
}
