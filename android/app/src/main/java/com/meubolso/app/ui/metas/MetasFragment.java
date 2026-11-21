package com.meubolso.app.ui.metas;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.snackbar.Snackbar;
import com.meubolso.app.R;
import com.meubolso.app.data.model.Meta;
import com.meubolso.app.data.repository.Resultado;
import com.meubolso.app.databinding.FragmentMetasBinding;

import java.util.Collections;
import java.util.List;

public class MetasFragment extends Fragment implements MetaAdapter.Acoes {

    private static final String TAG_DIALOGO = "dialogo_meta";

    private FragmentMetasBinding binding;
    private MetasViewModel viewModel;
    private MetaAdapter adapter;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = FragmentMetasBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        viewModel = new ViewModelProvider(this).get(MetasViewModel.class);

        adapter = new MetaAdapter(this);
        binding.listaMetas.setLayoutManager(new LinearLayoutManager(requireContext()));
        binding.listaMetas.setAdapter(adapter);

        binding.swipeRefresh.setOnRefreshListener(viewModel::carregar);
        binding.fabNovaMeta.setOnClickListener(v ->
                MetaDialogFragment.novaInstancia(null).show(getChildFragmentManager(), TAG_DIALOGO));

        viewModel.getMetas().observe(getViewLifecycleOwner(), this::exibir);
        viewModel.getMensagem().observe(getViewLifecycleOwner(), evento -> {
            String texto = evento.consumir();
            if (texto != null) {
                Snackbar.make(binding.getRoot(), texto, Snackbar.LENGTH_LONG).show();
            }
        });

        if (savedInstanceState == null) {
            viewModel.carregar();
        }
    }

    private void exibir(Resultado<List<Meta>> resultado) {
        binding.swipeRefresh.setRefreshing(resultado.isCarregando());
        if (resultado.isErro()) {
            Snackbar.make(binding.getRoot(), resultado.getMensagem(), Snackbar.LENGTH_LONG)
                    .setAction(R.string.tentar_novamente, v -> viewModel.carregar())
                    .show();
            return;
        }
        if (!resultado.isSucesso()) {
            return;
        }
        List<Meta> lista = resultado.getDados() != null ? resultado.getDados() : Collections.emptyList();
        adapter.submitList(lista);
        binding.tvVazio.setVisibility(lista.isEmpty() ? View.VISIBLE : View.GONE);
    }

    @Override
    public void editar(Meta meta) {
        MetaDialogFragment.novaInstancia(meta).show(getChildFragmentManager(), TAG_DIALOGO);
    }

    @Override
    public void movimentar(Meta meta) {
        MovimentoMetaDialogFragment.novaInstancia(meta).show(getChildFragmentManager(), TAG_DIALOGO);
    }

    @Override
    public void excluir(Meta meta) {
        new MaterialAlertDialogBuilder(requireContext())
                .setTitle(R.string.excluir_meta)
                .setMessage(getString(R.string.confirmar_exclusao_meta, meta.getNome()))
                .setNegativeButton(R.string.cancelar, null)
                .setPositiveButton(R.string.excluir, (dialog, which) -> viewModel.excluir(meta))
                .show();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
