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
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

@Entity
@Table(
    name = "turmas",
    uniqueConstraints = @UniqueConstraint(name = "uk_turma_codigo_instrutor", columnNames = {"codigo", "instrutor_login"})
)
public class Turma {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank
    @Size(min = 3, max = 100)
    @Column(name = "nome", nullable = false, length = 100)
    private String nome;

    @Size(max = 30)
    @Column(name = "codigo", length = 30)
    private String codigo;

    @Size(max = 100)
    @Column(name = "curso", length = 100)
    private String curso;

    @Size(max = 20)
    @Column(name = "turno", length = 20)
    private String turno;

    @Size(max = 30)
    @Column(name = "sala", length = 30)
    private String sala;

    @Min(0)
    @Max(9999)
    @Column(name = "qtd_alunos")
    private Integer qtdAlunos;

    @Min(0)
    @Max(9999)
    @Column(name = "qtd_ucs")
    private Integer qtdUcs;

    @Min(0)
    @Max(9999)
    @Column(name = "horas")
    private Integer horas;

    @NotNull
    @Column(name = "ativa", nullable = false)
    private Boolean ativa = true;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "instrutor_login", referencedColumnName = "login", nullable = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    private Usuario instrutor;

    public Turma() {
    }

    public Turma(String nome, String codigo, String curso, String turno, String sala,
                 Integer qtdAlunos, Integer qtdUcs, Integer horas, Usuario instrutor) {
        this.nome = nome;
        this.codigo = codigo;
        this.curso = curso;
        this.turno = turno;
        this.sala = sala;
        this.qtdAlunos = qtdAlunos;
        this.qtdUcs = qtdUcs;
        this.horas = horas;
        this.instrutor = instrutor;
        this.ativa = true;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public String getCodigo() {
        return codigo;
    }

    public void setCodigo(String codigo) {
        this.codigo = codigo;
    }

    public String getCurso() {
        return curso;
    }

    public void setCurso(String curso) {
        this.curso = curso;
    }

    public String getTurno() {
        return turno;
    }

    public void setTurno(String turno) {
        this.turno = turno;
    }

    public String getSala() {
        return sala;
    }

    public void setSala(String sala) {
        this.sala = sala;
    }

    public Integer getQtdAlunos() {
        return qtdAlunos;
    }

    public void setQtdAlunos(Integer qtdAlunos) {
        this.qtdAlunos = qtdAlunos;
    }

    public Integer getQtdUcs() {
        return qtdUcs;
    }

    public void setQtdUcs(Integer qtdUcs) {
        this.qtdUcs = qtdUcs;
    }

    public Integer getHoras() {
        return horas;
    }

    public void setHoras(Integer horas) {
        this.horas = horas;
    }

    public Boolean getAtiva() {
        return ativa;
    }

    public void setAtiva(Boolean ativa) {
        this.ativa = ativa;
    }

    public Usuario getInstrutor() {
        return instrutor;
    }

    public void setInstrutor(Usuario instrutor) {
        this.instrutor = instrutor;
    }
}
