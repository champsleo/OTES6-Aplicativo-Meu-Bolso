package com.meubolso.api.controller;

import com.meubolso.api.dto.RecorrenciaDtos.RecorrenciaRequest;
import com.meubolso.api.dto.RecorrenciaDtos.RecorrenciaResponse;
import com.meubolso.api.dto.RecorrenciaDtos.StatusRequest;
import com.meubolso.api.security.UsuarioAutenticado;
import com.meubolso.api.service.RecorrenciaService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/recorrencias")
@Tag(name = "Recorrências", description = "Lançamentos que se repetem todo mês")
public class RecorrenciaController {

    private final RecorrenciaService recorrenciaService;

    public RecorrenciaController(RecorrenciaService recorrenciaService) {
        this.recorrenciaService = recorrenciaService;
    }

    @GetMapping
    @Operation(summary = "Lista as recorrências, com a data do próximo lançamento")
    public ResponseEntity<List<RecorrenciaResponse>> listar(
            @Parameter(hidden = true) @AuthenticationPrincipal UsuarioAutenticado usuario) {
        return ResponseEntity.ok(recorrenciaService.listar(usuario.id()));
    }

    @PostMapping
    @Operation(summary = "Cria uma recorrência (lançamentos já vencidos são criados na hora)")
    public ResponseEntity<RecorrenciaResponse> criar(
            @Valid @RequestBody RecorrenciaRequest request,
            @Parameter(hidden = true) @AuthenticationPrincipal UsuarioAutenticado usuario) {
        return ResponseEntity.status(HttpStatus.CREATED).body(recorrenciaService.criar(request, usuario.id()));
    }

    @PatchMapping("/{id}")
    @Operation(summary = "Pausa ou retoma uma recorrência (corpo: {\"ativa\": false})")
    public ResponseEntity<RecorrenciaResponse> alterarStatus(
            @PathVariable Long id,
            @Valid @RequestBody StatusRequest request,
            @Parameter(hidden = true) @AuthenticationPrincipal UsuarioAutenticado usuario) {
        return ResponseEntity.ok(recorrenciaService.alterarStatus(id, request.ativa(), usuario.id()));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Exclui a recorrência (os lançamentos já criados continuam)")
    public ResponseEntity<Void> excluir(
            @PathVariable Long id,
            @Parameter(hidden = true) @AuthenticationPrincipal UsuarioAutenticado usuario) {
        recorrenciaService.excluir(id, usuario.id());
        return ResponseEntity.noContent().build();
    }
}
