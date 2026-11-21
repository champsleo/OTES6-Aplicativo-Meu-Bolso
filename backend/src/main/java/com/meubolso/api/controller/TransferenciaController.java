package com.meubolso.api.controller;

import com.meubolso.api.dto.TransferenciaDtos.TransferenciaRequest;
import com.meubolso.api.dto.TransferenciaDtos.TransferenciaResponse;
import com.meubolso.api.security.UsuarioAutenticado;
import com.meubolso.api.service.TransferenciaService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.YearMonth;
import java.util.List;

@RestController
@RequestMapping("/api/transferencias")
@Tag(name = "Transferências", description = "Movimentação de dinheiro entre contas")
public class TransferenciaController {

    private final TransferenciaService transferenciaService;

    public TransferenciaController(TransferenciaService transferenciaService) {
        this.transferenciaService = transferenciaService;
    }

    @GetMapping
    @Operation(summary = "Lista as transferências de um mês (ex.: ?mes=2026-11)")
    public ResponseEntity<List<TransferenciaResponse>> listar(
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM") YearMonth mes,
            @Parameter(hidden = true) @AuthenticationPrincipal UsuarioAutenticado usuario) {
        YearMonth mesConsultado = (mes != null) ? mes : YearMonth.now();
        return ResponseEntity.ok(transferenciaService.listar(usuario.id(), mesConsultado));
    }

    @PostMapping
    @Operation(summary = "Transfere um valor de uma conta para outra")
    public ResponseEntity<TransferenciaResponse> criar(
            @Valid @RequestBody TransferenciaRequest request,
            @Parameter(hidden = true) @AuthenticationPrincipal UsuarioAutenticado usuario) {
        return ResponseEntity.status(HttpStatus.CREATED).body(transferenciaService.criar(request, usuario.id()));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Desfaz (exclui) uma transferência")
    public ResponseEntity<Void> excluir(
            @PathVariable Long id,
            @Parameter(hidden = true) @AuthenticationPrincipal UsuarioAutenticado usuario) {
        transferenciaService.excluir(id, usuario.id());
        return ResponseEntity.noContent().build();
    }
}
