package com.meubolso.api.controller;

import com.meubolso.api.security.UsuarioAutenticado;
import com.meubolso.api.service.ExportacaoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.nio.charset.StandardCharsets;
import java.time.YearMonth;

@RestController
@RequestMapping("/api/exportacoes")
@Tag(name = "Exportação", description = "Download dos lançamentos em CSV")
public class ExportacaoController {

    private static final MediaType TEXT_CSV = new MediaType("text", "csv", StandardCharsets.UTF_8);

    private final ExportacaoService exportacaoService;

    public ExportacaoController(ExportacaoService exportacaoService) {
        this.exportacaoService = exportacaoService;
    }

    @GetMapping("/transacoes.csv")
    @Operation(summary = "Baixa os lançamentos do mês em CSV (abre no Excel ou Planilhas Google)")
    public ResponseEntity<byte[]> transacoesCsv(
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM") YearMonth mes,
            @Parameter(hidden = true) @AuthenticationPrincipal UsuarioAutenticado usuario) {
        YearMonth mesConsultado = (mes != null) ? mes : YearMonth.now();
        byte[] conteudo = exportacaoService.gerarCsv(usuario.id(), mesConsultado).getBytes(StandardCharsets.UTF_8);

        String nomeArquivo = "meu-bolso-" + mesConsultado + ".csv";
        return ResponseEntity.ok()
                .contentType(TEXT_CSV)
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        ContentDisposition.attachment().filename(nomeArquivo).build().toString())
                .body(conteudo);
    }
}
