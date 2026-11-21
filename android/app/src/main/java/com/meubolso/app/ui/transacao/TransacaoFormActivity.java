package com.meubolso.app.ui.transacao;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.google.android.material.snackbar.Snackbar;
import com.meubolso.app.R;
import com.meubolso.app.data.model.Categoria;
import com.meubolso.app.data.model.Conta;
import com.meubolso.app.data.model.TipoTransacao;
import com.meubolso.app.data.model.Transacao;
import com.meubolso.app.data.repository.Resultado;
import com.meubolso.app.databinding.ActivityTransacaoFormBinding;
import com.meubolso.app.util.Formatadores;
import com.meubolso.app.util.MascaraMoeda;
import com.meubolso.app.util.Notificacoes;
import com.meubolso.app.util.Telas;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class TransacaoFormActivity extends AppCompatActivity {

    public static final String EXTRA_ID = "transacao_id";
    private static final String EXTRA_EXCLUIDA = "transacao_excluida";

    private ActivityTransacaoFormBinding binding;
    private TransacaoFormViewModel viewModel;
    private Long transacaoId;
    private final List<Categoria> todasCategorias = new ArrayList<>();
    private final List<Conta> todasContas = new ArrayList<>();
    // Ao editar, o lançamento pode chegar antes das listas; guardamos o id até elas chegarem.
    private Long categoriaPendenteId;
    private Long contaPendenteId;

    @SuppressWarnings("deprecation")
    @Nullable
    public static Transacao lerExcluida(Intent dados) {
        Object excluida = dados.getSerializableExtra(EXTRA_EXCLUIDA);
        return (excluida instanceof Transacao) ? (Transacao) excluida : null;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Telas.ativarTelaCheia(this);
        binding = ActivityTransacaoFormBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        Telas.aplicarEspacamento(binding.getRoot());

        viewModel = new ViewModelProvider(this).get(TransacaoFormViewModel.class);
        transacaoId = getIntent().hasExtra(EXTRA_ID) ? getIntent().getLongExtra(EXTRA_ID, 0) : null;

        configurarToolbar();
        configurarCampos();

        viewModel.getCategorias().observe(this, this::exibirCategorias);
        viewModel.getContas().observe(this, this::exibirContas);
        viewModel.getTransacao().observe(this, this::exibirTransacao);
        viewModel.getOperacao().observe(this, this::exibirOperacao);

        viewModel.iniciar(transacaoId);
    }

    private void configurarToolbar() {
        binding.toolbar.setTitle(transacaoId == null ? R.string.novo_lancamento : R.string.editar_lancamento);
        binding.toolbar.setNavigationOnClickListener(v -> finish());

        if (transacaoId != null) {
            binding.toolbar.inflateMenu(R.menu.menu_transacao_form);
            binding.toolbar.setOnMenuItemClickListener(item -> {
                if (item.getItemId() == R.id.acaoExcluir) {
                    viewModel.excluir(transacaoId);
                    return true;
                }
                return false;
            });
        }
    }

    private void configurarCampos() {
        MascaraMoeda.aplicar(binding.etValor);
        atualizarCampoData();
        binding.etData.setOnClickListener(v -> abrirCalendario());

        // "Repetir todo mês" só ao criar; editar muda apenas este lançamento.
        binding.switchRepetir.setVisibility(transacaoId == null ? View.VISIBLE : View.GONE);

        binding.grupoTipo.addOnButtonCheckedListener((grupo, botaoId, marcado) -> {
            if (marcado) {
                // Trocou entre receita e despesa: a categoria escolhida deixa de valer.
                Categoria selecionada = viewModel.getCategoriaSelecionada();
                if (selecionada != null && selecionada.getTipo() != tipoSelecionado()) {
                    viewModel.setCategoriaSelecionada(null);
                }
                atualizarListaDeCategorias();
            }
        });

        binding.dropCategoria.setOnItemClickListener((lista, view, posicao, id) ->
                viewModel.setCategoriaSelecionada((Categoria) lista.getItemAtPosition(posicao)));
        binding.dropConta.setOnItemClickListener((lista, view, posicao, id) ->
                viewModel.setContaSelecionada((Conta) lista.getItemAtPosition(posicao)));

        binding.btnSalvar.setOnClickListener(v -> viewModel.salvar(
                transacaoId,
                Telas.texto(binding.etDescricao),
                MascaraMoeda.valor(binding.etValor.getText()),
                binding.switchRepetir.isChecked()));
    }

    private TipoTransacao tipoSelecionado() {
        return binding.grupoTipo.getCheckedButtonId() == R.id.btnReceita
                ? TipoTransacao.RECEITA : TipoTransacao.DESPESA;
    }

    private void exibirCategorias(Resultado<List<Categoria>> resultado) {
        if (resultado.isErro()) {
            Snackbar.make(binding.getRoot(), resultado.getMensagem(), Snackbar.LENGTH_LONG).show();
            return;
        }
        if (!resultado.isSucesso() || resultado.getDados() == null) {
            return;
        }
        todasCategorias.clear();
        todasCategorias.addAll(resultado.getDados());
        atualizarListaDeCategorias();

        if (categoriaPendenteId != null) {
            selecionarCategoria(categoriaPendenteId);
        }
    }

    private void atualizarListaDeCategorias() {
        TipoTransacao tipo = tipoSelecionado();
        List<Categoria> filtradas = new ArrayList<>();
        for (Categoria categoria : todasCategorias) {
            if (categoria.getTipo() == tipo) {
                filtradas.add(categoria);
            }
        }
        binding.dropCategoria.setAdapter(
                new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, filtradas));

        Categoria selecionada = viewModel.getCategoriaSelecionada();
        boolean combina = selecionada != null && selecionada.getTipo() == tipo;
        binding.dropCategoria.setText(combina ? selecionada.getNome() : "", false);
    }

    private void selecionarCategoria(Long categoriaId) {
        if (todasCategorias.isEmpty()) {
            categoriaPendenteId = categoriaId;
            return;
        }
        categoriaPendenteId = null;
        for (Categoria categoria : todasCategorias) {
            if (Objects.equals(categoria.getId(), categoriaId)) {
                viewModel.setCategoriaSelecionada(categoria);
                binding.dropCategoria.setText(categoria.getNome(), false);
                return;
            }
        }
    }

    private void exibirContas(Resultado<List<Conta>> resultado) {
        if (!resultado.isSucesso() || resultado.getDados() == null) {
            return;
        }
        todasContas.clear();
        todasContas.addAll(resultado.getDados());
        binding.dropConta.setAdapter(
                new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, todasContas));

        if (contaPendenteId != null) {
            selecionarConta(contaPendenteId);
        } else if (viewModel.getContaSelecionada() != null) {
            binding.dropConta.setText(viewModel.getContaSelecionada().getNome(), false);
        } else if (transacaoId == null && !todasContas.isEmpty()) {
            viewModel.setContaSelecionada(todasContas.get(0));
            binding.dropConta.setText(todasContas.get(0).getNome(), false);
        }
    }

    private void selecionarConta(Long contaId) {
        if (todasContas.isEmpty()) {
            contaPendenteId = contaId;
            return;
        }
        contaPendenteId = null;
        for (Conta conta : todasContas) {
            if (Objects.equals(conta.getId(), contaId)) {
                viewModel.setContaSelecionada(conta);
                binding.dropConta.setText(conta.getNome(), false);
                return;
            }
        }
    }

    private void exibirTransacao(Resultado<Transacao> resultado) {
        binding.progresso.setVisibility(resultado.isCarregando() ? View.VISIBLE : View.GONE);

        if (resultado.isErro()) {
            Toast.makeText(this, resultado.getMensagem(), Toast.LENGTH_LONG).show();
            finish();
            return;
        }
        if (!resultado.isSucesso() || resultado.getDados() == null || viewModel.isFormularioPreenchido()) {
            return;
        }

        Transacao transacao = resultado.getDados();
        binding.grupoTipo.check(transacao.getTipo() == TipoTransacao.RECEITA ? R.id.btnReceita : R.id.btnDespesa);
        binding.etDescricao.setText(transacao.getDescricao());
        MascaraMoeda.definirValor(binding.etValor, transacao.getValor());
        viewModel.setDataSelecionada(LocalDate.parse(transacao.getData()));
        atualizarCampoData();
        selecionarCategoria(transacao.getCategoriaId());
        selecionarConta(transacao.getContaId());
        viewModel.marcarFormularioPreenchido();
    }

    private void abrirCalendario() {
        LocalDate atual = viewModel.getDataSelecionada();
        new DatePickerDialog(this, (picker, ano, mes, dia) -> {
            // No DatePicker o mês começa em 0.
            viewModel.setDataSelecionada(LocalDate.of(ano, mes + 1, dia));
            atualizarCampoData();
        }, atual.getYear(), atual.getMonthValue() - 1, atual.getDayOfMonth()).show();
    }

    private void atualizarCampoData() {
        binding.etData.setText(Formatadores.data(viewModel.getDataSelecionada()));
    }

    private void exibirOperacao(Resultado<TransacaoFormViewModel.Concluido> resultado) {
        boolean carregando = resultado.isCarregando();
        binding.progresso.setVisibility(carregando ? View.VISIBLE : View.GONE);
        binding.btnSalvar.setEnabled(!carregando);

        if (resultado.isErro()) {
            Snackbar.make(binding.getRoot(), resultado.getMensagem(), Snackbar.LENGTH_LONG).show();
            return;
        }
        if (!resultado.isSucesso() || resultado.getDados() == null) {
            return;
        }

        TransacaoFormViewModel.Concluido concluido = resultado.getDados();
        switch (concluido.acao) {
            case EXCLUIDO:
                Intent dados = new Intent();
                dados.putExtra(EXTRA_EXCLUIDA, concluido.transacao);
                setResult(RESULT_OK, dados);
                break;
            case RECORRENCIA_CRIADA:
                Toast.makeText(this, R.string.recorrencia_criada, Toast.LENGTH_LONG).show();
                break;
            default:
                Toast.makeText(this, R.string.lancamento_salvo, Toast.LENGTH_SHORT).show();
                if (concluido.transacao != null) {
                    // O servidor avisa quando a categoria chega a 80% do limite.
                    Notificacoes.alertaDeLimite(this, concluido.transacao.getAlertaLimite());
                }
                break;
        }
        finish();
    }
}
