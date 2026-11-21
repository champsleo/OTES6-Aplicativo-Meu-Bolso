package com.meubolso.app.ui.categorias;

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

import com.google.android.material.snackbar.Snackbar;
import com.meubolso.app.R;
import com.meubolso.app.data.model.Categoria;
import com.meubolso.app.data.repository.Resultado;
import com.meubolso.app.databinding.FragmentCategoriasBinding;

import java.util.List;

public class CategoriasFragment extends Fragment implements CategoriaAdapter.Acoes {

    private static final String TAG_DIALOGO = "dialogo_categoria";

    private FragmentCategoriasBinding binding;
    private CategoriasViewModel viewModel;
    private CategoriaAdapter adapter;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = FragmentCategoriasBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        viewModel = new ViewModelProvider(this).get(CategoriasViewModel.class);

        adapter = new CategoriaAdapter(this);
        binding.listaCategorias.setLayoutManager(new LinearLayoutManager(requireContext()));
        binding.listaCategorias.addItemDecoration(
                new DividerItemDecoration(requireContext(), DividerItemDecoration.VERTICAL));
        binding.listaCategorias.setAdapter(adapter);

        binding.swipeRefresh.setOnRefreshListener(viewModel::carregar);
        binding.fabNova.setOnClickListener(v -> abrirDialogo(null));

        viewModel.getCategorias().observe(getViewLifecycleOwner(), this::exibir);
        viewModel.getMensagem().observe(getViewLifecycleOwner(), evento -> {
            String texto = evento.consumir();
            if (texto != null) {
                Snackbar.make(binding.getRoot(), texto, Snackbar.LENGTH_LONG).show();
            }
        });

        viewModel.getExcluida().observe(getViewLifecycleOwner(), evento -> {
            Categoria excluida = evento.consumir();
            if (excluida != null) {
                Snackbar.make(binding.getRoot(), getString(R.string.categoria_excluida_nome, excluida.getNome()),
                                Snackbar.LENGTH_LONG)
                        .setAction(R.string.desfazer, v -> viewModel.desfazerExclusao(excluida))
                        .show();
            }
        });

        if (savedInstanceState == null) {
            viewModel.carregar();
        }
    }

    private void exibir(Resultado<List<Categoria>> resultado) {
        binding.swipeRefresh.setRefreshing(resultado.isCarregando());
        if (resultado.isSucesso()) {
            adapter.submitList(resultado.getDados());
        } else if (resultado.isErro()) {
            Snackbar.make(binding.getRoot(), resultado.getMensagem(), Snackbar.LENGTH_LONG)
                    .setAction(R.string.tentar_novamente, v -> viewModel.carregar())
                    .show();
        }
    }

    private void abrirDialogo(@Nullable Categoria categoria) {
        CategoriaDialogFragment.novaInstancia(categoria).show(getChildFragmentManager(), TAG_DIALOGO);
    }

    @Override
    public void editar(Categoria categoria) {
        abrirDialogo(categoria);
    }

    @Override
    public void excluir(Categoria categoria) {
        viewModel.excluir(categoria);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
