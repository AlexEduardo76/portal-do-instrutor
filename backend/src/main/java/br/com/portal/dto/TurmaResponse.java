package br.com.portal.dto;

import br.com.portal.model.Turma;

import java.time.LocalDateTime;

public record TurmaResponse(
    Long id,
    String nome,
    String codigo,
    String status,
    LocalDateTime criadoEm,
    long totalAlunos,
    long totalUcs,
    int aulasRegistradas
) {
    public static TurmaResponse of(Turma turma, long totalAlunos, long totalUcs, int aulasRegistradas) {
        return new TurmaResponse(
            turma.getId(),
            turma.getNome(),
            turma.getCodigo(),
            turma.getStatus().name(),
            turma.getCriadoEm(),
            totalAlunos,
            totalUcs,
            aulasRegistradas
        );
    }
}
