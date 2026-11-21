package com.meubolso.app.ui.inicio;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.snackbar.Snackbar;
import com.meubolso.app.R;
import com.meubolso.app.data.model.ResumoMensal;
import com.meubolso.app.data.model.TipoTransacao;
import com.meubolso.app.data.model.Transacao;
import com.meubolso.app.data.repository.Resultado;
import com.meubolso.app.databinding.FragmentInicioBinding;
import com.meubolso.app.ui.main.MainViewModel;
import com.meubolso.app.ui.transacao.TransacaoFormActivity;
import com.meubolso.app.util.Formatadores;

import java.util.Collections;
import java.util.List;

public class InicioFragment extends Fragment {

    // Espera a pessoa parar de digitar antes de buscar, para não chamar a API a cada letra.
    private static final long ATRASO_BUSCA_MS = 400;

    private FragmentInicioBinding binding;
    private InicioViewModel viewModel;
    private MainViewModel mainViewModel;
    private TransacaoAdapter adapter;
    private boolean precisaRecarregar = false;
    private final Handler handler = new Handler(Looper.getMainLooper());
    private final Runnable buscaPendente = this::carregar;

    // O formulário devolve o lançamento excluído para oferecermos "Desfazer" aqui.
    private final ActivityResultLauncher<Intent> abrirFormulario = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(), resultado -> {
                if (resultado.getResultCode() == Activity.RESULT_OK && resultado.getData() != null) {
                    Transacao excluida = TransacaoFormActivity.lerExcluida(resultado.getData());
                    if (excluida != null && binding != null) {
                        mostrarDesfazer(excluida);
                    }
                }
            });

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = FragmentInicioBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        viewModel = new ViewModelProvider(this).get(InicioViewModel.class);
        mainViewModel = new ViewModelProvider(requireActivity()).get(MainViewModel.class);

        configurarLista();
        configurarAcoes();

        viewModel.getTransacoes().observe(getViewLifecycleOwner(), this::exibirTransacoes);
        viewModel.getResumo().observe(getViewLifecycleOwner(), this::exibirResumo);
        viewModel.getExcluida().observe(getViewLifecycleOwner(), evento -> {
            Transacao excluida = evento.consumir();
            if (excluida != null) {
                mostrarDesfazer(excluida);
            }
        });
        viewModel.getMensagem().observe(getViewLifecycleOwner(), evento -> {
            String texto = evento.consumir();
            if (texto != null) {
                Snackbar.make(binding.getRoot(), texto, Snackbar.LENGTH_LONG).show();
            }
        });
        mainViewModel.getMes().observe(getViewLifecycleOwner(), mes -> {
            binding.seletorMes.tvMes.setText(Formatadores.mesAno(mes));
            carregar();
        });
    }

    private void configurarLista() {
        adapter = new TransacaoAdapter(transacao -> abrirFormulario(transacao.getId()));
        binding.listaTransacoes.setLayoutManager(new LinearLayoutManager(requireContext()));
        binding.listaTransacoes.addItemDecoration(
                new DividerItemDecoration(requireContext(), DividerItemDecoration.VERTICAL));
        binding.listaTransacoes.setAdapter(adapter);

        new ItemTouchHelper(new ItemTouchHelper.SimpleCallback(0,
                ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT) {
            @Override
            public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder origem,
                                  @NonNull RecyclerView.ViewHolder destino) {
                return false;
            }

            @Override
            public void onSwiped(@NonNull RecyclerView.ViewHolder holder, int direcao) {
                int posicao = holder.getBindingAdapterPosition();
                if (posicao != RecyclerView.NO_POSITION) {
                    viewModel.excluir(adapter.getCurrentList().get(posicao));
                }
            }
        }).attachToRecyclerView(binding.listaTransacoes);
    }

    private void configurarAcoes() {
        binding.seletorMes.btnMesAnterior.setOnClickListener(v -> mainViewModel.mesAnterior());
        binding.seletorMes.btnProximoMes.setOnClickListener(v -> mainViewModel.proximoMes());
        binding.swipeRefresh.setOnRefreshListener(this::carregar);
        binding.fabNovo.setOnClickListener(v -> abrirFormulario(null));
        binding.chipsFiltro.setOnCheckedStateChangeListener((grupo, selecionados) -> {
            viewModel.setFiltro(filtroSelecionado());
            carregar();
        });
        binding.etBusca.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable texto) {
                viewModel.setBusca(texto.toString());
                handler.removeCallbacks(buscaPendente);
                handler.postDelayed(buscaPendente, ATRASO_BUSCA_MS);
            }
        });
    }

    private TipoTransacao filtroSelecionado() {
        int selecionado = binding.chipsFiltro.getCheckedChipId();
        if (selecionado == R.id.chipReceitas) {
            return TipoTransacao.RECEITA;
        } else if (selecionado == R.id.chipDespesas) {
            return TipoTransacao.DESPESA;
        }
        return null;
    }

    private void carregar() {
        viewModel.carregar(mainViewModel.mesAtual());
    }

    private void exibirTransacoes(Resultado<List<Transacao>> resultado) {
        binding.swipeRefresh.setRefreshing(resultado.isCarregando());

        if (resultado.isSucesso()) {
            List<Transacao> lista = resultado.getDados() != null
                    ? resultado.getDados() : Collections.emptyList();
            adapter.submitList(lista);
            binding.tvVazio.setText(viewModel.temBusca() ? R.string.busca_sem_resultado : R.string.nenhum_lancamento);
            binding.tvVazio.setVisibility(lista.isEmpty() ? View.VISIBLE : View.GONE);
        } else if (resultado.isErro()) {
            Snackbar.make(binding.getRoot(), resultado.getMensagem(), Snackbar.LENGTH_LONG)
                    .setAction(R.string.tentar_novamente, v -> carregar())
                    .show();
        }
    }

    private void exibirResumo(Resultado<ResumoMensal> resultado) {
        if (!resultado.isSucesso() || resultado.getDados() == null) {
            return;
        }
        ResumoMensal resumo = resultado.getDados();
        binding.tvSaldo.setText(Formatadores.moeda(resumo.getSaldo()));
        binding.tvReceitas.setText(Formatadores.moeda(resumo.getTotalReceitas()));
        binding.tvDespesas.setText(Formatadores.moeda(resumo.getTotalDespesas()));
    }

    private void mostrarDesfazer(Transacao excluida) {
        Snackbar.make(binding.getRoot(), getString(R.string.lancamento_excluido_nome, excluida.getDescricao()),
                        Snackbar.LENGTH_LONG)
                .setAction(R.string.desfazer, v -> viewModel.desfazerExclusao(excluida))
                .show();
    }

    private void abrirFormulario(@Nullable Long transacaoId) {
        Intent intent = new Intent(requireContext(), TransacaoFormActivity.class);
        if (transacaoId != null) {
            intent.putExtra(TransacaoFormActivity.EXTRA_ID, transacaoId.longValue());
        }
        abrirFormulario.launch(intent);
    }

    @Override
    // Ao voltar de outra tela, recarrega para mostrar o que mudou.
    public void onResume() {
        super.onResume();
        if (precisaRecarregar) {
            precisaRecarregar = false;
            carregar();
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        precisaRecarregar = true;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        handler.removeCallbacks(buscaPendente);
        binding = null;
    }
}
