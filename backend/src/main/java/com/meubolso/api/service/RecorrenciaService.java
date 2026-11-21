package com.meubolso.api.service;

import com.meubolso.api.dto.RecorrenciaDtos.RecorrenciaRequest;
import com.meubolso.api.dto.RecorrenciaDtos.RecorrenciaResponse;
import com.meubolso.api.exception.RecursoNaoEncontradoException;
import com.meubolso.api.model.Categoria;
import com.meubolso.api.model.Conta;
import com.meubolso.api.model.Recorrencia;
import com.meubolso.api.model.Transacao;
import com.meubolso.api.repository.RecorrenciaRepository;
import com.meubolso.api.repository.TransacaoRepository;
import com.meubolso.api.repository.UsuarioRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;

@Service
public class RecorrenciaService {

    private static final Logger log = LoggerFactory.getLogger(RecorrenciaService.class);

    private final RecorrenciaRepository recorrenciaRepository;
    private final TransacaoRepository transacaoRepository;
    private final UsuarioRepository usuarioRepository;
    private final CategoriaService categoriaService;
    private final ContaService contaService;
    private final Clock relogio;

    public RecorrenciaService(RecorrenciaRepository recorrenciaRepository,
                              TransacaoRepository transacaoRepository,
                              UsuarioRepository usuarioRepository,
                              CategoriaService categoriaService,
                              ContaService contaService,
                              Clock relogio) {
        this.recorrenciaRepository = recorrenciaRepository;
        this.transacaoRepository = transacaoRepository;
        this.usuarioRepository = usuarioRepository;
        this.categoriaService = categoriaService;
        this.contaService = contaService;
        this.relogio = relogio;
    }

    @Transactional(readOnly = true)
    public List<RecorrenciaResponse> listar(Long usuarioId) {
        return recorrenciaRepository.findByUsuarioIdOrderByDiaDoMesAscDescricaoAsc(usuarioId).stream()
                .map(this::paraResponse)
                .toList();
    }

    @Transactional
    public RecorrenciaResponse criar(RecorrenciaRequest request, Long usuarioId) {
        Categoria categoria = categoriaService.buscarEntidade(request.categoriaId(), usuarioId);
        Conta conta = contaService.buscarEntidade(request.contaId(), usuarioId);

        Recorrencia recorrencia = recorrenciaRepository.save(new Recorrencia(
                request.descricao().trim(), request.valor(), request.dataInicio(),
                categoria, conta, usuarioRepository.getReferenceById(usuarioId)));

        gerarPendentes(recorrencia, hoje());
        return paraResponse(recorrencia);
    }

    @Transactional
    public RecorrenciaResponse alterarStatus(Long id, boolean ativa, Long usuarioId) {
        Recorrencia recorrencia = buscarEntidade(id, usuarioId);
        // Ao retomar, os meses em que ficou pausada não são lançados retroativamente.
        if (ativa && !recorrencia.isAtiva()) {
            LocalDate hoje = hoje();
            YearMonth mesAtual = YearMonth.from(hoje);
            YearMonth ultimo = recorrencia.dataNoMes(mesAtual).isBefore(hoje) ? mesAtual : mesAtual.minusMonths(1);
            YearMonth jaGerado = recorrencia.getUltimoMesGerado();
            if (jaGerado == null || jaGerado.isBefore(ultimo)) {
                recorrencia.marcarGerado(ultimo);
            }
        }
        recorrencia.setAtiva(ativa);
        if (ativa) {
            gerarPendentes(recorrencia, hoje());
        }
        return paraResponse(recorrencia);
    }

    @Transactional
    public void excluir(Long id, Long usuarioId) {
        Recorrencia recorrencia = buscarEntidade(id, usuarioId);
        transacaoRepository.desvincularRecorrencia(id);
        recorrenciaRepository.delete(recorrencia);
    }

    // Roda todo dia às 00:05.
    @Scheduled(cron = "0 5 0 * * *")
    @Transactional
    public void gerarTodasAgendado() {
        gerarTodas();
    }

    // Cobre os dias em que a API ficou desligada.
    @EventListener(ApplicationReadyEvent.class)
    @Transactional
    public void gerarAoIniciar() {
        gerarTodas();
    }

    private void gerarTodas() {
        LocalDate hoje = hoje();
        int total = 0;
        for (Recorrencia recorrencia : recorrenciaRepository.findByAtivaTrue()) {
            total += gerarPendentes(recorrencia, hoje);
        }
        if (total > 0) {
            log.info("{} lançamento(s) recorrente(s) criado(s) automaticamente", total);
        }
    }

    // Lança os meses cujo dia já chegou e que ainda não foram lançados, sem duplicar.
    int gerarPendentes(Recorrencia recorrencia, LocalDate hoje) {
        if (!recorrencia.isAtiva()) {
            return 0;
        }
        YearMonth ultimo = recorrencia.getUltimoMesGerado();
        YearMonth mes = (ultimo == null) ? YearMonth.from(recorrencia.getDataInicio()) : ultimo.plusMonths(1);
        YearMonth mesAtual = YearMonth.from(hoje);
        int criados = 0;

        while (!mes.isAfter(mesAtual)) {
            LocalDate data = recorrencia.dataNoMes(mes);
            if (data.isAfter(hoje)) {
                break;
            }
            if (!data.isBefore(recorrencia.getDataInicio())) {
                Transacao transacao = new Transacao(recorrencia.getDescricao(), recorrencia.getValor(), data,
                        recorrencia.getCategoria(), recorrencia.getConta(), recorrencia.getUsuario());
                transacao.setRecorrencia(recorrencia);
                transacaoRepository.save(transacao);
                criados++;
            }
            recorrencia.marcarGerado(mes);
            mes = mes.plusMonths(1);
        }
        return criados;
    }

    private Recorrencia buscarEntidade(Long id, Long usuarioId) {
        return recorrenciaRepository.findByIdAndUsuarioId(id, usuarioId)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Recorrência não encontrada"));
    }

    private RecorrenciaResponse paraResponse(Recorrencia recorrencia) {
        return RecorrenciaResponse.de(recorrencia, proximaData(recorrencia));
    }

    private LocalDate proximaData(Recorrencia recorrencia) {
        if (!recorrencia.isAtiva()) {
            return null;
        }
        YearMonth ultimo = recorrencia.getUltimoMesGerado();
        YearMonth proximo = (ultimo == null) ? YearMonth.from(recorrencia.getDataInicio()) : ultimo.plusMonths(1);
        return recorrencia.dataNoMes(proximo);
    }

    private LocalDate hoje() {
        return LocalDate.now(relogio);
    }
}
