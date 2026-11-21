package com.meubolso.app.ui.recorrencias;

import android.os.Bundle;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.snackbar.Snackbar;
import com.meubolso.app.R;
import com.meubolso.app.data.model.Recorrencia;
import com.meubolso.app.data.repository.Resultado;
import com.meubolso.app.databinding.ActivityRecorrenciasBinding;
import com.meubolso.app.util.Telas;

import java.util.Collections;
import java.util.List;

public class RecorrenciasActivity extends AppCompatActivity implements RecorrenciaAdapter.Acoes {

    private ActivityRecorrenciasBinding binding;
    private RecorrenciasViewModel viewModel;
    private RecorrenciaAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Telas.ativarTelaCheia(this);
        binding = ActivityRecorrenciasBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        Telas.aplicarEspacamento(binding.getRoot());

        viewModel = new ViewModelProvider(this).get(RecorrenciasViewModel.class);
        binding.toolbar.setNavigationOnClickListener(v -> finish());

        adapter = new RecorrenciaAdapter(this);
        binding.listaRecorrencias.setLayoutManager(new LinearLayoutManager(this));
        binding.listaRecorrencias.addItemDecoration(new DividerItemDecoration(this, DividerItemDecoration.VERTICAL));
        binding.listaRecorrencias.setAdapter(adapter);
        binding.swipeRefresh.setOnRefreshListener(viewModel::carregar);

        viewModel.getRecorrencias().observe(this, this::exibir);
        viewModel.getMensagem().observe(this, evento -> {
            String texto = evento.consumir();
            if (texto != null) {
                Snackbar.make(binding.getRoot(), texto, Snackbar.LENGTH_LONG).show();
            }
        });

        if (savedInstanceState == null) {
            viewModel.carregar();
        }
    }

    private void exibir(Resultado<List<Recorrencia>> resultado) {
        binding.swipeRefresh.setRefreshing(resultado.isCarregando());
        if (resultado.isErro()) {
            Snackbar.make(binding.getRoot(), resultado.getMensagem(), Snackbar.LENGTH_LONG).show();
            return;
        }
        if (!resultado.isSucesso()) {
            return;
        }
        List<Recorrencia> lista = resultado.getDados() != null ? resultado.getDados() : Collections.emptyList();
        adapter.submitList(lista);
        binding.tvVazio.setVisibility(lista.isEmpty() ? View.VISIBLE : View.GONE);
    }

    @Override
    public void alterarStatus(Recorrencia recorrencia, boolean ativa) {
        viewModel.alterarStatus(recorrencia, ativa);
    }

    @Override
    public void excluir(Recorrencia recorrencia) {
        new MaterialAlertDialogBuilder(this)
                .setTitle(R.string.excluir_recorrencia)
                .setMessage(getString(R.string.confirmar_exclusao_recorrencia, recorrencia.getDescricao()))
                .setNegativeButton(R.string.cancelar, null)
                .setPositiveButton(R.string.excluir, (dialog, which) -> viewModel.excluir(recorrencia))
                .show();
    }
}
