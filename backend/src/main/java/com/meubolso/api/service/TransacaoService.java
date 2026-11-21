package com.meubolso.api.service;

import com.meubolso.api.dto.TransacaoDtos.AlertaLimite;
import com.meubolso.api.dto.TransacaoDtos.TransacaoRequest;
import com.meubolso.api.dto.TransacaoDtos.TransacaoResponse;
import com.meubolso.api.exception.RecursoNaoEncontradoException;
import com.meubolso.api.model.Categoria;
import com.meubolso.api.model.Conta;
import com.meubolso.api.model.TipoTransacao;
import com.meubolso.api.model.Transacao;
import com.meubolso.api.model.Usuario;
import com.meubolso.api.repository.TransacaoRepository;
import com.meubolso.api.repository.UsuarioRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.Normalizer;
import java.time.YearMonth;
import java.util.List;
import java.util.Locale;

@Service
public class TransacaoService {

    private static final double PERCENTUAL_ALERTA = 80.0;
    private static final BigDecimal CEM = BigDecimal.valueOf(100);

    private final TransacaoRepository transacaoRepository;
    private final UsuarioRepository usuarioRepository;
    private final CategoriaService categoriaService;
    private final ContaService contaService;

    public TransacaoService(TransacaoRepository transacaoRepository,
                            UsuarioRepository usuarioRepository,
                            CategoriaService categoriaService,
                            ContaService contaService) {
        this.transacaoRepository = transacaoRepository;
        this.usuarioRepository = usuarioRepository;
        this.categoriaService = categoriaService;
        this.contaService = contaService;
    }

    @Transactional(readOnly = true)
    public List<TransacaoResponse> listar(Long usuarioId, YearMonth mes, TipoTransacao tipo,
                                          Long contaId, String busca) {
        String termo = (busca == null || busca.isBlank()) ? null : normalizar(busca);

        return transacaoRepository
                // Os filtros rodam em memória porque um mês tem poucos lançamentos; mantém a consulta simples.
                .findByUsuarioIdAndDataBetweenOrderByDataDescIdDesc(usuarioId, mes.atDay(1), mes.atEndOfMonth())
                .stream()
                .filter(t -> tipo == null || t.getTipo() == tipo)
                .filter(t -> contaId == null || t.getConta().getId().equals(contaId))
                .filter(t -> termo == null || normalizar(t.getDescricao()).contains(termo))
                .map(TransacaoResponse::de)
                .toList();
    }

    @Transactional(readOnly = true)
    public TransacaoResponse buscar(Long id, Long usuarioId) {
        return TransacaoResponse.de(buscarEntidade(id, usuarioId));
    }

    @Transactional
    public TransacaoResponse criar(TransacaoRequest request, Long usuarioId) {
        Categoria categoria = categoriaService.buscarEntidade(request.categoriaId(), usuarioId);
        Conta conta = contaService.buscarEntidade(request.contaId(), usuarioId);
        Usuario usuario = usuarioRepository.getReferenceById(usuarioId);

        Transacao transacao = transacaoRepository.save(new Transacao(
                request.descricao().trim(), request.valor(), request.data(), categoria, conta, usuario));
        return TransacaoResponse.de(transacao, verificarLimite(transacao));
    }

    @Transactional
    public TransacaoResponse atualizar(Long id, TransacaoRequest request, Long usuarioId) {
        Transacao transacao = buscarEntidade(id, usuarioId);
        Categoria categoria = categoriaService.buscarEntidade(request.categoriaId(), usuarioId);
        Conta conta = contaService.buscarEntidade(request.contaId(), usuarioId);

        transacao.atualizar(request.descricao().trim(), request.valor(), request.data(), categoria, conta);
        return TransacaoResponse.de(transacao, verificarLimite(transacao));
    }

    @Transactional
    public void excluir(Long id, Long usuarioId) {
        transacaoRepository.delete(buscarEntidade(id, usuarioId));
    }

    // A soma já inclui o lançamento recém-salvo: o Hibernate grava as mudanças antes de rodar a consulta.
    private AlertaLimite verificarLimite(Transacao transacao) {
        Categoria categoria = transacao.getCategoria();
        BigDecimal limite = categoria.getLimiteMensal();
        if (transacao.getTipo() != TipoTransacao.DESPESA || limite == null || limite.signum() <= 0) {
            return null;
        }

        YearMonth mes = YearMonth.from(transacao.getData());
        BigDecimal total = transacaoRepository.somarPorCategoriaNoPeriodo(
                categoria.getId(), mes.atDay(1), mes.atEndOfMonth());
        if (total == null) {
            return null;
        }

        double percentual = total.multiply(CEM).divide(limite, 1, RoundingMode.HALF_UP).doubleValue();
        if (percentual < PERCENTUAL_ALERTA) {
            return null;
        }
        return new AlertaLimite(categoria.getNome(), total, limite, percentual, total.compareTo(limite) > 0);
    }

    private Transacao buscarEntidade(Long id, Long usuarioId) {
        return transacaoRepository.findByIdAndUsuarioId(id, usuarioId)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Lançamento não encontrado"));
    }

    // Sem acentos e em minúsculas, para "farmacia" encontrar "Farmácia".
    private static String normalizar(String texto) {
        String semAcento = Normalizer.normalize(texto, Normalizer.Form.NFD).replaceAll("\\p{M}", "");
        return semAcento.toLowerCase(Locale.ROOT).trim();
    }
}
