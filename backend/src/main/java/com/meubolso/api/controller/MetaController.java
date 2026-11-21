package com.meubolso.api.controller;

import com.meubolso.api.dto.MetaDtos.MetaRequest;
import com.meubolso.api.dto.MetaDtos.MetaResponse;
import com.meubolso.api.dto.MetaDtos.MovimentoRequest;
import com.meubolso.api.security.UsuarioAutenticado;
import com.meubolso.api.service.MetaService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/metas")
@Tag(name = "Metas", description = "Metas de economia")
public class MetaController {

    private final MetaService metaService;

    public MetaController(MetaService metaService) {
        this.metaService = metaService;
    }

    @GetMapping
    @Operation(summary = "Lista as metas com progresso e valor mensal sugerido")
    public ResponseEntity<List<MetaResponse>> listar(
            @Parameter(hidden = true) @AuthenticationPrincipal UsuarioAutenticado usuario) {
        return ResponseEntity.ok(metaService.listar(usuario.id()));
    }

    @PostMapping
    @Operation(summary = "Cria uma meta")
    public ResponseEntity<MetaResponse> criar(
            @Valid @RequestBody MetaRequest request,
            @Parameter(hidden = true) @AuthenticationPrincipal UsuarioAutenticado usuario) {
        return ResponseEntity.status(HttpStatus.CREATED).body(metaService.criar(request, usuario.id()));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Atualiza nome, valor ou prazo da meta")
    public ResponseEntity<MetaResponse> atualizar(
            @PathVariable Long id,
            @Valid @RequestBody MetaRequest request,
            @Parameter(hidden = true) @AuthenticationPrincipal UsuarioAutenticado usuario) {
        return ResponseEntity.ok(metaService.atualizar(id, request, usuario.id()));
    }

    @PostMapping("/{id}/movimentos")
    @Operation(summary = "Guarda (valor positivo) ou retira (valor negativo) dinheiro da meta")
    public ResponseEntity<MetaResponse> movimentar(
            @PathVariable Long id,
            @Valid @RequestBody MovimentoRequest request,
            @Parameter(hidden = true) @AuthenticationPrincipal UsuarioAutenticado usuario) {
        return ResponseEntity.ok(metaService.movimentar(id, request.valor(), usuario.id()));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Exclui uma meta")
    public ResponseEntity<Void> excluir(
            @PathVariable Long id,
            @Parameter(hidden = true) @AuthenticationPrincipal UsuarioAutenticado usuario) {
        metaService.excluir(id, usuario.id());
        return ResponseEntity.noContent().build();
    }
}
