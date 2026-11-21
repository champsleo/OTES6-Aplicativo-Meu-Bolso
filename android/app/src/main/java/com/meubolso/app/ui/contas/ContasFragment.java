package com.meubolso.app.ui.contas;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.snackbar.Snackbar;
import com.meubolso.app.R;
import com.meubolso.app.data.model.Conta;
import com.meubolso.app.data.model.Transferencia;
import com.meubolso.app.data.repository.Resultado;
import com.meubolso.app.databinding.FragmentContasBinding;
import com.meubolso.app.ui.main.MainViewModel;
import com.meubolso.app.util.Formatadores;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;

public class ContasFragment extends Fragment implements ContaAdapter.Acoes {

    private static final String TAG_DIALOGO = "dialogo_contas";

    private FragmentContasBinding binding;
    private ContasViewModel viewModel;
    private MainViewModel mainViewModel;
    private ContaAdapter contaAdapter;
    private TransferenciaAdapter transferenciaAdapter;
    private boolean precisaRecarregar = false;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = FragmentContasBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        viewModel = new ViewModelProvider(this).get(ContasViewModel.class);
        mainViewModel = new ViewModelProvider(requireActivity()).get(MainViewModel.class);

        contaAdapter = new ContaAdapter(this);
        binding.listaContas.setLayoutManager(new LinearLayoutManager(requireContext()));
        binding.listaContas.addItemDecoration(new DividerItemDecoration(requireContext(), DividerItemDecoration.VERTICAL));
        binding.listaContas.setNestedScrollingEnabled(false);
        binding.listaContas.setAdapter(contaAdapter);

        transferenciaAdapter = new TransferenciaAdapter(viewModel::excluirTransferencia);
        binding.listaTransferencias.setLayoutManager(new LinearLayoutManager(requireContext()));
        binding.listaTransferencias.setNestedScrollingEnabled(false);
        binding.listaTransferencias.setAdapter(transferenciaAdapter);

        binding.swipeRefresh.setOnRefreshListener(this::carregar);
        binding.btnNovaConta.setOnClickListener(v -> ContaDialogFragment.novaInstancia(null)
                .show(getChildFragmentManager(), TAG_DIALOGO));
        binding.btnTransferir.setOnClickListener(v -> abrirTransferencia());

        viewModel.getContas().observe(getViewLifecycleOwner(), this::exibirContas);
        viewModel.getTransferencias().observe(getViewLifecycleOwner(), this::exibirTransferencias);
        viewModel.getMensagem().observe(getViewLifecycleOwner(), evento -> {
            String texto = evento.consumir();
            if (texto != null) {
                Snackbar.make(binding.getRoot(), texto, Snackbar.LENGTH_LONG).show();
            }
        });
        viewModel.getTransferenciaExcluida().observe(getViewLifecycleOwner(), evento -> {
            Transferencia excluida = evento.consumir();
            if (excluida != null) {
                Snackbar.make(binding.getRoot(), R.string.transferencia_excluida, Snackbar.LENGTH_LONG)
                        .setAction(R.string.desfazer, v -> viewModel.desfazerExclusao(excluida))
                        .show();
            }
        });
        mainViewModel.getMes().observe(getViewLifecycleOwner(), mes -> {
            binding.tvTituloTransferencias.setText(getString(R.string.transferencias_de, Formatadores.mesAno(mes)));
            carregar();
        });
    }

    private void carregar() {
        viewModel.carregar(mainViewModel.mesAtual());
    }

    private void exibirContas(Resultado<List<Conta>> resultado) {
        binding.swipeRefresh.setRefreshing(resultado.isCarregando());
        if (resultado.isErro()) {
            Snackbar.make(binding.getRoot(), resultado.getMensagem(), Snackbar.LENGTH_LONG)
                    .setAction(R.string.tentar_novamente, v -> carregar())
                    .show();
            return;
        }
        if (!resultado.isSucesso() || resultado.getDados() == null) {
            return;
        }
        List<Conta> contas = resultado.getDados();
        contaAdapter.submitList(contas);

        BigDecimal total = BigDecimal.ZERO;
        for (Conta conta : contas) {
            if (conta.getSaldoAtual() != null) {
                total = total.add(conta.getSaldoAtual());
            }
        }
        binding.tvSaldoTotal.setText(Formatadores.moeda(total));
    }

    private void exibirTransferencias(Resultado<List<Transferencia>> resultado) {
        if (!resultado.isSucesso()) {
            return;
        }
        List<Transferencia> lista = resultado.getDados() != null ? resultado.getDados() : Collections.emptyList();
        transferenciaAdapter.submitList(lista);
        binding.tvSemTransferencias.setVisibility(lista.isEmpty() ? View.VISIBLE : View.GONE);
    }

    private void abrirTransferencia() {
        List<Conta> contas = viewModel.contasCarregadas();
        if (contas == null || contas.size() < 2) {
            Snackbar.make(binding.getRoot(), R.string.erro_duas_contas, Snackbar.LENGTH_LONG).show();
            return;
        }
        new TransferenciaDialogFragment().show(getChildFragmentManager(), TAG_DIALOGO);
    }

    @Override
    public void editar(Conta conta) {
        ContaDialogFragment.novaInstancia(conta).show(getChildFragmentManager(), TAG_DIALOGO);
    }

    @Override
    public void excluir(Conta conta) {
        new MaterialAlertDialogBuilder(requireContext())
                .setTitle(R.string.excluir_conta)
                .setMessage(getString(R.string.confirmar_exclusao_conta, conta.getNome()))
                .setNegativeButton(R.string.cancelar, null)
                .setPositiveButton(R.string.excluir, (dialog, which) -> viewModel.excluir(conta))
                .show();
    }

    @Override
    // Ao voltar de outra tela (ex.: novo lançamento), o saldo pode ter mudado.
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
        binding = null;
    }
}
