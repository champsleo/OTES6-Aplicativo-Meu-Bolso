package com.meubolso.app.data.remote;

import com.meubolso.app.data.model.AuthResponse;
import com.meubolso.app.data.model.CadastroRequest;
import com.meubolso.app.data.model.Categoria;
import com.meubolso.app.data.model.CategoriaRequest;
import com.meubolso.app.data.model.Conta;
import com.meubolso.app.data.model.ContaRequest;
import com.meubolso.app.data.model.HistoricoMensal;
import com.meubolso.app.data.model.LoginRequest;
import com.meubolso.app.data.model.Meta;
import com.meubolso.app.data.model.MetaRequest;
import com.meubolso.app.data.model.MovimentoRequest;
import com.meubolso.app.data.model.Recorrencia;
import com.meubolso.app.data.model.RecorrenciaRequest;
import com.meubolso.app.data.model.ResumoMensal;
import com.meubolso.app.data.model.StatusRequest;
import com.meubolso.app.data.model.TipoTransacao;
import com.meubolso.app.data.model.Transacao;
import com.meubolso.app.data.model.TransacaoRequest;
import com.meubolso.app.data.model.Transferencia;
import com.meubolso.app.data.model.TransferenciaRequest;

import java.util.List;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.PATCH;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Path;
import retrofit2.http.Query;

// Parâmetros @Query nulos são omitidos da URL pelo Retrofit.
public interface ApiService {

    @POST("api/auth/login")
    Call<AuthResponse> login(@Body LoginRequest request);

    @POST("api/auth/cadastro")
    Call<AuthResponse> cadastrar(@Body CadastroRequest request);

    @GET("api/categorias")
    Call<List<Categoria>> listarCategorias(@Query("tipo") TipoTransacao tipo);

    @POST("api/categorias")
    Call<Categoria> criarCategoria(@Body CategoriaRequest request);

    @PUT("api/categorias/{id}")
    Call<Categoria> atualizarCategoria(@Path("id") long id, @Body CategoriaRequest request);

    @DELETE("api/categorias/{id}")
    Call<Void> excluirCategoria(@Path("id") long id);

    @GET("api/transacoes")
    Call<List<Transacao>> listarTransacoes(@Query("mes") String mes,
                                           @Query("tipo") TipoTransacao tipo,
                                           @Query("contaId") Long contaId,
                                           @Query("busca") String busca);

    @GET("api/transacoes/{id}")
    Call<Transacao> buscarTransacao(@Path("id") long id);

    @POST("api/transacoes")
    Call<Transacao> criarTransacao(@Body TransacaoRequest request);

    @PUT("api/transacoes/{id}")
    Call<Transacao> atualizarTransacao(@Path("id") long id, @Body TransacaoRequest request);

    @DELETE("api/transacoes/{id}")
    Call<Void> excluirTransacao(@Path("id") long id);

    @GET("api/resumo")
    Call<ResumoMensal> buscarResumo(@Query("mes") String mes);

    @GET("api/resumo/historico")
    Call<List<HistoricoMensal>> buscarHistorico(@Query("meses") int meses, @Query("ate") String ate);

    @GET("api/contas")
    Call<List<Conta>> listarContas();

    @POST("api/contas")
    Call<Conta> criarConta(@Body ContaRequest request);

    @PUT("api/contas/{id}")
    Call<Conta> atualizarConta(@Path("id") long id, @Body ContaRequest request);

    @DELETE("api/contas/{id}")
    Call<Void> excluirConta(@Path("id") long id);

    @GET("api/transferencias")
    Call<List<Transferencia>> listarTransferencias(@Query("mes") String mes);

    @POST("api/transferencias")
    Call<Transferencia> criarTransferencia(@Body TransferenciaRequest request);

    @DELETE("api/transferencias/{id}")
    Call<Void> excluirTransferencia(@Path("id") long id);

    @GET("api/recorrencias")
    Call<List<Recorrencia>> listarRecorrencias();

    @POST("api/recorrencias")
    Call<Recorrencia> criarRecorrencia(@Body RecorrenciaRequest request);

    @PATCH("api/recorrencias/{id}")
    Call<Recorrencia> alterarStatusRecorrencia(@Path("id") long id, @Body StatusRequest request);

    @DELETE("api/recorrencias/{id}")
    Call<Void> excluirRecorrencia(@Path("id") long id);

    @GET("api/metas")
    Call<List<Meta>> listarMetas();

    @POST("api/metas")
    Call<Meta> criarMeta(@Body MetaRequest request);

    @PUT("api/metas/{id}")
    Call<Meta> atualizarMeta(@Path("id") long id, @Body MetaRequest request);

    @POST("api/metas/{id}/movimentos")
    Call<Meta> movimentarMeta(@Path("id") long id, @Body MovimentoRequest request);

    @DELETE("api/metas/{id}")
    Call<Void> excluirMeta(@Path("id") long id);

    // Retorna o arquivo bruto (ResponseBody), não JSON.
    @GET("api/exportacoes/transacoes.csv")
    Call<ResponseBody> exportarCsv(@Query("mes") String mes);
}
