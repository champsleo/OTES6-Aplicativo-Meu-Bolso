package com.meubolso.api.controller;

import com.meubolso.api.dto.ContaDtos.ContaRequest;
import com.meubolso.api.dto.ContaDtos.ContaResponse;
import com.meubolso.api.security.UsuarioAutenticado;
import com.meubolso.api.service.ContaService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
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
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/api/contas")
@Tag(name = "Contas", description = "Carteira, contas bancárias e cartões, com saldo calculado")
public class ContaController {

    private final ContaService contaService;

    public ContaController(ContaService contaService) {
        this.contaService = contaService;
    }

    @GetMapping
    @Operation(summary = "Lista as contas com o saldo atual de cada uma")
    public ResponseEntity<List<ContaResponse>> listar(
            @Parameter(hidden = true) @AuthenticationPrincipal UsuarioAutenticado usuario) {
        return ResponseEntity.ok(contaService.listar(usuario.id()));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Busca uma conta pelo id")
    public ResponseEntity<ContaResponse> buscar(
            @PathVariable Long id,
            @Parameter(hidden = true) @AuthenticationPrincipal UsuarioAutenticado usuario) {
        return ResponseEntity.ok(contaService.buscar(id, usuario.id()));
    }

    @PostMapping
    @Operation(summary = "Cria uma conta")
    public ResponseEntity<ContaResponse> criar(
            @Valid @RequestBody ContaRequest request,
            @Parameter(hidden = true) @AuthenticationPrincipal UsuarioAutenticado usuario) {
        ContaResponse criada = contaService.criar(request, usuario.id());
        URI local = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}").buildAndExpand(criada.id()).toUri();
        return ResponseEntity.created(local).body(criada);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Atualiza uma conta")
    public ResponseEntity<ContaResponse> atualizar(
            @PathVariable Long id,
            @Valid @RequestBody ContaRequest request,
            @Parameter(hidden = true) @AuthenticationPrincipal UsuarioAutenticado usuario) {
        return ResponseEntity.ok(contaService.atualizar(id, request, usuario.id()));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Exclui uma conta (somente se não estiver em uso)")
    public ResponseEntity<Void> excluir(
            @PathVariable Long id,
            @Parameter(hidden = true) @AuthenticationPrincipal UsuarioAutenticado usuario) {
        contaService.excluir(id, usuario.id());
        return ResponseEntity.noContent().build();
    }
}
