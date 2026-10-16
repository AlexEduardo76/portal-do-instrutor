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
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

@Entity
@Table(
    name = "alunos",
    uniqueConstraints = {
        @UniqueConstraint(name = "uk_aluno_turma_matricula", columnNames = {"turma_id", "matricula"}),
        @UniqueConstraint(name = "uk_aluno_turma_cpf", columnNames = {"turma_id", "cpf"})
    }
)
public class Aluno {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank
    @Size(min = 3, max = 120)
    @Column(name = "nome", nullable = false, length = 120)
    private String nome;

    @NotBlank
    @Size(max = 30)
    @Column(name = "matricula", nullable = false, length = 30)
    private String matricula;

    @Size(min = 11, max = 11)
    @Column(name = "cpf", length = 11)
    private String cpf;

    @NotNull
    @Column(name = "ativo", nullable = false)
    private Boolean ativo = true;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "turma_id", nullable = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    private Turma turma;

    public Aluno() {
    }

    public Aluno(String nome, String matricula, String cpf, Turma turma) {
        this.nome = nome;
        this.matricula = matricula;
        this.cpf = cpf;
        this.turma = turma;
        this.ativo = true;
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

    public String getMatricula() {
        return matricula;
    }

    public void setMatricula(String matricula) {
        this.matricula = matricula;
    }

    public String getCpf() {
        return cpf;
    }

    public void setCpf(String cpf) {
        this.cpf = cpf;
    }

    public Boolean getAtivo() {
        return ativo;
    }

    public void setAtivo(Boolean ativo) {
        this.ativo = ativo;
    }

    public Turma getTurma() {
        return turma;
    }

    public void setTurma(Turma turma) {
        this.turma = turma;
    }
}
