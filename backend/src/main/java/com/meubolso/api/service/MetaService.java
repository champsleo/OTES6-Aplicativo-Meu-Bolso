package com.meubolso.api.service;

import com.meubolso.api.dto.MetaDtos.MetaRequest;
import com.meubolso.api.dto.MetaDtos.MetaResponse;
import com.meubolso.api.exception.RecursoNaoEncontradoException;
import com.meubolso.api.exception.RegraNegocioException;
import com.meubolso.api.model.Meta;
import com.meubolso.api.repository.MetaRepository;
import com.meubolso.api.repository.UsuarioRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Clock;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.temporal.ChronoUnit;
import java.util.List;

@Service
public class MetaService {

    private static final BigDecimal CEM = BigDecimal.valueOf(100);

    private final MetaRepository metaRepository;
    private final UsuarioRepository usuarioRepository;
    private final Clock relogio;

    public MetaService(MetaRepository metaRepository, UsuarioRepository usuarioRepository, Clock relogio) {
        this.metaRepository = metaRepository;
        this.usuarioRepository = usuarioRepository;
        this.relogio = relogio;
    }

    @Transactional(readOnly = true)
    public List<MetaResponse> listar(Long usuarioId) {
        return metaRepository.findByUsuarioIdOrderByPrazoAscNomeAsc(usuarioId).stream()
                .map(this::paraResponse)
                .toList();
    }

    @Transactional
    public MetaResponse criar(MetaRequest request, Long usuarioId) {
        validarPrazo(request.prazo());
        Meta meta = new Meta(request.nome().trim(), request.valorAlvo(), request.prazo(),
                usuarioRepository.getReferenceById(usuarioId));
        return paraResponse(metaRepository.save(meta));
    }

    @Transactional
    public MetaResponse atualizar(Long id, MetaRequest request, Long usuarioId) {
        Meta meta = buscarEntidade(id, usuarioId);
        meta.atualizar(request.nome().trim(), request.valorAlvo(), request.prazo());
        return paraResponse(meta);
    }

    @Transactional
    public MetaResponse movimentar(Long id, BigDecimal valor, Long usuarioId) {
        if (valor.signum() == 0) {
            throw new RegraNegocioException("Informe um valor diferente de zero");
        }
        Meta meta = buscarEntidade(id, usuarioId);
        if (meta.getValorGuardado().add(valor).signum() < 0) {
            throw new RegraNegocioException("Não é possível retirar mais do que o valor guardado na meta");
        }
        meta.movimentar(valor);
        return paraResponse(meta);
    }

    @Transactional
    public void excluir(Long id, Long usuarioId) {
        metaRepository.delete(buscarEntidade(id, usuarioId));
    }

    private Meta buscarEntidade(Long id, Long usuarioId) {
        return metaRepository.findByIdAndUsuarioId(id, usuarioId)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Meta não encontrada"));
    }

    private void validarPrazo(LocalDate prazo) {
        if (prazo != null && prazo.isBefore(LocalDate.now(relogio))) {
            throw new RegraNegocioException("O prazo da meta não pode estar no passado");
        }
    }

    MetaResponse paraResponse(Meta meta) {
        BigDecimal faltante = meta.getValorAlvo().subtract(meta.getValorGuardado()).max(BigDecimal.ZERO);
        double percentual = meta.getValorGuardado().multiply(CEM)
                .divide(meta.getValorAlvo(), 1, RoundingMode.HALF_UP).doubleValue();

        return new MetaResponse(
                meta.getId(),
                meta.getNome(),
                meta.getValorAlvo(),
                meta.getValorGuardado(),
                faltante,
                percentual,
                meta.getPrazo(),
                valorMensalSugerido(faltante, meta.getPrazo()),
                faltante.signum() == 0);
    }

    // Divide o que falta pelos meses até o prazo, contando o mês atual.
    private BigDecimal valorMensalSugerido(BigDecimal faltante, LocalDate prazo) {
        if (prazo == null || faltante.signum() == 0) {
            return null;
        }
        long meses = ChronoUnit.MONTHS.between(YearMonth.now(relogio), YearMonth.from(prazo)) + 1;
        return faltante.divide(BigDecimal.valueOf(Math.max(meses, 1)), 2, RoundingMode.UP);
    }
}
