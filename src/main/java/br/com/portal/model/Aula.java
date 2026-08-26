package br.com.portal.model;

import jakarta.persistence.*; import java.time.LocalDate; import java.time.LocalDateTime;
@Entity @Table(name="aulas",uniqueConstraints=@UniqueConstraint(name="uk_aula_uc_numero",columnNames={"unidade_curricular_id","numero"}))
public class Aula {
 @Id @GeneratedValue(strategy=GenerationType.IDENTITY) private Long id;
 @ManyToOne(fetch=FetchType.LAZY,optional=false) @JoinColumn(name="unidade_curricular_id",nullable=false) private UnidadeCurricular unidadeCurricular;
 @Column(nullable=false) private Integer numero; @Column(nullable=false) private LocalDate data;
 @Column(length=500) private String observacao; @Column(nullable=false) private LocalDateTime criadoEm;
 @PrePersist void pre(){criadoEm=LocalDateTime.now();}
 public Long getId(){return id;} public UnidadeCurricular getUnidadeCurricular(){return unidadeCurricular;} public Integer getNumero(){return numero;} public LocalDate getData(){return data;} public String getObservacao(){return observacao;} public void setUnidadeCurricular(UnidadeCurricular v){unidadeCurricular=v;} public void setNumero(Integer v){numero=v;} public void setData(LocalDate v){data=v;} public void setObservacao(String v){observacao=v;}
}
