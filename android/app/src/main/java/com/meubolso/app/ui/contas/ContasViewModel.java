package com.meubolso.app.ui.contas;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.StringRes;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.meubolso.app.R;
import com.meubolso.app.data.model.Conta;
import com.meubolso.app.data.model.ContaRequest;
import com.meubolso.app.data.model.Transferencia;
import com.meubolso.app.data.model.TransferenciaRequest;
import com.meubolso.app.data.repository.ContaRepository;
import com.meubolso.app.data.repository.Resultado;
import com.meubolso.app.util.Evento;

import java.time.YearMonth;
import java.util.List;

public class ContasViewModel extends AndroidViewModel {

    private final ContaRepository repository;
    private final MutableLiveData<Resultado<List<Conta>>> contas = new MutableLiveData<>();
    private final MutableLiveData<Resultado<List<Transferencia>>> transferencias = new MutableLiveData<>();
    private final MutableLiveData<Evento<String>> mensagem = new MutableLiveData<>();
    private final MutableLiveData<Evento<Transferencia>> transferenciaExcluida = new MutableLiveData<>();

    private YearMonth mes = YearMonth.now();

    public ContasViewModel(@NonNull Application application) {
        super(application);
        repository = new ContaRepository(application);
    }

    public LiveData<Resultado<List<Conta>>> getContas() {
        return contas;
    }

    public LiveData<Resultado<List<Transferencia>>> getTransferencias() {
        return transferencias;
    }

    public LiveData<Evento<String>> getMensagem() {
        return mensagem;
    }

    public LiveData<Evento<Transferencia>> getTransferenciaExcluida() {
        return transferenciaExcluida;
    }

    @Nullable
    public List<Conta> contasCarregadas() {
        Resultado<List<Conta>> atual = contas.getValue();
        return (atual != null && atual.isSucesso()) ? atual.getDados() : null;
    }

    public void carregar(YearMonth mes) {
        this.mes = mes;
        contas.setValue(Resultado.carregando());
        repository.listar(contas::setValue);
        repository.listarTransferencias(mes, transferencias::setValue);
    }

    private void recarregar() {
        carregar(mes);
    }

    public void salvar(@Nullable Long id, ContaRequest request) {
        if (id == null) {
            repository.criar(request, r -> aoConcluir(r, R.string.conta_criada));
        } else {
            repository.atualizar(id, request, r -> aoConcluir(r, R.string.conta_atualizada));
        }
    }

    public void excluir(Conta conta) {
        repository.excluir(conta.getId(), r -> aoConcluir(r, R.string.conta_excluida));
    }

    public void transferir(TransferenciaRequest request) {
        repository.transferir(request, r -> aoConcluir(r, R.string.transferencia_realizada));
    }

    public void excluirTransferencia(Transferencia transferencia) {
        repository.excluirTransferencia(transferencia.getId(), r -> {
            if (r.isSucesso()) {
                transferenciaExcluida.setValue(new Evento<>(transferencia));
                recarregar();
            } else {
                mensagem.setValue(new Evento<>(r.getMensagem()));
            }
        });
    }

    public void desfazerExclusao(Transferencia transferencia) {
        repository.transferir(transferencia.paraRequest(), r -> aoConcluir(r, R.string.exclusao_desfeita));
    }

    private void aoConcluir(Resultado<?> resultado, @StringRes int mensagemSucesso) {
        if (resultado.isSucesso()) {
            mensagem.setValue(new Evento<>(getApplication().getString(mensagemSucesso)));
            recarregar();
        } else if (resultado.isErro()) {
            mensagem.setValue(new Evento<>(resultado.getMensagem()));
        }
    }
}
