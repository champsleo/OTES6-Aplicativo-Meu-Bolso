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
import java.time.YearMonth;

@Entity
@Table(name = "recorrencias")
public class Recorrencia {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    private String descricao;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal valor;

    @Column(name = "dia_do_mes", nullable = false)
    private int diaDoMes;

    @Column(name = "data_inicio", nullable = false)
    private LocalDate dataInicio;

    @Column(nullable = false)
    private boolean ativa = true;

    // Último mês já lançado (AAAA-MM). É o que impede lançar o mesmo mês duas vezes.
    @Column(name = "ultimo_mes_gerado", length = 7)
    private String ultimoMesGerado;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "categoria_id", nullable = false)
    private Categoria categoria;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "conta_id", nullable = false)
    private Conta conta;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "usuario_id", nullable = false)
    private Usuario usuario;

    // Construtor vazio exigido pelo JPA.
    protected Recorrencia() {
    }

    public Recorrencia(String descricao, BigDecimal valor, LocalDate dataInicio,
                       Categoria categoria, Conta conta, Usuario usuario) {
        this.descricao = descricao;
        this.valor = valor;
        this.dataInicio = dataInicio;
        this.diaDoMes = dataInicio.getDayOfMonth();
        this.categoria = categoria;
        this.conta = conta;
        this.usuario = usuario;
    }

    // Em meses mais curtos (ex.: dia 31 em fevereiro), usa o último dia do mês.
    public LocalDate dataNoMes(YearMonth mes) {
        return mes.atDay(Math.min(diaDoMes, mes.lengthOfMonth()));
    }

    public YearMonth getUltimoMesGerado() {
        return ultimoMesGerado == null ? null : YearMonth.parse(ultimoMesGerado);
    }

    public void marcarGerado(YearMonth mes) {
        this.ultimoMesGerado = mes.toString();
    }

    public void setAtiva(boolean ativa) {
        this.ativa = ativa;
    }

    public Long getId() {
        return id;
    }

    public String getDescricao() {
        return descricao;
    }

    public BigDecimal getValor() {
        return valor;
    }

    public int getDiaDoMes() {
        return diaDoMes;
    }

    public LocalDate getDataInicio() {
        return dataInicio;
    }

    public boolean isAtiva() {
        return ativa;
    }

    public Categoria getCategoria() {
        return categoria;
    }

    public Conta getConta() {
        return conta;
    }

    public Usuario getUsuario() {
        return usuario;
    }
}
