package com.meubolso.app.ui.recorrencias;

import android.content.Context;
import android.content.res.ColorStateList;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.meubolso.app.R;
import com.meubolso.app.data.model.Recorrencia;
import com.meubolso.app.data.model.TipoTransacao;
import com.meubolso.app.databinding.ItemRecorrenciaBinding;
import com.meubolso.app.util.Formatadores;

import java.util.Objects;

public class RecorrenciaAdapter extends ListAdapter<Recorrencia, RecorrenciaAdapter.ViewHolder> {

    public interface Acoes {
        void alterarStatus(Recorrencia recorrencia, boolean ativa);

        void excluir(Recorrencia recorrencia);
    }

    private static final DiffUtil.ItemCallback<Recorrencia> COMPARADOR = new DiffUtil.ItemCallback<Recorrencia>() {
        @Override
        public boolean areItemsTheSame(@NonNull Recorrencia antiga, @NonNull Recorrencia nova) {
            return Objects.equals(antiga.getId(), nova.getId());
        }

        @Override
        public boolean areContentsTheSame(@NonNull Recorrencia antiga, @NonNull Recorrencia nova) {
            return antiga.isAtiva() == nova.isAtiva()
                    && Objects.equals(antiga.getProximaData(), nova.getProximaData())
                    && Objects.equals(antiga.getValor(), nova.getValor());
        }
    };

    private final Acoes acoes;

    public RecorrenciaAdapter(Acoes acoes) {
        super(COMPARADOR);
        this.acoes = acoes;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ViewHolder(ItemRecorrenciaBinding.inflate(
                LayoutInflater.from(parent.getContext()), parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.exibir(getItem(position), acoes);
    }

    static class ViewHolder extends RecyclerView.ViewHolder {

        private final ItemRecorrenciaBinding binding;

        ViewHolder(ItemRecorrenciaBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        void exibir(Recorrencia r, Acoes acoes) {
            Context context = binding.getRoot().getContext();
            boolean receita = r.getTipo() == TipoTransacao.RECEITA;

            binding.indicadorCor.setBackgroundTintList(ColorStateList.valueOf(Formatadores.cor(r.getCategoriaCor())));
            binding.tvDescricao.setText(r.getDescricao());
            binding.tvValor.setText(Formatadores.moeda(r.getValor()));
            binding.tvValor.setTextColor(ContextCompat.getColor(context, receita ? R.color.receita : R.color.despesa));
            binding.tvDetalhes.setText(context.getString(R.string.recorrencia_detalhes,
                    r.getDiaDoMes(), r.getCategoriaNome(), r.getContaNome()));
            binding.tvProxima.setText(r.isAtiva() && r.getProximaData() != null
                    ? context.getString(R.string.recorrencia_proxima, Formatadores.data(r.getProximaData()))
                    : context.getString(R.string.recorrencia_pausada_rotulo));

            // Tira o listener antes de mudar o estado; senão a própria atualização dispararia a ação.
            binding.switchAtiva.setOnCheckedChangeListener(null);
            binding.switchAtiva.setChecked(r.isAtiva());
            binding.switchAtiva.setOnCheckedChangeListener((botao, marcado) -> acoes.alterarStatus(r, marcado));

            binding.btnExcluir.setOnClickListener(v -> acoes.excluir(r));
        }
    }
}
