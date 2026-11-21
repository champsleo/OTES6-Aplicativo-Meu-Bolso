package com.meubolso.api.service;

import com.meubolso.api.dto.ResumoDtos.GastoPorCategoria;
import com.meubolso.api.dto.ResumoDtos.HistoricoMensal;
import com.meubolso.api.dto.ResumoDtos.ResumoMensalResponse;
import com.meubolso.api.model.Categoria;
import com.meubolso.api.model.TipoTransacao;
import com.meubolso.api.model.Transacao;
import com.meubolso.api.repository.TransacaoRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
public class ResumoService {

    private static final BigDecimal CEM = BigDecimal.valueOf(100);

    private final TransacaoRepository transacaoRepository;

    public ResumoService(TransacaoRepository transacaoRepository) {
        this.transacaoRepository = transacaoRepository;
    }

    @Transactional(readOnly = true)
    public ResumoMensalResponse gerar(Long usuarioId, YearMonth mes) {
        List<Transacao> transacoes = transacaoRepository.findByUsuarioIdAndDataBetweenOrderByDataDescIdDesc(
                usuarioId, mes.atDay(1), mes.atEndOfMonth());

        BigDecimal totalReceitas = somar(transacoes, TipoTransacao.RECEITA);
        BigDecimal totalDespesas = somar(transacoes, TipoTransacao.DESPESA);

        return new ResumoMensalResponse(
                mes.toString(),
                totalReceitas,
                totalDespesas,
                totalReceitas.subtract(totalDespesas),
                transacoes.size(),
                agruparDespesasPorCategoria(transacoes, totalDespesas));
    }

    @Transactional(readOnly = true)
    // Uma consulta só para o período inteiro; o agrupamento por mês é feito em memória.
    public List<HistoricoMensal> historico(Long usuarioId, YearMonth ate, int meses) {
        YearMonth inicio = ate.minusMonths(meses - 1L);
        List<Transacao> transacoes = transacaoRepository.findByUsuarioIdAndDataBetweenOrderByDataDescIdDesc(
                usuarioId, inicio.atDay(1), ate.atEndOfMonth());

        Map<YearMonth, List<Transacao>> porMes = new LinkedHashMap<>();
        for (YearMonth mes = inicio; !mes.isAfter(ate); mes = mes.plusMonths(1)) {
            porMes.put(mes, new ArrayList<>());
        }
        for (Transacao t : transacoes) {
            porMes.get(YearMonth.from(t.getData())).add(t);
        }

        List<HistoricoMensal> resultado = new ArrayList<>();
        porMes.forEach((mes, lista) -> {
            BigDecimal receitas = somar(lista, TipoTransacao.RECEITA);
            BigDecimal despesas = somar(lista, TipoTransacao.DESPESA);
            resultado.add(new HistoricoMensal(mes.toString(), receitas, despesas, receitas.subtract(despesas)));
        });
        return resultado;
    }

    private BigDecimal somar(List<Transacao> transacoes, TipoTransacao tipo) {
        return transacoes.stream()
                .filter(t -> t.getTipo() == tipo)
                .map(Transacao::getValor)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private List<GastoPorCategoria> agruparDespesasPorCategoria(List<Transacao> transacoes, BigDecimal totalDespesas) {
        Map<Long, BigDecimal> totalPorCategoria = new LinkedHashMap<>();
        Map<Long, Categoria> categorias = new LinkedHashMap<>();

        for (Transacao t : transacoes) {
            if (t.getTipo() != TipoTransacao.DESPESA) {
                continue;
            }
            Categoria categoria = t.getCategoria();
            categorias.putIfAbsent(categoria.getId(), categoria);
            totalPorCategoria.merge(categoria.getId(), t.getValor(), BigDecimal::add);
        }

        return totalPorCategoria.entrySet().stream()
                .map(item -> criarGasto(categorias.get(item.getKey()), item.getValue(), totalDespesas))
                .sorted(Comparator.comparing(GastoPorCategoria::total).reversed())
                .toList();
    }

    private GastoPorCategoria criarGasto(Categoria categoria, BigDecimal total, BigDecimal totalDespesas) {
        BigDecimal limite = categoria.getLimiteMensal();
        Double percentualDoLimite = (limite == null) ? null : percentual(total, limite);
        boolean excedido = limite != null && total.compareTo(limite) > 0;

        return new GastoPorCategoria(
                categoria.getId(),
                categoria.getNome(),
                categoria.getCor(),
                total,
                percentual(total, totalDespesas),
                limite,
                percentualDoLimite,
                excedido);
    }

    private double percentual(BigDecimal parte, BigDecimal todo) {
        if (todo.signum() == 0) {
            return 0;
        }
        return parte.multiply(CEM).divide(todo, 1, RoundingMode.HALF_UP).doubleValue();
    }
}
