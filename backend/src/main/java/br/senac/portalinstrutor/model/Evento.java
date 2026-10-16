package br.senac.portalinstrutor.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Enumerated;
import jakarta.persistence.EnumType;
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

import br.senac.portalinstrutor.model.enums.TipoEvento;

import java.time.LocalDate;
import java.time.LocalTime;

@Entity
@Table(name = "eventos")
public class Evento {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank
    @Size(max = 50)
    @Column(name = "instrutor_login", nullable = false, length = 50)
    private String instrutorLogin;

    @NotBlank
    @Size(min = 3, max = 100)
    @Column(name = "titulo", nullable = false, length = 100)
    private String titulo;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "tipo", nullable = false, length = 20)
    private TipoEvento tipo;

    @NotNull
    @Column(name = "data", nullable = false)
    private LocalDate data;

    @Column(name = "hora")
    private LocalTime hora;

    @Size(max = 60)
    @Column(name = "turma_nome", length = 60)
    private String turmaNome;

    @Size(max = 500)
    @Column(name = "descricao", length = 500)
    private String descricao;

    // Vínculo opcional com a turma (D2). Mantido turmaNome apenas por compatibilidade com o front.
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "turma_id")
    private Turma turma;

    public Evento() {
    }

    public Evento(String instrutorLogin, String titulo, TipoEvento tipo, LocalDate data,
                  LocalTime hora, String turmaNome, String descricao) {
        this.instrutorLogin = instrutorLogin;
        this.titulo = titulo;
        this.tipo = tipo;
        this.data = data;
        this.hora = hora;
        this.turmaNome = turmaNome;
        this.descricao = descricao;
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

    public String getTitulo() {
        return titulo;
    }

    public void setTitulo(String titulo) {
        this.titulo = titulo;
    }

    public TipoEvento getTipo() {
        return tipo;
    }

    public void setTipo(TipoEvento tipo) {
        this.tipo = tipo;
    }

    public LocalDate getData() {
        return data;
    }

    public void setData(LocalDate data) {
        this.data = data;
    }

    public LocalTime getHora() {
        return hora;
    }

    public void setHora(LocalTime hora) {
        this.hora = hora;
    }

    public String getTurmaNome() {
        return turmaNome;
    }

    public void setTurmaNome(String turmaNome) {
        this.turmaNome = turmaNome;
    }

    public String getDescricao() {
        return descricao;
    }

    public void setDescricao(String descricao) {
        this.descricao = descricao;
    }

    public Turma getTurma() {
        return turma;
    }

    public void setTurma(Turma turma) {
        this.turma = turma;
    }
}
