package br.com.portal.dto; import jakarta.validation.constraints.NotNull; public record PresencaRequest(@NotNull Long alunoId,@NotNull Boolean presente) {}
