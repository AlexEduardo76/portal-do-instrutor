package br.senac.portalinstrutor.mapper;

import br.senac.portalinstrutor.dto.request.UnidadeCurricularRequest;
import br.senac.portalinstrutor.dto.response.UnidadeCurricularResponse;
import br.senac.portalinstrutor.model.Turma;
import br.senac.portalinstrutor.model.UnidadeCurricular;
import br.senac.portalinstrutor.util.TextoUtil;

public class UnidadeCurricularMapper {

    public static UnidadeCurricular toEntity(UnidadeCurricularRequest request, Turma turma) {
        return new UnidadeCurricular(TextoUtil.trim(request.nome()), request.cargaHoraria(), turma);
    }

    public static void updateEntity(UnidadeCurricular uc, UnidadeCurricularRequest request) {
        uc.setNome(TextoUtil.trim(request.nome()));
        uc.setCargaHoraria(request.cargaHoraria());
    }

    public static UnidadeCurricularResponse toResponse(UnidadeCurricular uc) {
        return new UnidadeCurricularResponse(uc.getId(), uc.getNome(), uc.getCargaHoraria());
    }
}
