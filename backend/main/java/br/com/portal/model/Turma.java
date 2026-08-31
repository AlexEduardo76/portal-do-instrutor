package br.com.portal.model;

import jakarta.persistence.*; import jakarta.validation.constraints.NotBlank; import java.time.LocalDateTime;
@Entity @Table(name="turmas", uniqueConstraints=@UniqueConstraint(name="uk_turma_instrutor_codigo", columnNames={"instrutor_id","codigo"}))
public class Turma {
 @Id @GeneratedValue(strategy=GenerationType.IDENTITY) private Long id;
 @NotBlank @Column(nullable=false,length=100) private String nome;
 @Column(nullable=false,length=30) private String codigo;
 @ManyToOne(fetch=FetchType.LAZY,optional=false) @JoinColumn(name="instrutor_id",nullable=false) private Usuario instrutor;
 @Enumerated(EnumType.STRING) @Column(nullable=false,length=20) private Status status=Status.ATIVO;
 @Column(nullable=false) private LocalDateTime criadoEm; @Column(nullable=false) private LocalDateTime atualizadoEm;
 @PrePersist void pre(){criadoEm=LocalDateTime.now();atualizadoEm=criadoEm;} @PreUpdate void upd(){atualizadoEm=LocalDateTime.now();}
 public Long getId(){return id;} public String getNome(){return nome;} public String getCodigo(){return codigo;} public Usuario getInstrutor(){return instrutor;} public Status getStatus(){return status;} public LocalDateTime getCriadoEm(){return criadoEm;} public void setNome(String v){nome=v;} public void setCodigo(String v){codigo=v;} public void setInstrutor(Usuario v){instrutor=v;} public void setStatus(Status v){status=v;}
}
