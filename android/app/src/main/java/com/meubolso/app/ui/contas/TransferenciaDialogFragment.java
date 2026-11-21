package com.meubolso.app.ui.contas;

import android.app.DatePickerDialog;
import android.app.Dialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.widget.ArrayAdapter;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;
import androidx.lifecycle.ViewModelProvider;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.meubolso.app.R;
import com.meubolso.app.data.model.Conta;
import com.meubolso.app.data.model.TransferenciaRequest;
import com.meubolso.app.databinding.DialogTransferenciaBinding;
import com.meubolso.app.util.Formatadores;
import com.meubolso.app.util.MascaraMoeda;
import com.meubolso.app.util.Telas;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class TransferenciaDialogFragment extends DialogFragment {

    private static final String ESTADO_DATA = "data";

    private DialogTransferenciaBinding binding;
    private ContasViewModel viewModel;
    private final List<Conta> contas = new ArrayList<>();
    private Conta origem;
    private Conta destino;
    private LocalDate data = LocalDate.now();

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        viewModel = new ViewModelProvider(requireParentFragment()).get(ContasViewModel.class);
        binding = DialogTransferenciaBinding.inflate(LayoutInflater.from(requireContext()));
        if (savedInstanceState != null) {
            data = LocalDate.parse(savedInstanceState.getString(ESTADO_DATA, data.toString()));
        }

        List<Conta> carregadas = viewModel.contasCarregadas();
        if (carregadas != null) {
            contas.addAll(carregadas);
        }
        binding.dropOrigem.setAdapter(new ArrayAdapter<>(requireContext(), android.R.layout.simple_list_item_1, contas));
        binding.dropDestino.setAdapter(new ArrayAdapter<>(requireContext(), android.R.layout.simple_list_item_1, contas));
        binding.dropOrigem.setOnItemClickListener((lista, v, posicao, id) ->
                origem = (Conta) lista.getItemAtPosition(posicao));
        binding.dropDestino.setOnItemClickListener((lista, v, posicao, id) ->
                destino = (Conta) lista.getItemAtPosition(posicao));

        MascaraMoeda.aplicar(binding.etValor);
        binding.etData.setText(Formatadores.data(data));
        binding.etData.setOnClickListener(v -> new DatePickerDialog(requireContext(), (picker, ano, mes, dia) -> {
            data = LocalDate.of(ano, mes + 1, dia);
            binding.etData.setText(Formatadores.data(data));
        }, data.getYear(), data.getMonthValue() - 1, data.getDayOfMonth()).show());

        AlertDialog dialogo = new MaterialAlertDialogBuilder(requireContext())
                .setTitle(R.string.nova_transferencia)
                .setView(binding.getRoot())
                .setNegativeButton(R.string.cancelar, null)
                .setPositiveButton(R.string.transferir, null)
                .create();
        // O clique é tratado aqui para o diálogo não fechar quando há erro no formulário.
        dialogo.setOnShowListener(d ->
                dialogo.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(v -> transferir()));
        return dialogo;
    }

    private void transferir() {
        binding.campoOrigem.setError(null);
        binding.campoDestino.setError(null);
        binding.campoValor.setError(null);

        if (origem == null) {
            binding.campoOrigem.setError(getString(R.string.erro_selecione_conta));
            return;
        }
        if (destino == null) {
            binding.campoDestino.setError(getString(R.string.erro_selecione_conta));
            return;
        }
        if (origem.getId().equals(destino.getId())) {
            binding.campoDestino.setError(getString(R.string.erro_contas_iguais));
            return;
        }
        BigDecimal valor = MascaraMoeda.valor(binding.etValor.getText());
        if (valor == null || valor.signum() <= 0) {
            binding.campoValor.setError(getString(R.string.erro_valor_invalido));
            return;
        }
        String descricao = Telas.texto(binding.etDescricao);

        viewModel.transferir(new TransferenciaRequest(origem.getId(), destino.getId(), valor, data.toString(),
                descricao.isEmpty() ? null : descricao));
        dismiss();
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString(ESTADO_DATA, data.toString());
    }
}
