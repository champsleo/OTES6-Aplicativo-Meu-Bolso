package com.meubolso.api.repository;

import com.meubolso.api.model.TipoTransacao;
import com.meubolso.api.model.Transacao;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface TransacaoRepository extends JpaRepository<Transacao, Long> {

    List<Transacao> findByUsuarioIdAndDataBetweenOrderByDataDescIdDesc(
            Long usuarioId, LocalDate inicio, LocalDate fim);

    Optional<Transacao> findByIdAndUsuarioId(Long id, Long usuarioId);

    boolean existsByCategoriaId(Long categoriaId);

    boolean existsByContaId(Long contaId);

    // As somas retornam null quando não há lançamentos; quem chama trata como zero.
    @Query("select sum(t.valor) from Transacao t where t.conta.id = :contaId and t.tipo = :tipo")
    BigDecimal somarPorContaETipo(@Param("contaId") Long contaId, @Param("tipo") TipoTransacao tipo);

    @Query("select sum(t.valor) from Transacao t "
            + "where t.categoria.id = :categoriaId and t.data between :inicio and :fim")
    BigDecimal somarPorCategoriaNoPeriodo(@Param("categoriaId") Long categoriaId,
                                          @Param("inicio") LocalDate inicio,
                                          @Param("fim") LocalDate fim);

    // Ao excluir uma recorrência, os lançamentos já criados continuam no histórico; só perdem o vínculo.
    @Modifying
    @Query("update Transacao t set t.recorrencia = null where t.recorrencia.id = :recorrenciaId")
    int desvincularRecorrencia(@Param("recorrenciaId") Long recorrenciaId);
}
