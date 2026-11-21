package com.meubolso.app.ui.transacao;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.StringRes;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.meubolso.app.R;
import com.meubolso.app.data.model.Categoria;
import com.meubolso.app.data.model.Conta;
import com.meubolso.app.data.model.RecorrenciaRequest;
import com.meubolso.app.data.model.Transacao;
import com.meubolso.app.data.model.TransacaoRequest;
import com.meubolso.app.data.repository.CategoriaRepository;
import com.meubolso.app.data.repository.ContaRepository;
import com.meubolso.app.data.repository.RecorrenciaRepository;
import com.meubolso.app.data.repository.Resultado;
import com.meubolso.app.data.repository.ResultadoCallback;
import com.meubolso.app.data.repository.TransacaoRepository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

// Guarda as escolhas do formulário para não perdê-las quando a tela gira.
public class TransacaoFormViewModel extends AndroidViewModel {

    public enum Acao { SALVO, RECORRENCIA_CRIADA, EXCLUIDO }

    public static class Concluido {
        public final Acao acao;
        public final Transacao transacao;

        Concluido(Acao acao, Transacao transacao) {
            this.acao = acao;
            this.transacao = transacao;
        }
    }

    private final CategoriaRepository categoriaRepository;
    private final ContaRepository contaRepository;
    private final TransacaoRepository transacaoRepository;
    private final RecorrenciaRepository recorrenciaRepository;

    private final MutableLiveData<Resultado<List<Categoria>>> categorias = new MutableLiveData<>();
    private final MutableLiveData<Resultado<List<Conta>>> contas = new MutableLiveData<>();
    private final MutableLiveData<Resultado<Transacao>> transacao = new MutableLiveData<>();
    private final MutableLiveData<Resultado<Concluido>> operacao = new MutableLiveData<>();

    private LocalDate dataSelecionada = LocalDate.now();
    private Categoria categoriaSelecionada;
    private Conta contaSelecionada;
    private boolean iniciado = false;
    private boolean formularioPreenchido = false;

    public TransacaoFormViewModel(@NonNull Application application) {
        super(application);
        categoriaRepository = new CategoriaRepository(application);
        contaRepository = new ContaRepository(application);
        transacaoRepository = new TransacaoRepository(application);
        recorrenciaRepository = new RecorrenciaRepository(application);
    }

    public LiveData<Resultado<List<Categoria>>> getCategorias() {
        return categorias;
    }

    public LiveData<Resultado<List<Conta>>> getContas() {
        return contas;
    }

    public LiveData<Resultado<Transacao>> getTransacao() {
        return transacao;
    }

    public LiveData<Resultado<Concluido>> getOperacao() {
        return operacao;
    }

    public void iniciar(@Nullable Long transacaoId) {
        if (iniciado) {
            return;
        }
        iniciado = true;
        categorias.setValue(Resultado.carregando());
        categoriaRepository.listar(null, categorias::setValue);
        contaRepository.listar(contas::setValue);

        if (transacaoId != null) {
            transacao.setValue(Resultado.carregando());
            transacaoRepository.buscar(transacaoId, transacao::setValue);
        }
    }

    public LocalDate getDataSelecionada() {
        return dataSelecionada;
    }

    public void setDataSelecionada(LocalDate data) {
        this.dataSelecionada = data;
    }

    public Categoria getCategoriaSelecionada() {
        return categoriaSelecionada;
    }

    public void setCategoriaSelecionada(@Nullable Categoria categoria) {
        this.categoriaSelecionada = categoria;
    }

    public Conta getContaSelecionada() {
        return contaSelecionada;
    }

    public void setContaSelecionada(@Nullable Conta conta) {
        this.contaSelecionada = conta;
    }

    public boolean isFormularioPreenchido() {
        return formularioPreenchido;
    }

    public void marcarFormularioPreenchido() {
        this.formularioPreenchido = true;
    }

    public void salvar(@Nullable Long transacaoId, String descricao, BigDecimal valor, boolean repetirTodoMes) {
        if (descricao.isEmpty()) {
            erro(R.string.erro_informe_descricao);
            return;
        }
        if (valor == null || valor.signum() <= 0) {
            erro(R.string.erro_valor_invalido);
            return;
        }
        if (categoriaSelecionada == null) {
            erro(R.string.erro_selecione_categoria);
            return;
        }
        if (contaSelecionada == null) {
            erro(R.string.erro_selecione_conta);
            return;
        }

        operacao.setValue(Resultado.carregando());

        if (repetirTodoMes && transacaoId == null) {
            RecorrenciaRequest request = new RecorrenciaRequest(descricao, valor, dataSelecionada.toString(),
                    categoriaSelecionada.getId(), contaSelecionada.getId());
            recorrenciaRepository.criar(request, resultado -> operacao.setValue(resultado.isSucesso()
                    ? Resultado.sucesso(new Concluido(Acao.RECORRENCIA_CRIADA, null))
                    : Resultado.erro(resultado.getMensagem())));
            return;
        }

        TransacaoRequest request = new TransacaoRequest(descricao, valor, dataSelecionada.toString(),
                categoriaSelecionada.getId(), contaSelecionada.getId());
        ResultadoCallback<Transacao> aoSalvar = resultado -> operacao.setValue(resultado.isSucesso()
                ? Resultado.sucesso(new Concluido(Acao.SALVO, resultado.getDados()))
                : Resultado.erro(resultado.getMensagem()));

        if (transacaoId == null) {
            transacaoRepository.criar(request, aoSalvar);
        } else {
            transacaoRepository.atualizar(transacaoId, request, aoSalvar);
        }
    }

    // Sem confirmação: a tela anterior oferece "Desfazer".
    public void excluir(long transacaoId) {
        Resultado<Transacao> carregada = transacao.getValue();
        Transacao original = (carregada != null) ? carregada.getDados() : null;
        operacao.setValue(Resultado.carregando());
        transacaoRepository.excluir(transacaoId, resultado -> operacao.setValue(resultado.isSucesso()
                ? Resultado.sucesso(new Concluido(Acao.EXCLUIDO, original))
                : Resultado.erro(resultado.getMensagem())));
    }

    private void erro(@StringRes int mensagem) {
        operacao.setValue(Resultado.erro(getApplication().getString(mensagem)));
    }
}
