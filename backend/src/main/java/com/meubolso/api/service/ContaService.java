package com.meubolso.api.service;

import com.meubolso.api.dto.ContaDtos.ContaRequest;
import com.meubolso.api.dto.ContaDtos.ContaResponse;
import com.meubolso.api.exception.ConflitoException;
import com.meubolso.api.exception.RecursoNaoEncontradoException;
import com.meubolso.api.exception.RegraNegocioException;
import com.meubolso.api.model.Conta;
import com.meubolso.api.model.TipoConta;
import com.meubolso.api.model.TipoTransacao;
import com.meubolso.api.model.Usuario;
import com.meubolso.api.repository.ContaRepository;
import com.meubolso.api.repository.RecorrenciaRepository;
import com.meubolso.api.repository.TransacaoRepository;
import com.meubolso.api.repository.TransferenciaRepository;
import com.meubolso.api.repository.UsuarioRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Service
public class ContaService {

    private final ContaRepository contaRepository;
    private final TransacaoRepository transacaoRepository;
    private final TransferenciaRepository transferenciaRepository;
    private final RecorrenciaRepository recorrenciaRepository;
    private final UsuarioRepository usuarioRepository;

    public ContaService(ContaRepository contaRepository,
                        TransacaoRepository transacaoRepository,
                        TransferenciaRepository transferenciaRepository,
                        RecorrenciaRepository recorrenciaRepository,
                        UsuarioRepository usuarioRepository) {
        this.contaRepository = contaRepository;
        this.transacaoRepository = transacaoRepository;
        this.transferenciaRepository = transferenciaRepository;
        this.recorrenciaRepository = recorrenciaRepository;
        this.usuarioRepository = usuarioRepository;
    }

    @Transactional(readOnly = true)
    public List<ContaResponse> listar(Long usuarioId) {
        return contaRepository.findByUsuarioIdOrderByNomeAsc(usuarioId).stream()
                .map(conta -> ContaResponse.de(conta, calcularSaldo(conta)))
                .toList();
    }

    @Transactional(readOnly = true)
    public ContaResponse buscar(Long id, Long usuarioId) {
        Conta conta = buscarEntidade(id, usuarioId);
        return ContaResponse.de(conta, calcularSaldo(conta));
    }

    @Transactional
    public ContaResponse criar(ContaRequest request, Long usuarioId) {
        String nome = request.nome().trim();
        if (contaRepository.existsByUsuarioIdAndNomeIgnoreCase(usuarioId, nome)) {
            throw new ConflitoException("Já existe uma conta com esse nome");
        }
        Usuario usuario = usuarioRepository.getReferenceById(usuarioId);
        Conta conta = contaRepository.save(new Conta(nome, request.tipo(), request.saldoInicial(), usuario));
        return ContaResponse.de(conta, calcularSaldo(conta));
    }

    @Transactional
    public ContaResponse atualizar(Long id, ContaRequest request, Long usuarioId) {
        Conta conta = buscarEntidade(id, usuarioId);
        String nome = request.nome().trim();
        if (contaRepository.existsByUsuarioIdAndNomeIgnoreCaseAndIdNot(usuarioId, nome, id)) {
            throw new ConflitoException("Já existe uma conta com esse nome");
        }
        conta.atualizar(nome, request.tipo(), request.saldoInicial());
        return ContaResponse.de(conta, calcularSaldo(conta));
    }

    @Transactional
    public void excluir(Long id, Long usuarioId) {
        Conta conta = buscarEntidade(id, usuarioId);
        boolean emUso = transacaoRepository.existsByContaId(id)
                || transferenciaRepository.existsByContaOrigemIdOrContaDestinoId(id, id)
                || recorrenciaRepository.existsByContaId(id);
        if (emUso) {
            throw new RegraNegocioException(
                    "Esta conta possui lançamentos, transferências ou recorrências. Remova-os antes de excluir.");
        }
        contaRepository.delete(conta);
    }

    @Transactional(readOnly = true)
    public Conta buscarEntidade(Long id, Long usuarioId) {
        return contaRepository.findByIdAndUsuarioId(id, usuarioId)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Conta não encontrada"));
    }

    // Saldo = inicial + receitas − despesas + transferências recebidas − enviadas.
    public BigDecimal calcularSaldo(Conta conta) {
        Long id = conta.getId();
        return conta.getSaldoInicial()
                .add(zeroSeNulo(transacaoRepository.somarPorContaETipo(id, TipoTransacao.RECEITA)))
                .subtract(zeroSeNulo(transacaoRepository.somarPorContaETipo(id, TipoTransacao.DESPESA)))
                .add(zeroSeNulo(transferenciaRepository.somarEntradas(id)))
                .subtract(zeroSeNulo(transferenciaRepository.somarSaidas(id)));
    }

    @Transactional
    public void criarContasPadrao(Usuario usuario) {
        contaRepository.saveAll(List.of(
                new Conta("Carteira", TipoConta.CARTEIRA, BigDecimal.ZERO, usuario),
                new Conta("Conta bancária", TipoConta.BANCO, BigDecimal.ZERO, usuario)));
    }

    private BigDecimal zeroSeNulo(BigDecimal valor) {
        return valor == null ? BigDecimal.ZERO : valor;
    }
}
