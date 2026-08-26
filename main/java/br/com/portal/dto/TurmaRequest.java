package br.com.portal.dto; import jakarta.validation.constraints.NotBlank; public record TurmaRequest(@NotBlank(message="Nome da turma é obrigatório") String nome,String codigo) {}
