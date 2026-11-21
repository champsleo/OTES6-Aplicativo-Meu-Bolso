package com.meubolso.api.repository;

import com.meubolso.api.model.Recorrencia;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface RecorrenciaRepository extends JpaRepository<Recorrencia, Long> {

    List<Recorrencia> findByUsuarioIdOrderByDiaDoMesAscDescricaoAsc(Long usuarioId);

    List<Recorrencia> findByAtivaTrue();

    Optional<Recorrencia> findByIdAndUsuarioId(Long id, Long usuarioId);

    boolean existsByContaId(Long contaId);

    boolean existsByCategoriaId(Long categoriaId);
}
