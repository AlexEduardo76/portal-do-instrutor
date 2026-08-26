package br.com.portal.model;

import jakarta.persistence.*; import jakarta.validation.constraints.Min; import jakarta.validation.constraints.NotBlank; import jakarta.validation.constraints.NotNull; import java.time.LocalDateTime;
@Entity @Table(name="unidades_curriculares",uniqueConstraints=@UniqueConstraint(name="uk_uc_turma_nome",columnNames={"turma_id","nome"}))
public class UnidadeCurricular {
 @Id @GeneratedValue(strategy=GenerationType.IDENTITY) private Long id;
 @NotBlank @Column(nullable=false,length=120) private String nome;
 @NotNull @Min(1) @Column(nullable=false) private Integer totalAulas;
 @ManyToOne(fetch=FetchType.LAZY,optional=false) @JoinColumn(name="turma_id",nullable=false) private Turma turma;
 @Enumerated(EnumType.STRING) @Column(nullable=false,length=20) private Status status=Status.ATIVO;
 @Column(nullable=false) private LocalDateTime criadoEm; @PrePersist void pre(){criadoEm=LocalDateTime.now();}
 public Long getId(){return id;} public String getNome(){return nome;} public Integer getTotalAulas(){return totalAulas;} public Turma getTurma(){return turma;} public Status getStatus(){return status;} public void setNome(String v){nome=v;} public void setTotalAulas(Integer v){totalAulas=v;} public void setTurma(Turma v){turma=v;} public void setStatus(Status v){status=v;}
}
