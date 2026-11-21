package com.meubolso.app.util;

import android.text.Editable;
import android.text.TextWatcher;
import android.widget.EditText;

import java.math.BigDecimal;
import java.text.NumberFormat;

// A pessoa digita só números e o valor se forma da direita para a esquerda, como em app de banco.
public class MascaraMoeda implements TextWatcher {

    private static final int MAXIMO_DIGITOS = 12;

    private final EditText campo;
    private boolean formatando = false;

    private MascaraMoeda(EditText campo) {
        this.campo = campo;
    }

    public static void aplicar(EditText campo) {
        campo.addTextChangedListener(new MascaraMoeda(campo));
    }

    public static void definirValor(EditText campo, BigDecimal valor) {
        campo.setText(valor == null ? "" : formatar(valor.abs()));
    }

    public static BigDecimal valor(CharSequence texto) {
        if (texto == null) {
            return null;
        }
        String digitos = texto.toString().replaceAll("[^0-9]", "");
        if (digitos.isEmpty()) {
            return null;
        }
        return new BigDecimal(digitos).movePointLeft(2);
    }

    private static String formatar(BigDecimal valor) {
        NumberFormat formato = NumberFormat.getNumberInstance(Formatadores.BRASIL);
        formato.setMinimumFractionDigits(2);
        formato.setMaximumFractionDigits(2);
        return formato.format(valor);
    }

    @Override
    public void beforeTextChanged(CharSequence s, int start, int count, int after) {
    }

    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count) {
    }

    @Override
    public void afterTextChanged(Editable texto) {
        // Mudar o texto aqui dispara este método de novo; a flag evita o loop.
        if (formatando) {
            return;
        }
        formatando = true;

        String digitos = texto.toString().replaceAll("[^0-9]", "");
        if (digitos.length() > MAXIMO_DIGITOS) {
            digitos = digitos.substring(0, MAXIMO_DIGITOS);
        }

        String formatado = digitos.isEmpty() ? "" : formatar(new BigDecimal(digitos).movePointLeft(2));
        if (!formatado.equals(texto.toString())) {
            campo.setText(formatado);
        }
        campo.setSelection(campo.getText() == null ? 0 : campo.getText().length());

        formatando = false;
    }
}
