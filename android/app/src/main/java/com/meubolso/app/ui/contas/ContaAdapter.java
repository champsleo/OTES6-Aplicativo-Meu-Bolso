package com.meubolso.app.ui.contas;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.DrawableRes;
import androidx.annotation.NonNull;
import androidx.annotation.StringRes;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.meubolso.app.R;
import com.meubolso.app.data.model.Conta;
import com.meubolso.app.data.model.TipoConta;
import com.meubolso.app.databinding.ItemContaBinding;
import com.meubolso.app.util.Formatadores;

import java.util.Objects;

public class ContaAdapter extends ListAdapter<Conta, ContaAdapter.ViewHolder> {

    public interface Acoes {
        void editar(Conta conta);

        void excluir(Conta conta);
    }

    private static final DiffUtil.ItemCallback<Conta> COMPARADOR = new DiffUtil.ItemCallback<Conta>() {
        @Override
        public boolean areItemsTheSame(@NonNull Conta antiga, @NonNull Conta nova) {
            return Objects.equals(antiga.getId(), nova.getId());
        }

        @Override
        public boolean areContentsTheSame(@NonNull Conta antiga, @NonNull Conta nova) {
            return antiga.equals(nova);
        }
    };

    private final Acoes acoes;

    public ContaAdapter(Acoes acoes) {
        super(COMPARADOR);
        this.acoes = acoes;
    }

    @DrawableRes
    public static int icone(TipoConta tipo) {
        if (tipo == TipoConta.BANCO) {
            return R.drawable.ic_banco;
        } else if (tipo == TipoConta.CARTAO_CREDITO) {
            return R.drawable.ic_cartao;
        }
        return R.drawable.ic_carteira;
    }

    @StringRes
    public static int nomeDoTipo(TipoConta tipo) {
        if (tipo == TipoConta.BANCO) {
            return R.string.tipo_banco;
        } else if (tipo == TipoConta.CARTAO_CREDITO) {
            return R.string.tipo_cartao;
        }
        return R.string.tipo_carteira;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ViewHolder(ItemContaBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.exibir(getItem(position), acoes);
    }

    static class ViewHolder extends RecyclerView.ViewHolder {

        private final ItemContaBinding binding;

        ViewHolder(ItemContaBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        void exibir(Conta conta, Acoes acoes) {
            Context context = binding.getRoot().getContext();
            boolean negativo = conta.getSaldoAtual() != null && conta.getSaldoAtual().signum() < 0;

            binding.ivIcone.setImageResource(icone(conta.getTipo()));
            binding.tvNome.setText(conta.getNome());
            binding.tvTipo.setText(nomeDoTipo(conta.getTipo()));
            binding.tvSaldo.setText(Formatadores.moeda(conta.getSaldoAtual()));
            binding.tvSaldo.setTextColor(ContextCompat.getColor(context, negativo ? R.color.despesa : R.color.texto_principal));

            binding.getRoot().setOnClickListener(v -> acoes.editar(conta));
            binding.btnExcluir.setOnClickListener(v -> acoes.excluir(conta));
        }
    }
}
