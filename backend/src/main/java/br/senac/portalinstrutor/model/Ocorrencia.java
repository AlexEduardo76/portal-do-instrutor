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
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "ocorrencias")
public class Ocorrencia {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "aluno_id", nullable = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    private Aluno aluno;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "turma_id", nullable = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    private Turma turma;

    @NotBlank
    @Size(max = 50)
    @Column(name = "instrutor_login", nullable = false, length = 50)
    private String instrutorLogin;

    @NotNull
    @Column(name = "data", nullable = false)
    private LocalDate data;

    @NotBlank
    @Size(min = 3, max = 2000)
    @Column(name = "descricao", nullable = false, length = 2000)
    private String descricao;

    @NotNull
    @Column(name = "criado_em", nullable = false)
    private LocalDateTime criadoEm;

    public Ocorrencia() {
    }

    public Ocorrencia(Aluno aluno, Turma turma, String instrutorLogin, LocalDate data, String descricao, LocalDateTime criadoEm) {
        this.aluno = aluno;
        this.turma = turma;
        this.instrutorLogin = instrutorLogin;
        this.data = data;
        this.descricao = descricao;
        this.criadoEm = criadoEm;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Aluno getAluno() {
        return aluno;
    }

    public void setAluno(Aluno aluno) {
        this.aluno = aluno;
    }

    public Turma getTurma() {
        return turma;
    }

    public void setTurma(Turma turma) {
        this.turma = turma;
    }

    public String getInstrutorLogin() {
        return instrutorLogin;
    }

    public void setInstrutorLogin(String instrutorLogin) {
        this.instrutorLogin = instrutorLogin;
    }

    public LocalDate getData() {
        return data;
    }

    public void setData(LocalDate data) {
        this.data = data;
    }

    public String getDescricao() {
        return descricao;
    }

    public void setDescricao(String descricao) {
        this.descricao = descricao;
    }

    public LocalDateTime getCriadoEm() {
        return criadoEm;
    }

    public void setCriadoEm(LocalDateTime criadoEm) {
        this.criadoEm = criadoEm;
    }
}
