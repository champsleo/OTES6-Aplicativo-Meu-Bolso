package com.meubolso.api.controller;

import com.meubolso.api.dto.ResumoDtos.HistoricoMensal;
import com.meubolso.api.dto.ResumoDtos.ResumoMensalResponse;
import com.meubolso.api.security.UsuarioAutenticado;
import com.meubolso.api.service.ResumoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RestController;

import java.time.YearMonth;
import java.util.List;

@RestController
@Validated
@RequestMapping("/api/resumo")
@Tag(name = "Resumo", description = "Totais, saldo e gastos por categoria")
public class ResumoController {

    private final ResumoService resumoService;

    public ResumoController(ResumoService resumoService) {
        this.resumoService = resumoService;
    }

    @GetMapping
    @Operation(summary = "Resumo financeiro do mês (ex.: ?mes=2026-11)")
    public ResponseEntity<ResumoMensalResponse> resumo(
            @Parameter(description = "Mês no formato AAAA-MM. Se não informado, usa o mês atual.")
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM") YearMonth mes,
            @Parameter(hidden = true) @AuthenticationPrincipal UsuarioAutenticado usuario) {
        YearMonth mesConsultado = (mes != null) ? mes : YearMonth.now();
        return ResponseEntity.ok(resumoService.gerar(usuario.id(), mesConsultado));
    }

    @GetMapping("/historico")
    @Operation(summary = "Receitas, despesas e saldo dos últimos meses (ex.: ?meses=6&ate=2026-11)")
    public ResponseEntity<List<HistoricoMensal>> historico(
            @Parameter(description = "Quantidade de meses (1 a 24). Padrão: 6.")
            @RequestParam(defaultValue = "6") @Min(value = 1, message = "Mínimo de 1 mês")
            @Max(value = 24, message = "Máximo de 24 meses") int meses,
            @Parameter(description = "Último mês do período (AAAA-MM). Padrão: mês atual.")
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM") YearMonth ate,
            @Parameter(hidden = true) @AuthenticationPrincipal UsuarioAutenticado usuario) {
        YearMonth ultimoMes = (ate != null) ? ate : YearMonth.now();
        return ResponseEntity.ok(resumoService.historico(usuario.id(), ultimoMes, meses));
    }
}
