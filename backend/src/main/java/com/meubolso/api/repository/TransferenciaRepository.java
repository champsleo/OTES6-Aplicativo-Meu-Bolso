package com.meubolso.api.repository;

import com.meubolso.api.model.Transferencia;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface TransferenciaRepository extends JpaRepository<Transferencia, Long> {

    List<Transferencia> findByUsuarioIdAndDataBetweenOrderByDataDescIdDesc(
            Long usuarioId, LocalDate inicio, LocalDate fim);

    Optional<Transferencia> findByIdAndUsuarioId(Long id, Long usuarioId);

    boolean existsByContaOrigemIdOrContaDestinoId(Long contaOrigemId, Long contaDestinoId);

    @Query("select sum(t.valor) from Transferencia t where t.contaDestino.id = :contaId")
    BigDecimal somarEntradas(@Param("contaId") Long contaId);

    @Query("select sum(t.valor) from Transferencia t where t.contaOrigem.id = :contaId")
    BigDecimal somarSaidas(@Param("contaId") Long contaId);
}
