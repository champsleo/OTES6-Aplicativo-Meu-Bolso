package com.meubolso.app.ui.main;

import android.Manifest;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.view.View;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.IdRes;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.snackbar.Snackbar;
import com.meubolso.app.R;
import com.meubolso.app.data.repository.Resultado;
import com.meubolso.app.databinding.ActivityMainBinding;
import com.meubolso.app.ui.auth.LoginActivity;
import com.meubolso.app.ui.categorias.CategoriasFragment;
import com.meubolso.app.ui.contas.ContasFragment;
import com.meubolso.app.ui.inicio.InicioFragment;
import com.meubolso.app.ui.metas.MetasFragment;
import com.meubolso.app.ui.recorrencias.RecorrenciasActivity;
import com.meubolso.app.ui.resumo.ResumoFragment;
import com.meubolso.app.util.Compartilhamento;
import com.meubolso.app.util.Formatadores;
import com.meubolso.app.util.Notificacoes;
import com.meubolso.app.util.SessionManager;
import com.meubolso.app.util.Telas;

public class MainActivity extends AppCompatActivity {

    private ActivityMainBinding binding;
    private SessionManager sessao;
    private MainViewModel viewModel;

    // Android 13+ pede permissão para notificações. Se a pessoa negar, o app funciona normalmente, só sem avisos.
    private final ActivityResultLauncher<String> pedirPermissaoNotificacao =
            registerForActivityResult(new ActivityResultContracts.RequestPermission(), concedida -> {
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        sessao = new SessionManager(this);

        if (!sessao.estaLogado()) {
            startActivity(new Intent(this, LoginActivity.class));
            finish();
            return;
        }

        Telas.ativarTelaCheia(this);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        Telas.aplicarEspacamento(binding.getRoot());

        viewModel = new ViewModelProvider(this).get(MainViewModel.class);
        configurarToolbar();

        binding.bottomNav.setOnItemSelectedListener(item -> {
            exibir(criarFragment(item.getItemId()));
            return true;
        });

        viewModel.getExportacao().observe(this, evento -> {
            Resultado<MainViewModel.ArquivoExportado> resultado = evento.consumir();
            if (resultado != null) {
                exibirExportacao(resultado);
            }
        });

        if (savedInstanceState == null) {
            exibir(new InicioFragment());
            Notificacoes.criarCanais(this);
            pedirPermissaoDeNotificacao();
        }
    }

    private void configurarToolbar() {
        binding.toolbar.setSubtitle(getString(R.string.ola_usuario, primeiroNome(sessao.getNome())));
        binding.toolbar.inflateMenu(R.menu.menu_main);
        binding.toolbar.setOnMenuItemClickListener(item -> {
            int id = item.getItemId();
            if (id == R.id.acaoRecorrentes) {
                startActivity(new Intent(this, RecorrenciasActivity.class));
                return true;
            } else if (id == R.id.acaoExportar) {
                escolherFormatoDeExportacao();
                return true;
            } else if (id == R.id.acaoSair) {
                confirmarSaida();
                return true;
            }
            return false;
        });
    }

    private Fragment criarFragment(@IdRes int itemId) {
        if (itemId == R.id.navResumo) {
            return new ResumoFragment();
        } else if (itemId == R.id.navContas) {
            return new ContasFragment();
        } else if (itemId == R.id.navMetas) {
            return new MetasFragment();
        } else if (itemId == R.id.navCategorias) {
            return new CategoriasFragment();
        }
        return new InicioFragment();
    }

    private void exibir(Fragment fragment) {
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.container, fragment)
                .commit();
    }

    private void pedirPermissaoDeNotificacao() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU && !Notificacoes.temPermissao(this)) {
            pedirPermissaoNotificacao.launch(Manifest.permission.POST_NOTIFICATIONS);
        }
    }

    private void escolherFormatoDeExportacao() {
        String[] opcoes = {getString(R.string.exportar_csv), getString(R.string.exportar_pdf)};
        new MaterialAlertDialogBuilder(this)
                .setTitle(getString(R.string.exportar_titulo, Formatadores.mesAno(viewModel.mesAtual())))
                .setItems(opcoes, (dialog, escolha) -> {
                    if (escolha == 0) {
                        viewModel.exportarCsv();
                    } else {
                        viewModel.exportarPdf();
                    }
                })
                .setNegativeButton(R.string.cancelar, null)
                .show();
    }

    private void exibirExportacao(Resultado<MainViewModel.ArquivoExportado> resultado) {
        binding.progresso.setVisibility(resultado.isCarregando() ? View.VISIBLE : View.GONE);
        if (resultado.isErro()) {
            Snackbar.make(binding.getRoot(), resultado.getMensagem(), Snackbar.LENGTH_LONG).show();
        } else if (resultado.isSucesso() && resultado.getDados() != null) {
            Compartilhamento.compartilhar(this, resultado.getDados().arquivo, resultado.getDados().tipoMime,
                    getString(R.string.compartilhar_relatorio));
        }
    }

    private void confirmarSaida() {
        new MaterialAlertDialogBuilder(this)
                .setTitle(R.string.sair)
                .setMessage(R.string.confirmar_saida)
                .setNegativeButton(R.string.cancelar, null)
                .setPositiveButton(R.string.sair, (dialog, which) -> {
                    sessao.encerrar();
                    Intent intent = new Intent(this, LoginActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);
                    finish();
                })
                .show();
    }

    private String primeiroNome(String nomeCompleto) {
        String nome = nomeCompleto == null ? "" : nomeCompleto.trim();
        int espaco = nome.indexOf(' ');
        return espaco > 0 ? nome.substring(0, espaco) : nome;
    }
}
