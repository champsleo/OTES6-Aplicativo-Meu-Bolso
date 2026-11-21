package com.meubolso.app.ui.metas;

import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;
import androidx.lifecycle.ViewModelProvider;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.meubolso.app.R;
import com.meubolso.app.data.model.Meta;
import com.meubolso.app.databinding.DialogMovimentoMetaBinding;
import com.meubolso.app.util.Formatadores;
import com.meubolso.app.util.MascaraMoeda;

import java.math.BigDecimal;

public class MovimentoMetaDialogFragment extends DialogFragment {

    private static final String ARG_META = "meta";

    private DialogMovimentoMetaBinding binding;
    private Meta meta;

    public static MovimentoMetaDialogFragment novaInstancia(Meta meta) {
        Bundle argumentos = new Bundle();
        argumentos.putSerializable(ARG_META, meta);
        MovimentoMetaDialogFragment dialogo = new MovimentoMetaDialogFragment();
        dialogo.setArguments(argumentos);
        return dialogo;
    }

    @SuppressWarnings("deprecation")
    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        meta = (Meta) requireArguments().getSerializable(ARG_META);
        binding = DialogMovimentoMetaBinding.inflate(LayoutInflater.from(requireContext()));
        MascaraMoeda.aplicar(binding.etValor);
        binding.tvGuardado.setText(getString(R.string.meta_progresso,
                Formatadores.moeda(meta.getValorGuardado()), Formatadores.moeda(meta.getValorAlvo())));

        AlertDialog dialogo = new MaterialAlertDialogBuilder(requireContext())
                .setTitle(meta.getNome())
                .setView(binding.getRoot())
                .setNegativeButton(R.string.cancelar, null)
                .setNeutralButton(R.string.retirar, null)
                .setPositiveButton(R.string.guardar, null)
                .create();
        dialogo.setOnShowListener(d -> {
            dialogo.getButton(DialogInterface.BUTTON_POSITIVE).setOnClickListener(v -> concluir(true));
            dialogo.getButton(DialogInterface.BUTTON_NEUTRAL).setOnClickListener(v -> concluir(false));
        });
        return dialogo;
    }

    private void concluir(boolean guardar) {
        binding.campoValor.setError(null);
        BigDecimal valor = MascaraMoeda.valor(binding.etValor.getText());
        if (valor == null || valor.signum() <= 0) {
            binding.campoValor.setError(getString(R.string.erro_valor_invalido));
            return;
        }
        MetasViewModel viewModel = new ViewModelProvider(requireParentFragment()).get(MetasViewModel.class);
        if (guardar) {
            viewModel.guardar(meta, valor);
        } else {
            viewModel.retirar(meta, valor);
        }
        dismiss();
    }
}
