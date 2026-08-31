package br.com.portal.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.time.LocalDateTime;

@Entity @Table(name="usuarios", uniqueConstraints=@UniqueConstraint(name="uk_usuario_email", columnNames="email"))
public class Usuario {
 @Id @GeneratedValue(strategy=GenerationType.IDENTITY) private Long id;
 @NotBlank @Column(nullable=false,length=100) private String nome;
 @NotBlank @Email @Column(nullable=false,length=180) private String email;
 @JsonProperty(access=JsonProperty.Access.WRITE_ONLY) @Size(min=6,max=100) @Column(nullable=false,length=255) private String senhaHash;
 @Enumerated(EnumType.STRING) @Column(nullable=false,length=20) private Status status=Status.ATIVO;
 @Column(nullable=false) private LocalDateTime criadoEm;
 @Column(nullable=false) private LocalDateTime atualizadoEm;
 @PrePersist void pre(){criadoEm=LocalDateTime.now();atualizadoEm=criadoEm;}
 @PreUpdate void upd(){atualizadoEm=LocalDateTime.now();}
 public Long getId(){return id;} public String getNome(){return nome;} public String getEmail(){return email;} public String getSenhaHash(){return senhaHash;} public Status getStatus(){return status;} public LocalDateTime getCriadoEm(){return criadoEm;} public LocalDateTime getAtualizadoEm(){return atualizadoEm;}
 public void setId(Long v){id=v;} public void setNome(String v){nome=v;} public void setEmail(String v){email=v;} public void setSenhaHash(String v){senhaHash=v;} public void setStatus(Status v){status=v;}
}
