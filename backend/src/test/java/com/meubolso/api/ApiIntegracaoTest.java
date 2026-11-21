package com.meubolso.api;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

// Testa a API de ponta a ponta: HTTP → controller → service → banco H2 em memória.
@SpringBootTest
@AutoConfigureMockMvc
class ApiIntegracaoTest {

    @Autowired
    private MockMvc mvc;

    @Autowired
    private ObjectMapper objectMapper;

    private final String hoje = LocalDate.now().toString();

    @Test
    void deveRecusarRequisicaoSemToken() throws Exception {
        mvc.perform(get("/api/transacoes"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.mensagem").exists());
    }

    @Test
    void deveCadastrarComCategoriasEContasPadrao() throws Exception {
        String token = cadastrar(emailAleatorio());

        mvc.perform(get("/api/categorias").header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(10));
        mvc.perform(get("/api/contas").header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2));
    }

    @Test
    void naoDevePermitirEmailDuplicado() throws Exception {
        String email = emailAleatorio();
        cadastrar(email);

        mvc.perform(post("/api/auth/cadastro")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(Map.of("nome", "Outro", "email", email, "senha", "123456"))))
                .andExpect(status().isConflict());
    }

    @Test
    void deveRecusarLoginComSenhaErrada() throws Exception {
        String email = emailAleatorio();
        cadastrar(email);

        mvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(Map.of("email", email, "senha", "senhaErrada"))))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.mensagem").value("E-mail ou senha incorretos"));
    }

    @Test
    void deveValidarDadosDoLancamento() throws Exception {
        String token = cadastrar(emailAleatorio());

        mvc.perform(post("/api/transacoes")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(Map.of("descricao", "", "valor", -10, "data", hoje, "categoriaId", 1))))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.detalhes.length()").value(3));
    }

    @Test
    void fluxoCompletoDeLancamentosEResumo() throws Exception {
        String token = cadastrar(emailAleatorio());
        long alimentacao = idPorNome(token, "/api/categorias", "Alimentação");
        long salario = idPorNome(token, "/api/categorias", "Salário");
        long banco = idPorNome(token, "/api/contas", "Conta bancária");
        String mes = YearMonth.now().toString();

        criarLancamento(token, "Salário", "3000.00", hoje, salario, banco);
        long idMercado = criarLancamento(token, "Mercado", "250.00", hoje, alimentacao, banco);

        mvc.perform(put("/api/transacoes/" + idMercado)
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(Map.of("descricao", "Mercado do mês", "valor", "300.00",
                                "data", hoje, "categoriaId", alimentacao, "contaId", banco))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.descricao").value("Mercado do mês"))
                .andExpect(jsonPath("$.tipo").value("DESPESA"))
                .andExpect(jsonPath("$.contaNome").value("Conta bancária"));

        mvc.perform(get("/api/transacoes").param("mes", mes).header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2));

        mvc.perform(get("/api/resumo").param("mes", mes).header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalReceitas").value(3000.0))
                .andExpect(jsonPath("$.totalDespesas").value(300.0))
                .andExpect(jsonPath("$.saldo").value(2700.0))
                .andExpect(jsonPath("$.despesasPorCategoria[0].nome").value("Alimentação"));

        mvc.perform(delete("/api/categorias/" + alimentacao).header("Authorization", "Bearer " + token))
                .andExpect(status().isUnprocessableEntity());

        mvc.perform(delete("/api/transacoes/" + idMercado).header("Authorization", "Bearer " + token))
                .andExpect(status().isNoContent());
    }

    @Test
    void deveBuscarPorDescricaoIgnorandoAcentos() throws Exception {
        String token = cadastrar(emailAleatorio());
        long saude = idPorNome(token, "/api/categorias", "Saúde");
        long carteira = idPorNome(token, "/api/contas", "Carteira");
        criarLancamento(token, "Farmácia", "50.00", hoje, saude, carteira);
        criarLancamento(token, "Consulta", "200.00", hoje, saude, carteira);

        mvc.perform(get("/api/transacoes").param("busca", "FARMACIA").header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].descricao").value("Farmácia"));
    }

    @Test
    void deveAvisarQuandoLimiteForExcedido() throws Exception {
        String token = cadastrar(emailAleatorio());
        long carteira = idPorNome(token, "/api/contas", "Carteira");

        String resposta = mvc.perform(post("/api/categorias")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(Map.of("nome", "Delivery", "tipo", "DESPESA", "cor", "#FF0000", "limiteMensal", "100.00"))))
                .andExpect(status().isCreated())
                .andExpect(header().exists("Location"))
                .andReturn().getResponse().getContentAsString();
        long delivery = objectMapper.readTree(resposta).get("id").asLong();

        mvc.perform(post("/api/transacoes")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(Map.of("descricao", "Lanche", "valor", "50.00", "data", hoje,
                                "categoriaId", delivery, "contaId", carteira))))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.alertaLimite").doesNotExist());

        mvc.perform(post("/api/transacoes")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(Map.of("descricao", "Pizza", "valor", "70.00", "data", hoje,
                                "categoriaId", delivery, "contaId", carteira))))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.alertaLimite.excedido").value(true))
                .andExpect(jsonPath("$.alertaLimite.percentual").value(120.0));

        mvc.perform(get("/api/resumo").header("Authorization", "Bearer " + token))
                .andExpect(jsonPath("$.despesasPorCategoria[0].limiteExcedido").value(true));
    }

    @Test
    void usuarioNaoAcessaDadosDeOutroUsuario() throws Exception {
        String tokenA = cadastrar(emailAleatorio());
        String tokenB = cadastrar(emailAleatorio());
        long categoriaA = idPorNome(tokenA, "/api/categorias", "Lazer");
        long contaA = idPorNome(tokenA, "/api/contas", "Carteira");
        long lancamentoA = criarLancamento(tokenA, "Cinema", "40.00", hoje, categoriaA, contaA);

        mvc.perform(get("/api/transacoes/" + lancamentoA).header("Authorization", "Bearer " + tokenB))
                .andExpect(status().isNotFound());
        mvc.perform(get("/api/contas/" + contaA).header("Authorization", "Bearer " + tokenB))
                .andExpect(status().isNotFound());
    }

    @Test
    void saldoDaContaConsideraLancamentosETransferencias() throws Exception {
        String token = cadastrar(emailAleatorio());
        long banco = idPorNome(token, "/api/contas", "Conta bancária");
        long carteira = idPorNome(token, "/api/contas", "Carteira");
        long salario = idPorNome(token, "/api/categorias", "Salário");
        long lazer = idPorNome(token, "/api/categorias", "Lazer");

        criarLancamento(token, "Salário", "1000.00", hoje, salario, banco);
        criarLancamento(token, "Cinema", "30.00", hoje, lazer, carteira);

        mvc.perform(post("/api/transferencias")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(Map.of("contaOrigemId", banco, "contaDestinoId", carteira,
                                "valor", "200.00", "data", hoje))))
                .andExpect(status().isCreated());

        mvc.perform(get("/api/contas/" + banco).header("Authorization", "Bearer " + token))
                .andExpect(jsonPath("$.saldoAtual").value(800.0));
        mvc.perform(get("/api/contas/" + carteira).header("Authorization", "Bearer " + token))
                .andExpect(jsonPath("$.saldoAtual").value(170.0));

        mvc.perform(post("/api/transferencias")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(Map.of("contaOrigemId", banco, "contaDestinoId", banco,
                                "valor", "10.00", "data", hoje))))
                .andExpect(status().isUnprocessableEntity());

        mvc.perform(delete("/api/contas/" + banco).header("Authorization", "Bearer " + token))
                .andExpect(status().isUnprocessableEntity());
    }

    @Test
    void recorrenciaCriaOsLancamentosDosMesesQueJaPassaram() throws Exception {
        String token = cadastrar(emailAleatorio());
        long moradia = idPorNome(token, "/api/categorias", "Moradia");
        long banco = idPorNome(token, "/api/contas", "Conta bancária");
        LocalDate inicio = LocalDate.now().minusMonths(2).withDayOfMonth(1);

        String resposta = mvc.perform(post("/api/recorrencias")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(Map.of("descricao", "Aluguel", "valor", "1000.00",
                                "dataInicio", inicio.toString(), "categoriaId", moradia, "contaId", banco))))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.diaDoMes").value(1))
                .andReturn().getResponse().getContentAsString();
        long recorrenciaId = objectMapper.readTree(resposta).get("id").asLong();

        mvc.perform(get("/api/resumo/historico").param("meses", "3").header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(3))
                .andExpect(jsonPath("$[0].totalDespesas").value(1000.0))
                .andExpect(jsonPath("$[1].totalDespesas").value(1000.0))
                .andExpect(jsonPath("$[2].totalDespesas").value(1000.0));

        mvc.perform(get("/api/transacoes").header("Authorization", "Bearer " + token))
                .andExpect(jsonPath("$[0].recorrente").value(true));

        mvc.perform(patch("/api/recorrencias/" + recorrenciaId)
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(Map.of("ativa", false))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.ativa").value(false))
                .andExpect(jsonPath("$.proximaData").doesNotExist());

        mvc.perform(delete("/api/recorrencias/" + recorrenciaId).header("Authorization", "Bearer " + token))
                .andExpect(status().isNoContent());
        mvc.perform(get("/api/transacoes").header("Authorization", "Bearer " + token))
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].recorrente").value(false));
    }

    @Test
    void historicoValidaQuantidadeDeMeses() throws Exception {
        String token = cadastrar(emailAleatorio());
        mvc.perform(get("/api/resumo/historico").param("meses", "99").header("Authorization", "Bearer " + token))
                .andExpect(status().isBadRequest());
    }

    @Test
    void metaCalculaProgressoENaoPermiteRetirarDemais() throws Exception {
        String token = cadastrar(emailAleatorio());
        String resposta = mvc.perform(post("/api/metas")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(Map.of("nome", "Notebook", "valorAlvo", "4000.00"))))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.percentual").value(0.0))
                .andReturn().getResponse().getContentAsString();
        long meta = objectMapper.readTree(resposta).get("id").asLong();

        mvc.perform(post("/api/metas/" + meta + "/movimentos")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(Map.of("valor", "1000.00"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.percentual").value(25.0))
                .andExpect(jsonPath("$.valorFaltante").value(3000.0))
                .andExpect(jsonPath("$.concluida").value(false));

        mvc.perform(post("/api/metas/" + meta + "/movimentos")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(Map.of("valor", "-2000.00"))))
                .andExpect(status().isUnprocessableEntity());
    }

    @Test
    void deveExportarCsvDoMes() throws Exception {
        String token = cadastrar(emailAleatorio());
        long lazer = idPorNome(token, "/api/categorias", "Lazer");
        long carteira = idPorNome(token, "/api/contas", "Carteira");
        criarLancamento(token, "Cinema; pipoca", "45.50", hoje, lazer, carteira);

        String csv = mvc.perform(get("/api/exportacoes/transacoes.csv").header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith("text/csv"))
                .andExpect(header().string("Content-Disposition",
                        org.hamcrest.Matchers.containsString("meu-bolso-")))
                .andReturn().getResponse().getContentAsString(java.nio.charset.StandardCharsets.UTF_8);

        assertThat(csv).contains("Data;Descrição;Categoria;Conta;Tipo;Valor");
        assertThat(csv).contains("\"Cinema; pipoca\";Lazer;Carteira;Despesa;45,50");
    }

    private String cadastrar(String email) throws Exception {
        String resposta = mvc.perform(post("/api/auth/cadastro")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(Map.of("nome", "Pessoa Teste", "email", email, "senha", "123456"))))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        return objectMapper.readTree(resposta).get("token").asText();
    }

    private long idPorNome(String token, String endpoint, String nome) throws Exception {
        String resposta = mvc.perform(get(endpoint).header("Authorization", "Bearer " + token))
                .andReturn().getResponse().getContentAsString();
        for (JsonNode item : objectMapper.readTree(resposta)) {
            if (item.get("nome").asText().equals(nome)) {
                return item.get("id").asLong();
            }
        }
        throw new IllegalStateException("Não encontrado: " + nome);
    }

    private long criarLancamento(String token, String descricao, String valor, String data,
                                 long categoriaId, long contaId) throws Exception {
        String resposta = mvc.perform(post("/api/transacoes")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(Map.of("descricao", descricao, "valor", valor,
                                "data", data, "categoriaId", categoriaId, "contaId", contaId))))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        return objectMapper.readTree(resposta).get("id").asLong();
    }

    private String json(Object objeto) throws Exception {
        return objectMapper.writeValueAsString(objeto);
    }

    private String emailAleatorio() {
        return "teste-" + UUID.randomUUID() + "@email.com";
    }
}
