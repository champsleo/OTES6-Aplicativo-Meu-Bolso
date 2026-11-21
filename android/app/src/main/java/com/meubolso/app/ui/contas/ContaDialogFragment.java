package com.meubolso.app.ui.contas;

import android.app.Dialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;
import androidx.lifecycle.ViewModelProvider;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.meubolso.app.R;
import com.meubolso.app.data.model.Conta;
import com.meubolso.app.data.model.ContaRequest;
import com.meubolso.app.data.model.TipoConta;
import com.meubolso.app.databinding.DialogContaBinding;
import com.meubolso.app.util.Formatadores;
import com.meubolso.app.util.MascaraMoeda;
import com.meubolso.app.util.Telas;

import java.math.BigDecimal;

public class ContaDialogFragment extends DialogFragment {

    private static final String ARG_CONTA = "conta";

    private DialogContaBinding binding;
    private Conta conta;

    public static ContaDialogFragment novaInstancia(@Nullable Conta conta) {
        Bundle argumentos = new Bundle();
        argumentos.putSerializable(ARG_CONTA, conta);
        ContaDialogFragment dialogo = new ContaDialogFragment();
        dialogo.setArguments(argumentos);
        return dialogo;
    }

    @SuppressWarnings("deprecation")
    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        conta = (Conta) requireArguments().getSerializable(ARG_CONTA);
        binding = DialogContaBinding.inflate(LayoutInflater.from(requireContext()));
        MascaraMoeda.aplicar(binding.etSaldo);

        if (conta != null && savedInstanceState == null) {
            binding.etNome.setText(conta.getNome());
            binding.grupoTipo.check(botaoDoTipo(conta.getTipo()));
            MascaraMoeda.definirValor(binding.etSaldo, conta.getSaldoInicial());
        }
        if (conta != null) {
            // Ao editar, o tipo fica fixo para os botões não parecerem "trocar de conta".
            binding.grupoTipo.setVisibility(View.GONE);
            binding.tvTipo.setText(getString(R.string.tipo_desta_conta) + ": "
                    + getString(ContaAdapter.nomeDoTipo(conta.getTipo())));
            binding.tvTipo.setTextColor(androidx.core.content.ContextCompat.getColor(requireContext(),
                    R.color.texto_principal));

            binding.tvSaldoAtual.setVisibility(View.VISIBLE);
            binding.tvSaldoAtual.setText(getString(R.string.saldo_atual_conta,
                    Formatadores.moeda(conta.getSaldoAtual())));
        }
        atualizarRotuloSaldo();
        binding.grupoTipo.addOnButtonCheckedListener((grupo, id, marcado) -> {
            if (marcado) {
                atualizarRotuloSaldo();
            }
        });

        AlertDialog dialogo = new MaterialAlertDialogBuilder(requireContext())
                .setTitle(conta == null ? R.string.nova_conta : R.string.editar_conta)
                .setView(binding.getRoot())
                .setNegativeButton(R.string.cancelar, null)
                .setPositiveButton(R.string.salvar, null)
                .create();
        // O clique é tratado aqui para o diálogo não fechar quando há erro no formulário.
        dialogo.setOnShowListener(d ->
                dialogo.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(v -> salvar()));
        return dialogo;
    }

    private int botaoDoTipo(TipoConta tipo) {
        if (tipo == TipoConta.BANCO) {
            return R.id.btnBanco;
        } else if (tipo == TipoConta.CARTAO_CREDITO) {
            return R.id.btnCartao;
        }
        return R.id.btnCarteira;
    }

    private TipoConta tipoSelecionado() {
        int id = binding.grupoTipo.getCheckedButtonId();
        if (id == R.id.btnBanco) {
            return TipoConta.BANCO;
        } else if (id == R.id.btnCartao) {
            return TipoConta.CARTAO_CREDITO;
        }
        return TipoConta.CARTEIRA;
    }

    private void atualizarRotuloSaldo() {
        boolean cartao = tipoSelecionado() == TipoConta.CARTAO_CREDITO;
        binding.campoSaldo.setHint(cartao ? R.string.fatura_em_aberto : R.string.saldo_inicial);
        int ajuda = cartao ? R.string.fatura_ajuda
                : (conta != null ? R.string.saldo_inicial_ajuda_edicao : R.string.saldo_inicial_ajuda);
        binding.campoSaldo.setHelperText(getString(ajuda));
    }

    private void salvar() {
        binding.campoNome.setError(null);
        String nome = Telas.texto(binding.etNome);
        if (nome.isEmpty()) {
            binding.campoNome.setError(getString(R.string.erro_informe_nome_conta));
            return;
        }
        TipoConta tipo = tipoSelecionado();
        BigDecimal valor = MascaraMoeda.valor(binding.etSaldo.getText());
        if (valor == null) {
            valor = BigDecimal.ZERO;
        }
        // No cartão, a fatura em aberto é dívida: entra como saldo negativo.
        BigDecimal saldoInicial = (tipo == TipoConta.CARTAO_CREDITO) ? valor.negate() : valor;

        new ViewModelProvider(requireParentFragment()).get(ContasViewModel.class)
                .salvar(conta == null ? null : conta.getId(), new ContaRequest(nome, tipo, saldoInicial));
        dismiss();
    }
}
