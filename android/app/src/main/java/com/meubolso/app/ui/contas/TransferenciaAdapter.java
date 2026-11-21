package com.meubolso.app.ui.contas;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.meubolso.app.R;
import com.meubolso.app.data.model.Transferencia;
import com.meubolso.app.databinding.ItemTransferenciaBinding;
import com.meubolso.app.util.Formatadores;

import java.util.Objects;

public class TransferenciaAdapter extends ListAdapter<Transferencia, TransferenciaAdapter.ViewHolder> {

    public interface AoExcluir {
        void excluir(Transferencia transferencia);
    }

    private static final DiffUtil.ItemCallback<Transferencia> COMPARADOR =
            new DiffUtil.ItemCallback<Transferencia>() {
                @Override
                public boolean areItemsTheSame(@NonNull Transferencia antiga, @NonNull Transferencia nova) {
                    return Objects.equals(antiga.getId(), nova.getId());
                }

                @Override
                public boolean areContentsTheSame(@NonNull Transferencia antiga, @NonNull Transferencia nova) {
                    return Objects.equals(antiga.getValor(), nova.getValor())
                            && Objects.equals(antiga.getData(), nova.getData())
                            && Objects.equals(antiga.getDescricao(), nova.getDescricao());
                }
            };

    private final AoExcluir aoExcluir;

    public TransferenciaAdapter(AoExcluir aoExcluir) {
        super(COMPARADOR);
        this.aoExcluir = aoExcluir;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ViewHolder(ItemTransferenciaBinding.inflate(
                LayoutInflater.from(parent.getContext()), parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.exibir(getItem(position), aoExcluir);
    }

    static class ViewHolder extends RecyclerView.ViewHolder {

        private final ItemTransferenciaBinding binding;

        ViewHolder(ItemTransferenciaBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        void exibir(Transferencia t, AoExcluir aoExcluir) {
            Context context = binding.getRoot().getContext();
            binding.tvContas.setText(context.getString(R.string.transferencia_de_para,
                    t.getContaOrigemNome(), t.getContaDestinoNome()));
            String data = Formatadores.data(t.getData());
            binding.tvDetalhes.setText(t.getDescricao() == null ? data
                    : context.getString(R.string.descricao_e_data, t.getDescricao(), data));
            binding.tvValor.setText(Formatadores.moeda(t.getValor()));
            binding.btnExcluir.setOnClickListener(v -> aoExcluir.excluir(t));
        }
    }
}
