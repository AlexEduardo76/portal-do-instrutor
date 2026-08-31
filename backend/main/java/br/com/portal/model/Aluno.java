package br.com.portal.model;

import jakarta.persistence.*; import jakarta.validation.constraints.NotBlank; import java.time.LocalDate; import java.time.LocalDateTime;
@Entity @Table(name="alunos", uniqueConstraints=@UniqueConstraint(name="uk_aluno_matricula",columnNames="matricula"))
public class Aluno {
 @Id @GeneratedValue(strategy=GenerationType.IDENTITY) private Long id;
 @NotBlank @Column(nullable=false,length=120) private String nome;
 @NotBlank @Column(nullable=false,length=30) private String matricula;
 @ManyToOne(fetch=FetchType.LAZY,optional=false) @JoinColumn(name="turma_id",nullable=false) private Turma turma;
 @Enumerated(EnumType.STRING) @Column(nullable=false,length=20) private Status status=Status.ATIVO;
 private LocalDate dataNascimento;
 @Column(nullable=false) private LocalDateTime criadoEm; @Column(nullable=false) private LocalDateTime atualizadoEm;
 @PrePersist void pre(){criadoEm=LocalDateTime.now();atualizadoEm=criadoEm;} @PreUpdate void upd(){atualizadoEm=LocalDateTime.now();}
 public Long getId(){return id;} public String getNome(){return nome;} public String getMatricula(){return matricula;} public Turma getTurma(){return turma;} public Status getStatus(){return status;} public LocalDate getDataNascimento(){return dataNascimento;} public void setNome(String v){nome=v;} public void setMatricula(String v){matricula=v;} public void setTurma(Turma v){turma=v;} public void setStatus(Status v){status=v;} public void setDataNascimento(LocalDate v){dataNascimento=v;}
}
