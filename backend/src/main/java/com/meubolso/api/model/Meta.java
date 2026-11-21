package com.meubolso.api.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "metas")
public class Meta {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 60)
    private String nome;

    @Column(name = "valor_alvo", nullable = false, precision = 12, scale = 2)
    private BigDecimal valorAlvo;

    @Column(name = "valor_guardado", nullable = false, precision = 12, scale = 2)
    private BigDecimal valorGuardado = BigDecimal.ZERO;

    private LocalDate prazo;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "usuario_id", nullable = false)
    private Usuario usuario;

    // Construtor vazio exigido pelo JPA.
    protected Meta() {
    }

    public Meta(String nome, BigDecimal valorAlvo, LocalDate prazo, Usuario usuario) {
        this.nome = nome;
        this.valorAlvo = valorAlvo;
        this.prazo = prazo;
        this.usuario = usuario;
    }

    public void atualizar(String nome, BigDecimal valorAlvo, LocalDate prazo) {
        this.nome = nome;
        this.valorAlvo = valorAlvo;
        this.prazo = prazo;
    }

    public void movimentar(BigDecimal valor) {
        this.valorGuardado = this.valorGuardado.add(valor);
    }

    public Long getId() {
        return id;
    }

    public String getNome() {
        return nome;
    }

    public BigDecimal getValorAlvo() {
        return valorAlvo;
    }

    public BigDecimal getValorGuardado() {
        return valorGuardado;
    }

    public LocalDate getPrazo() {
        return prazo;
    }

    public Usuario getUsuario() {
        return usuario;
    }
}
