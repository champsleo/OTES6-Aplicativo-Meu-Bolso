package com.meubolso.app.ui.metas;

import android.app.DatePickerDialog;
import android.app.Dialog;
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
import com.meubolso.app.data.model.MetaRequest;
import com.meubolso.app.databinding.DialogMetaBinding;
import com.meubolso.app.util.Formatadores;
import com.meubolso.app.util.MascaraMoeda;
import com.meubolso.app.util.Telas;

import java.math.BigDecimal;
import java.time.LocalDate;

public class MetaDialogFragment extends DialogFragment {

    private static final String ARG_META = "meta";
    private static final String ESTADO_PRAZO = "prazo";

    private DialogMetaBinding binding;
    private Meta meta;
    private LocalDate prazo;

    public static MetaDialogFragment novaInstancia(@Nullable Meta meta) {
        Bundle argumentos = new Bundle();
        argumentos.putSerializable(ARG_META, meta);
        MetaDialogFragment dialogo = new MetaDialogFragment();
        dialogo.setArguments(argumentos);
        return dialogo;
    }

    @SuppressWarnings("deprecation")
    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        meta = (Meta) requireArguments().getSerializable(ARG_META);
        binding = DialogMetaBinding.inflate(LayoutInflater.from(requireContext()));
        MascaraMoeda.aplicar(binding.etValorAlvo);

        if (savedInstanceState != null) {
            String salvo = savedInstanceState.getString(ESTADO_PRAZO);
            prazo = salvo == null ? null : LocalDate.parse(salvo);
        } else if (meta != null) {
            binding.etNome.setText(meta.getNome());
            MascaraMoeda.definirValor(binding.etValorAlvo, meta.getValorAlvo());
            prazo = meta.getPrazo() == null ? null : LocalDate.parse(meta.getPrazo());
        }
        atualizarCampoPrazo();

        binding.etPrazo.setOnClickListener(v -> abrirCalendario());
        // O "x" no fim do campo remove o prazo.
        binding.campoPrazo.setEndIconOnClickListener(v -> {
            prazo = null;
            atualizarCampoPrazo();
        });

        AlertDialog dialogo = new MaterialAlertDialogBuilder(requireContext())
                .setTitle(meta == null ? R.string.nova_meta : R.string.editar_meta)
                .setView(binding.getRoot())
                .setNegativeButton(R.string.cancelar, null)
                .setPositiveButton(R.string.salvar, null)
                .create();
        // O clique é tratado aqui para o diálogo não fechar quando há erro no formulário.
        dialogo.setOnShowListener(d ->
                dialogo.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(v -> salvar()));
        return dialogo;
    }

    private void abrirCalendario() {
        LocalDate inicial = prazo != null ? prazo : LocalDate.now().plusMonths(6);
        new DatePickerDialog(requireContext(), (picker, ano, mes, dia) -> {
            prazo = LocalDate.of(ano, mes + 1, dia);
            atualizarCampoPrazo();
        }, inicial.getYear(), inicial.getMonthValue() - 1, inicial.getDayOfMonth()).show();
    }

    private void atualizarCampoPrazo() {
        binding.etPrazo.setText(prazo == null ? "" : Formatadores.data(prazo));
    }

    private void salvar() {
        binding.campoNome.setError(null);
        binding.campoValor.setError(null);

        String nome = Telas.texto(binding.etNome);
        if (nome.isEmpty()) {
            binding.campoNome.setError(getString(R.string.erro_informe_nome_meta));
            return;
        }
        BigDecimal valor = MascaraMoeda.valor(binding.etValorAlvo.getText());
        if (valor == null || valor.signum() <= 0) {
            binding.campoValor.setError(getString(R.string.erro_valor_invalido));
            return;
        }

        new ViewModelProvider(requireParentFragment()).get(MetasViewModel.class)
                .salvar(meta == null ? null : meta.getId(),
                        new MetaRequest(nome, valor, prazo == null ? null : prazo.toString()));
        dismiss();
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString(ESTADO_PRAZO, prazo == null ? null : prazo.toString());
    }
}
