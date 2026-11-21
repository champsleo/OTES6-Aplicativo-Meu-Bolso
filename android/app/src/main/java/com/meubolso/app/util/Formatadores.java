package com.meubolso.app.util;

import android.graphics.Color;

import java.math.BigDecimal;
import java.text.NumberFormat;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

public final class Formatadores {

    public static final Locale BRASIL = Locale.forLanguageTag("pt-BR");

    private static final DateTimeFormatter DATA_BR = DateTimeFormatter.ofPattern("dd/MM/yyyy", BRASIL);
    private static final DateTimeFormatter MES_ANO = DateTimeFormatter.ofPattern("MMMM 'de' yyyy", BRASIL);
    private static final DateTimeFormatter MES_CURTO = DateTimeFormatter.ofPattern("MMM", BRASIL);

    private Formatadores() {
    }

    public static String moeda(BigDecimal valor) {
        BigDecimal seguro = (valor == null) ? BigDecimal.ZERO : valor;
        return NumberFormat.getCurrencyInstance(BRASIL).format(seguro);
    }

    public static String data(String dataIso) {
        if (dataIso == null) {
            return "";
        }
        return LocalDate.parse(dataIso).format(DATA_BR);
    }

    public static String data(LocalDate data) {
        return data.format(DATA_BR);
    }

    public static String mesAno(YearMonth mes) {
        String texto = mes.format(MES_ANO);
        return texto.substring(0, 1).toUpperCase(BRASIL) + texto.substring(1);
    }

    public static String mesCurto(YearMonth mes) {
        return mes.format(MES_CURTO).replace(".", "");
    }

    public static String mesAnoCurto(String dataIso) {
        if (dataIso == null) {
            return "";
        }
        YearMonth mes = YearMonth.from(LocalDate.parse(dataIso));
        return mesCurto(mes) + "/" + mes.getYear();
    }

    public static String percentual(double valor) {
        NumberFormat formato = NumberFormat.getNumberInstance(BRASIL);
        formato.setMaximumFractionDigits(1);
        return formato.format(valor) + "%";
    }

    public static BigDecimal converterValor(String texto) {
        if (texto == null || texto.trim().isEmpty()) {
            return null;
        }
        try {
            String limpo = texto.trim();
            if (limpo.contains(",")) {
                limpo = limpo.replace(".", "").replace(",", ".");
            }
            return new BigDecimal(limpo);
        } catch (NumberFormatException e) {
            return null;
        }
    }

    public static int cor(String hex) {
        try {
            return Color.parseColor(hex);
        } catch (IllegalArgumentException | NullPointerException e) {
            return Color.GRAY;
        }
    }
}
