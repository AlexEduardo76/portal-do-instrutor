package br.senac.portalinstrutor.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDateTime;

@Entity
@Table(name = "atividades")
public class Atividade {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank
    @Size(max = 50)
    @Column(name = "instrutor_login", nullable = false, length = 50)
    private String instrutorLogin;

    @NotBlank
    @Size(min = 3, max = 500)
    @Column(name = "descricao", nullable = false, length = 500)
    private String descricao;

    @NotNull
    @Column(name = "criado_em", nullable = false)
    private LocalDateTime criadoEm;

    public Atividade() {
    }

    public Atividade(String instrutorLogin, String descricao, LocalDateTime criadoEm) {
        this.instrutorLogin = instrutorLogin;
        this.descricao = descricao;
        this.criadoEm = criadoEm;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getInstrutorLogin() {
        return instrutorLogin;
    }

    public void setInstrutorLogin(String instrutorLogin) {
        this.instrutorLogin = instrutorLogin;
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
