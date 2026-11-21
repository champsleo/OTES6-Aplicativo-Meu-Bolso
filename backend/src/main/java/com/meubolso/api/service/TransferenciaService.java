package com.meubolso.api.service;

import com.meubolso.api.dto.TransferenciaDtos.TransferenciaRequest;
import com.meubolso.api.dto.TransferenciaDtos.TransferenciaResponse;
import com.meubolso.api.exception.RecursoNaoEncontradoException;
import com.meubolso.api.exception.RegraNegocioException;
import com.meubolso.api.model.Conta;
import com.meubolso.api.model.Transferencia;
import com.meubolso.api.repository.TransferenciaRepository;
import com.meubolso.api.repository.UsuarioRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.YearMonth;
import java.util.List;

@Service
public class TransferenciaService {

    private final TransferenciaRepository transferenciaRepository;
    private final UsuarioRepository usuarioRepository;
    private final ContaService contaService;

    public TransferenciaService(TransferenciaRepository transferenciaRepository,
                                UsuarioRepository usuarioRepository,
                                ContaService contaService) {
        this.transferenciaRepository = transferenciaRepository;
        this.usuarioRepository = usuarioRepository;
        this.contaService = contaService;
    }

    @Transactional(readOnly = true)
    public List<TransferenciaResponse> listar(Long usuarioId, YearMonth mes) {
        return transferenciaRepository
                .findByUsuarioIdAndDataBetweenOrderByDataDescIdDesc(usuarioId, mes.atDay(1), mes.atEndOfMonth())
                .stream()
                .map(TransferenciaResponse::de)
                .toList();
    }

    @Transactional
    public TransferenciaResponse criar(TransferenciaRequest request, Long usuarioId) {
        if (request.contaOrigemId().equals(request.contaDestinoId())) {
            throw new RegraNegocioException("A conta de origem e a de destino devem ser diferentes");
        }
        Conta origem = contaService.buscarEntidade(request.contaOrigemId(), usuarioId);
        Conta destino = contaService.buscarEntidade(request.contaDestinoId(), usuarioId);

        String descricao = (request.descricao() == null || request.descricao().isBlank())
                ? null : request.descricao().trim();

        Transferencia transferencia = new Transferencia(origem, destino, request.valor(), request.data(),
                descricao, usuarioRepository.getReferenceById(usuarioId));
        return TransferenciaResponse.de(transferenciaRepository.save(transferencia));
    }

    @Transactional
    public void excluir(Long id, Long usuarioId) {
        Transferencia transferencia = transferenciaRepository.findByIdAndUsuarioId(id, usuarioId)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Transferência não encontrada"));
        transferenciaRepository.delete(transferencia);
    }
}
