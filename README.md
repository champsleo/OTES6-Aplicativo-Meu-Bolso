# Meu Bolso — Controle Financeiro Pessoal

Aplicativo Android nativo para organizar as finanças pessoais: registrar receitas e despesas, acompanhar o saldo do mês, ver para onde o dinheiro está indo e receber alertas quando o gasto de uma categoria passa do limite definido.

O projeto é dividido em duas partes que se comunicam por **API REST**:

| Parte | Tecnologia | Pasta |
|---|---|---|
| Front-end (app) | Android nativo em Java, Views XML, arquitetura MVVM | `android/` |
| Back-end (API) | Java 21 + Spring Boot 3, banco H2, autenticação JWT | `backend/` |

> **Equipe:** _(preencha com os nomes e matrículas)_
> **Vídeo de demonstração:** _(cole aqui o link)_

---

## 1. Funcionalidades

**Conta de usuário**
- Cadastro com nome, e-mail e senha (a senha é guardada criptografada com BCrypt).
- Login com token JWT; o app mantém a sessão aberta e volta ao login se o token expirar.
- Cada usuário só enxerga os próprios dados.

**Lançamentos (receitas e despesas)**
- Criar, editar e excluir lançamentos com descrição, valor, data, categoria e conta.
- Campo de valor com máscara de moeda (digita `1250` → mostra `12,50`).
- Busca por descrição (ignora maiúsculas e acentos), filtro por tipo e navegação entre meses.
- Excluir deslizando o item para o lado, com botão **Desfazer**.

**Lançamentos recorrentes**
- Ao criar um lançamento, a opção "Repetir todo mês" o transforma em recorrente (salário, aluguel, assinaturas).
- O servidor cria sozinho o lançamento de cada mês no dia certo (tarefa agendada com `@Scheduled`), sem duplicar.
- Tela para pausar, retomar ou excluir recorrências.

**Contas e cartões**
- Contas do tipo Carteira, Banco e Cartão de crédito, com saldo atual calculado pelo servidor.
- Transferências entre contas (não contam como receita nem despesa), com **Desfazer**.

**Resumo do mês**
- Saldo, total de receitas e total de despesas.
- Gráfico de rosca das despesas por categoria e gráfico de barras com a evolução dos últimos 6 meses (ambos desenhados com Canvas, sem bibliotecas).

**Categorias e limites de gastos**
- Cada conta nova já começa com 10 categorias e 2 contas prontas.
- Limite mensal por categoria de despesa, com barra de progresso (verde, laranja a partir de 80% e vermelha quando passa).
- **Notificação no celular** quando uma despesa faz a categoria chegar a 80% do limite ou passar dele (o servidor envia o aviso na resposta da API).

**Metas de economia**
- Ex.: "Viagem: juntar R$ 3.000 até julho". Guardar e retirar dinheiro, ver percentual, quanto falta e quanto guardar por mês para cumprir o prazo.

**Exportação**
- Planilha **CSV** do mês, gerada pelo servidor (abre no Excel com acentos e vírgula decimal).
- Relatório **PDF** do mês, montado no próprio celular. Os dois abrem a janela "Compartilhar" do Android.

**Visual**
- Modo escuro automático (segue o tema do celular), ícone próprio e tela de abertura (splash).

**Regras de negócio no servidor**
- O tipo do lançamento (receita/despesa) vem sempre da categoria escolhida.
- Não é possível excluir categoria ou conta em uso, nem ter duas com o mesmo nome.
- Apenas categorias de despesa podem ter limite mensal.
- Transferência para a mesma conta é proibida; não se pode retirar de uma meta mais do que foi guardado.
- Todos os cálculos (totais, saldos, percentuais, limites, metas) são feitos no back-end.

---

## 2. Arquitetura

```
┌──────────────────────── App Android (MVVM) ─────────────────────────┐
│ Activity/Fragment  →  ViewModel  →  Repository  →  Retrofit (HTTP)  │
│   (exibe a tela)     (estado e      (fonte única    + AuthInterceptor│
│                       validação)     de dados)        (envia o token)│
└───────────────────────────────────┬─────────────────────────────────┘
                                    │  JSON via HTTP (REST)
                                    ▼
┌────────────────────── Back-end Spring Boot ─────────────────────────┐
│ JwtAuthenticationFilter → Controller → Service → Repository → H2   │
│  (valida o token)         (HTTP)      (regras)   (Spring Data JPA)  │
│                 ApiExceptionHandler: erros em JSON padronizado       │
└─────────────────────────────────────────────────────────────────────┘
```

### Endpoints da API

Todos (menos `/api/auth/**`) exigem o cabeçalho `Authorization: Bearer <token>`.

| Método | Endpoint | Descrição | Sucesso |
|---|---|---|---|
| POST | `/api/auth/cadastro` | Cria conta e retorna token | 201 |
| POST | `/api/auth/login` | Faz login e retorna token | 200 |
| GET | `/api/usuarios/me` | Dados do usuário logado | 200 |
| GET | `/api/categorias?tipo=DESPESA` | Lista categorias (filtro opcional) | 200 |
| GET | `/api/categorias/{id}` | Busca uma categoria | 200 |
| POST | `/api/categorias` | Cria categoria | 201 |
| PUT | `/api/categorias/{id}` | Atualiza categoria | 200 |
| DELETE | `/api/categorias/{id}` | Exclui categoria | 204 |
| GET | `/api/transacoes?mes=2026-11&tipo=RECEITA&busca=mercado&contaId=2` | Lista lançamentos do mês (filtros opcionais) | 200 |
| GET | `/api/transacoes/{id}` | Busca um lançamento | 200 |
| POST | `/api/transacoes` | Cria lançamento | 201 |
| PUT | `/api/transacoes/{id}` | Atualiza lançamento | 200 |
| DELETE | `/api/transacoes/{id}` | Exclui lançamento | 204 |
| GET | `/api/resumo?mes=2026-11` | Totais, saldo e gastos por categoria | 200 |
| GET | `/api/resumo/historico?meses=6&ate=2026-11` | Receitas e despesas mês a mês | 200 |
| GET | `/api/contas` | Lista contas com saldo atual | 200 |
| GET / POST | `/api/contas`, `/api/contas/{id}` | Busca e cria conta | 200 / 201 |
| PUT / DELETE | `/api/contas/{id}` | Atualiza e exclui conta | 200 / 204 |
| GET | `/api/transferencias?mes=2026-11` | Transferências do mês | 200 |
| POST | `/api/transferencias` | Transfere entre contas | 201 |
| DELETE | `/api/transferencias/{id}` | Desfaz transferência | 204 |
| GET / POST | `/api/recorrencias` | Lista e cria recorrências | 200 / 201 |
| PATCH | `/api/recorrencias/{id}` | Pausa ou retoma (`{"ativa": false}`) | 200 |
| DELETE | `/api/recorrencias/{id}` | Exclui recorrência | 204 |
| GET / POST | `/api/metas` | Lista e cria metas | 200 / 201 |
| PUT / DELETE | `/api/metas/{id}` | Atualiza e exclui meta | 200 / 204 |
| POST | `/api/metas/{id}/movimentos` | Guarda (+) ou retira (−) dinheiro | 200 |
| GET | `/api/exportacoes/transacoes.csv?mes=2026-11` | Baixa o CSV do mês | 200 |

**Códigos de erro:** 400 (dados inválidos), 401 (sem login ou token expirado), 404 (não encontrado), 409 (conflito, por exemplo e-mail já cadastrado), 422 (regra de negócio violada).

Exemplo de erro:
```json
{ "status": 400, "erro": "Bad Request", "mensagem": "O valor deve ser maior que zero",
  "detalhes": ["valor: O valor deve ser maior que zero"], "timestamp": "2026-11-02T20:00:00Z" }
```

A documentação interativa (Swagger) fica em **http://localhost:8080/swagger-ui.html** com o back-end rodando.

---

## 3. Como executar

### Pré-requisitos
- **JDK 21** (pode ser baixado pelo próprio IntelliJ)
- **IntelliJ IDEA** (versão gratuita) ou outra IDE Java com Maven
- **Android Studio** com um emulador Android 8.0 (API 26) ou superior

### 3.1 Back-end
1. Abra a pasta `backend/` no IntelliJ IDEA e aguarde o Maven baixar as dependências.
2. Execute a classe `com.meubolso.api.MeuBolsoApplication`.
   - Pelo terminal (com Maven instalado): `mvn spring-boot:run`
3. A API sobe em `http://localhost:8080`. Os dados ficam salvos em `backend/data/`.
4. Na primeira execução é criado um usuário de demonstração com 6 meses de dados (contas, recorrências, metas e transferências):
   **e-mail:** `demo@meubolso.com` · **senha:** `123456`

**Testes automatizados:** clique com o botão direito na pasta `src/test/java` → *Run 'All Tests'*, ou rode `mvn test`.

**Configurações** (`backend/src/main/resources/application.properties`):
- `app.demo.habilitado=false` desliga os dados de demonstração.
- `JWT_SECRET` (variável de ambiente) substitui a chave de assinatura dos tokens.
- Para recomeçar o banco do zero, pare a API e apague a pasta `backend/data/`.

### 3.2 App Android
1. Abra a pasta `android/` no Android Studio e aguarde o *Gradle Sync*.
2. Com o back-end rodando, inicie um emulador e clique em **Run ▶**.
3. O app acessa a API em `http://10.0.2.2:8080/` (endereço do computador visto de dentro do emulador).
   - **Celular físico:** conecte o celular na mesma rede Wi-Fi do computador e troque o endereço em `app/src/main/res/values/config.xml` pelo IP do computador (ex.: `http://192.168.0.15:8080/`). Veja o IP com o comando `ipconfig` no Windows.

**Dependências do app** (em `app/build.gradle.kts`): Retrofit 2 + Gson, OkHttp Logging Interceptor, AndroidX Lifecycle (ViewModel/LiveData), RecyclerView, SwipeRefreshLayout, Core SplashScreen e Material Components. View Binding está ativado. O `targetSdk` é 36 (no Android 17, apps com `targetSdk` 37 precisam de uma permissão extra para acessar a rede local, onde roda o servidor de desenvolvimento).

---

## 4. Estrutura do código

```
backend/src/main/java/com/meubolso/api
├── config/        SecurityConfig, OpenApiConfig, GeralConfig, DadosDemonstracao
├── controller/    Auth, Usuario, Categoria, Transacao, Resumo, Conta,
│                  Transferencia, Recorrencia, Meta, Exportacao
├── dto/           Records de entrada/saída (o JSON da API)
├── exception/     Exceções de negócio + ApiExceptionHandler
├── model/         Entidades JPA: Usuario, Categoria, Transacao, Conta,
│                  Transferencia, Recorrencia, Meta
├── repository/    Interfaces Spring Data JPA
├── security/      JwtService, JwtAuthenticationFilter, UsuarioAutenticado
└── service/       Regras de negócio: Auth, Categoria, Transacao, Resumo, Conta,
                   Transferencia, Recorrencia (agendada), Meta, Exportacao

android/app/src/main/java/com/meubolso/app
├── data/model/        Classes que espelham o JSON da API
├── data/remote/       ApiService (endpoints Retrofit), ApiClient, AuthInterceptor
├── data/repository/   Repositórios + Resultado (carregando/sucesso/erro)
├── ui/auth/           Login e cadastro
├── ui/main/           Tela principal com menu inferior
├── ui/inicio/         Saldo e lista de lançamentos
├── ui/resumo/         Gráfico, limites e totais
├── ui/categorias/     Lista e diálogo de categorias
├── ui/contas/         Contas, transferências e seus diálogos
├── ui/metas/          Metas de economia e seus diálogos
├── ui/recorrencias/   Lançamentos recorrentes
├── ui/transacao/      Formulário de lançamento
└── util/              Sessão, formatação, máscara de moeda, notificações,
                       relatório PDF, compartilhamento de arquivos
```

## 5. Boas práticas adotadas

- **Separação em camadas** no back-end (Controller → Service → Repository) e **MVVM** no app (View → ViewModel → Repository).
- **DTOs** separados das entidades: a API nunca expõe a senha nem a estrutura interna do banco.
- **Validação** com Bean Validation no servidor e validação prévia no app.
- **Tratamento centralizado de erros** (`@RestControllerAdvice`) com status HTTP corretos e JSON padronizado.
- **Segurança:** senhas com BCrypt, autenticação stateless com JWT, isolamento dos dados por usuário.
- **Dinheiro com `BigDecimal`** (nunca `double`) para evitar erros de arredondamento.
- **Injeção de dependência** por construtor, sem `@Autowired` em atributos.
- **Testes automatizados:** teste unitário do cálculo do resumo (Mockito) e 14 testes de integração da API (MockMvc) cobrindo login, lançamentos, busca, limites, contas, transferências, recorrências, metas e exportação.
- **Datas testáveis:** os services recebem um `Clock` injetado em vez de chamar `LocalDate.now()` direto.
- **Documentação da API** gerada automaticamente com OpenAPI/Swagger.
- No app: **View Binding**, **ListAdapter + DiffUtil**, textos em `strings.xml`, ViewModels que sobrevivem à rotação da tela, eventos de mensagem entregues uma única vez e tratamento de estados de carregamento, erro e lista vazia.
