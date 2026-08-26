package br.com.portal.dto;

import br.com.portal.model.UnidadeCurricular;

public record UCResponse(
    Long id,
    String nome,
    int totalAulas,
    int aulasRegistradas,
    int aulasRestantes,
    boolean concluida,
    String status
) {
    public static UCResponse of(UnidadeCurricular uc, int aulasRegistradas) {
        int restantes = Math.max(0, uc.getTotalAulas() - aulasRegistradas);
        return new UCResponse(
            uc.getId(),
            uc.getNome(),
            uc.getTotalAulas(),
            aulasRegistradas,
            restantes,
            aulasRegistradas >= uc.getTotalAulas(),
            uc.getStatus().name()
        );
    }
}
