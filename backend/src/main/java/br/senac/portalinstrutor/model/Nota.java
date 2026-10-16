package br.senac.portalinstrutor.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotNull;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

import java.math.BigDecimal;

@Entity
@Table(
    name = "notas",
    uniqueConstraints = @UniqueConstraint(name = "uk_nota_avaliacao_aluno", columnNames = {"avaliacao_id", "aluno_id"})
)
public class Nota {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "avaliacao_id", nullable = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    private Avaliacao avaliacao;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "aluno_id", nullable = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    private Aluno aluno;

    @Digits(integer = 3, fraction = 1)
    @DecimalMin(value = "0.0")
    @DecimalMax(value = "100.0")
    @Column(name = "valor", precision = 4, scale = 1)
    private BigDecimal valor;

    public Nota() {
    }

    public Nota(Avaliacao avaliacao, Aluno aluno, BigDecimal valor) {
        this.avaliacao = avaliacao;
        this.aluno = aluno;
        this.valor = valor;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Avaliacao getAvaliacao() {
        return avaliacao;
    }

    public void setAvaliacao(Avaliacao avaliacao) {
        this.avaliacao = avaliacao;
    }

    public Aluno getAluno() {
        return aluno;
    }

    public void setAluno(Aluno aluno) {
        this.aluno = aluno;
    }

    public BigDecimal getValor() {
        return valor;
    }

    public void setValor(BigDecimal valor) {
        this.valor = valor;
    }
}
