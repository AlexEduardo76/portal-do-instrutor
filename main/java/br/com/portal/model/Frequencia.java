package br.com.portal.model;

import jakarta.persistence.*; import java.time.LocalDateTime;
@Entity @Table(name="frequencias",uniqueConstraints=@UniqueConstraint(name="uk_frequencia_aula_aluno",columnNames={"aula_id","aluno_id"}))
public class Frequencia {
 @Id @GeneratedValue(strategy=GenerationType.IDENTITY) private Long id;
 @ManyToOne(fetch=FetchType.LAZY,optional=false) @JoinColumn(name="aula_id",nullable=false) private Aula aula;
 @ManyToOne(fetch=FetchType.LAZY,optional=false) @JoinColumn(name="aluno_id",nullable=false) private Aluno aluno;
 @Column(nullable=false) private boolean presente;
 @Column(nullable=false) private LocalDateTime registradoEm;
 @PrePersist void pre(){registradoEm=LocalDateTime.now();}
 public Long getId(){return id;} public Aula getAula(){return aula;} public Aluno getAluno(){return aluno;} public boolean isPresente(){return presente;} public void setAula(Aula v){aula=v;} public void setAluno(Aluno v){aluno=v;} public void setPresente(boolean v){presente=v;}
}
