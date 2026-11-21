package com.meubolso.app.ui.metas;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.meubolso.app.R;
import com.meubolso.app.data.model.Meta;
import com.meubolso.app.databinding.ItemMetaBinding;
import com.meubolso.app.util.Formatadores;

import java.util.Objects;

public class MetaAdapter extends ListAdapter<Meta, MetaAdapter.ViewHolder> {

    public interface Acoes {
        void editar(Meta meta);

        void movimentar(Meta meta);

        void excluir(Meta meta);
    }

    private static final DiffUtil.ItemCallback<Meta> COMPARADOR = new DiffUtil.ItemCallback<Meta>() {
        @Override
        public boolean areItemsTheSame(@NonNull Meta antiga, @NonNull Meta nova) {
            return Objects.equals(antiga.getId(), nova.getId());
        }

        @Override
        public boolean areContentsTheSame(@NonNull Meta antiga, @NonNull Meta nova) {
            return Objects.equals(antiga.getNome(), nova.getNome())
                    && Objects.equals(antiga.getValorAlvo(), nova.getValorAlvo())
                    && Objects.equals(antiga.getValorGuardado(), nova.getValorGuardado())
                    && Objects.equals(antiga.getPrazo(), nova.getPrazo());
        }
    };

    private final Acoes acoes;

    public MetaAdapter(Acoes acoes) {
        super(COMPARADOR);
        this.acoes = acoes;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ViewHolder(ItemMetaBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.exibir(getItem(position), acoes);
    }

    static class ViewHolder extends RecyclerView.ViewHolder {

        private final ItemMetaBinding binding;

        ViewHolder(ItemMetaBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        void exibir(Meta meta, Acoes acoes) {
            Context context = binding.getRoot().getContext();

            binding.tvNome.setText(meta.getNome());
            binding.tvProgresso.setText(context.getString(R.string.meta_progresso,
                    Formatadores.moeda(meta.getValorGuardado()), Formatadores.moeda(meta.getValorAlvo())));
            binding.tvPercentual.setText(Formatadores.percentual(meta.getPercentual()));
            binding.barraProgresso.setProgressCompat((int) Math.min(100, Math.round(meta.getPercentual())), false);
            binding.barraProgresso.setIndicatorColor(ContextCompat.getColor(context, R.color.receita));

            if (meta.isConcluida()) {
                binding.tvDetalhe.setText(R.string.meta_concluida);
            } else if (meta.getPrazo() != null && meta.getValorMensalSugerido() != null) {
                binding.tvDetalhe.setText(context.getString(R.string.meta_detalhe_prazo,
                        Formatadores.moeda(meta.getValorFaltante()),
                        Formatadores.moeda(meta.getValorMensalSugerido()),
                        Formatadores.mesAnoCurto(meta.getPrazo())));
            } else {
                binding.tvDetalhe.setText(context.getString(R.string.meta_detalhe_sem_prazo,
                        Formatadores.moeda(meta.getValorFaltante())));
            }

            binding.getRoot().setOnClickListener(v -> acoes.editar(meta));
            binding.btnMovimentar.setOnClickListener(v -> acoes.movimentar(meta));
            binding.btnExcluir.setOnClickListener(v -> acoes.excluir(meta));
        }
    }
}
