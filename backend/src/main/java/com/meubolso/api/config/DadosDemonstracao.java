package com.meubolso.api.config;

import com.meubolso.api.dto.AuthDtos.CadastroRequest;
import com.meubolso.api.dto.CategoriaDtos.CategoriaRequest;
import com.meubolso.api.dto.CategoriaDtos.CategoriaResponse;
import com.meubolso.api.dto.ContaDtos.ContaRequest;
import com.meubolso.api.dto.ContaDtos.ContaResponse;
import com.meubolso.api.dto.MetaDtos.MetaRequest;
import com.meubolso.api.dto.MetaDtos.MetaResponse;
import com.meubolso.api.dto.RecorrenciaDtos.RecorrenciaRequest;
import com.meubolso.api.dto.TransacaoDtos.TransacaoRequest;
import com.meubolso.api.dto.TransferenciaDtos.TransferenciaRequest;
import com.meubolso.api.model.TipoConta;
import com.meubolso.api.model.TipoTransacao;
import com.meubolso.api.repository.UsuarioRepository;
import com.meubolso.api.service.AuthService;
import com.meubolso.api.service.CategoriaService;
import com.meubolso.api.service.ContaService;
import com.meubolso.api.service.MetaService;
import com.meubolso.api.service.RecorrenciaService;
import com.meubolso.api.service.TransacaoService;
import com.meubolso.api.service.TransferenciaService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.Map;
import java.util.stream.Collectors;

// Cria a conta demo@meubolso.com (senha 123456) com 6 meses de dados, só na primeira vez que a API sobe.
@Component
@ConditionalOnProperty(name = "app.demo.habilitado", havingValue = "true")
public class DadosDemonstracao implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(DadosDemonstracao.class);
    private static final String EMAIL_DEMO = "demo@meubolso.com";

    private final UsuarioRepository usuarioRepository;
    private final AuthService authService;
    private final CategoriaService categoriaService;
    private final ContaService contaService;
    private final TransacaoService transacaoService;
    private final TransferenciaService transferenciaService;
    private final RecorrenciaService recorrenciaService;
    private final MetaService metaService;
    private final Clock relogio;

    public DadosDemonstracao(UsuarioRepository usuarioRepository,
                             AuthService authService,
                             CategoriaService categoriaService,
                             ContaService contaService,
                             TransacaoService transacaoService,
                             TransferenciaService transferenciaService,
                             RecorrenciaService recorrenciaService,
                             MetaService metaService,
                             Clock relogio) {
        this.usuarioRepository = usuarioRepository;
        this.authService = authService;
        this.categoriaService = categoriaService;
        this.contaService = contaService;
        this.transacaoService = transacaoService;
        this.transferenciaService = transferenciaService;
        this.recorrenciaService = recorrenciaService;
        this.metaService = metaService;
        this.relogio = relogio;
    }

    @Override
    public void run(String... args) {
        if (usuarioRepository.existsByEmailIgnoreCase(EMAIL_DEMO)) {
            return;
        }

        Long uid = authService.cadastrar(new CadastroRequest("Usuário Demo", EMAIL_DEMO, "123456")).usuario().id();

        Map<String, Long> cat = categoriaService.listar(uid, null).stream()
                .collect(Collectors.toMap(CategoriaResponse::nome, CategoriaResponse::id, (a, b) -> a));

        categoriaService.atualizar(cat.get("Alimentação"),
                new CategoriaRequest("Alimentação", TipoTransacao.DESPESA, "#EF5350", new BigDecimal("900.00")), uid);
        categoriaService.atualizar(cat.get("Lazer"),
                new CategoriaRequest("Lazer", TipoTransacao.DESPESA, "#AB47BC", new BigDecimal("150.00")), uid);

        Map<String, Long> conta = contaService.listar(uid).stream()
                .collect(Collectors.toMap(ContaResponse::nome, ContaResponse::id, (a, b) -> a));
        contaService.atualizar(conta.get("Conta bancária"),
                new ContaRequest("Conta bancária", TipoConta.BANCO, new BigDecimal("1500.00")), uid);
        contaService.atualizar(conta.get("Carteira"),
                new ContaRequest("Carteira", TipoConta.CARTEIRA, new BigDecimal("80.00")), uid);
        Long cartao = contaService.criar(
                new ContaRequest("Cartão de crédito", TipoConta.CARTAO_CREDITO, BigDecimal.ZERO), uid).id();
        Long banco = conta.get("Conta bancária");
        Long carteira = conta.get("Carteira");

        YearMonth atual = YearMonth.now(relogio);
        YearMonth inicio = atual.minusMonths(5);

        recorrencia(uid, "Salário", "3200.00", inicio.atDay(5), cat.get("Salário"), banco);
        recorrencia(uid, "Aluguel", "1100.00", inicio.atDay(8), cat.get("Moradia"), banco);
        recorrencia(uid, "Streaming", "39.90", inicio.atDay(12), cat.get("Lazer"), cartao);
        recorrencia(uid, "Academia", "99.90", inicio.atDay(10), cat.get("Saúde"), cartao);

        String[] mercado = {"612.40", "548.90", "701.15", "590.00", "655.30", "478.60"};
        String[] transporte = {"180.00", "210.50", "165.00", "240.00", "195.80", "120.00"};
        String[] freela = {"0", "850.00", "0", "1200.00", "0", "600.00"};
        for (int i = 0; i < 6; i++) {
            YearMonth mes = inicio.plusMonths(i);
            boolean mesAtual = mes.equals(atual);
            lancar(uid, "Supermercado", mercado[i], mes, 3, cat.get("Alimentação"), cartao);
            lancar(uid, "Transporte", transporte[i], mes, 15, cat.get("Transporte"), carteira);
            if (!"0".equals(freela[i])) {
                lancar(uid, "Projeto freelance", freela[i], mes, 20, cat.get("Freelance"), banco);
            }
            if (!mesAtual) {
                lancar(uid, "Restaurante", "120.00", mes, 18, cat.get("Alimentação"), cartao);
                transferir(uid, banco, cartao, mes.atDay(25), "650.00", "Pagamento da fatura");
            }
        }
        LocalDate hoje = LocalDate.now(relogio);
        lancarNoDia(uid, "Show", "180.00", hoje, cat.get("Lazer"), cartao);
        lancarNoDia(uid, "Farmácia", "72.40", hoje, cat.get("Saúde"), carteira);
        transferenciaService.criar(new TransferenciaRequest(banco, carteira, new BigDecimal("200.00"), hoje,
                "Saque"), uid);

        MetaResponse viagem = metaService.criar(new MetaRequest("Viagem de férias", new BigDecimal("3000.00"),
                atual.plusMonths(8).atEndOfMonth()), uid);
        metaService.movimentar(viagem.id(), new BigDecimal("1200.00"), uid);
        MetaResponse reserva = metaService.criar(new MetaRequest("Reserva de emergência", new BigDecimal("10000.00"),
                null), uid);
        metaService.movimentar(reserva.id(), new BigDecimal("2500.00"), uid);

        log.info("Usuário de demonstração criado: {} / senha 123456", EMAIL_DEMO);
    }

    private void recorrencia(Long uid, String descricao, String valor, LocalDate inicio, Long categoriaId, Long contaId) {
        recorrenciaService.criar(new RecorrenciaRequest(descricao, new BigDecimal(valor), inicio, categoriaId, contaId),
                uid);
    }

    private void lancar(Long uid, String descricao, String valor, YearMonth mes, int dia, Long categoriaId,
                        Long contaId) {
        LocalDate data = mes.atDay(Math.min(dia, mes.lengthOfMonth()));
        LocalDate hoje = LocalDate.now(relogio);
        // Datas no futuro ficariam estranhas na demonstração.
        if (data.isAfter(hoje)) {
            data = hoje;
        }
        lancarNoDia(uid, descricao, valor, data, categoriaId, contaId);
    }

    private void lancarNoDia(Long uid, String descricao, String valor, LocalDate data, Long categoriaId,
                             Long contaId) {
        transacaoService.criar(new TransacaoRequest(descricao, new BigDecimal(valor), data, categoriaId, contaId),
                uid);
    }

    private void transferir(Long uid, Long origem, Long destino, LocalDate data, String valor, String descricao) {
        LocalDate hoje = LocalDate.now(relogio);
        transferenciaService.criar(new TransferenciaRequest(origem, destino, new BigDecimal(valor),
                data.isAfter(hoje) ? hoje : data, descricao), uid);
    }
}
