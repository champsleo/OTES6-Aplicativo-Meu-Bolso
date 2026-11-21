package com.meubolso.app.util;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;

import androidx.core.content.FileProvider;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

public final class Compartilhamento {

    private static final String PASTA = "exportacoes";

    private Compartilhamento() {
    }

    public static File novoArquivo(Context context, String nome) {
        File pasta = new File(context.getCacheDir(), PASTA);
        if (!pasta.exists() && !pasta.mkdirs()) {
            throw new IllegalStateException("Não foi possível criar a pasta de exportação");
        }
        return new File(pasta, nome);
    }

    public static File salvar(Context context, String nome, byte[] conteudo) throws IOException {
        File arquivo = novoArquivo(context, nome);
        try (FileOutputStream saida = new FileOutputStream(arquivo)) {
            saida.write(conteudo);
        }
        return arquivo;
    }

    // O FileProvider dá acesso temporário ao arquivo, sem expor as pastas do app.
    public static void compartilhar(Context context, File arquivo, String tipoMime, String titulo) {
        Uri uri = FileProvider.getUriForFile(context, context.getPackageName() + ".arquivos", arquivo);
        Intent envio = new Intent(Intent.ACTION_SEND);
        envio.setType(tipoMime);
        envio.putExtra(Intent.EXTRA_STREAM, uri);
        envio.putExtra(Intent.EXTRA_SUBJECT, titulo);
        envio.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        context.startActivity(Intent.createChooser(envio, titulo));
    }
}
