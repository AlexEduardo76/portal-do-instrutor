package br.senac.portalinstrutor.mapper;

import br.senac.portalinstrutor.dto.response.FrequenciaAlunoResponse;
import br.senac.portalinstrutor.dto.response.FrequenciaResponse;
import br.senac.portalinstrutor.repository.FrequenciaRepository;

import java.util.List;

public class FrequenciaMapper {

    public static FrequenciaResponse toResponse(List<FrequenciaRepository.FrequenciaAlunoProjection> projections) {
        boolean jaRegistrada = projections.stream().anyMatch(p -> p.getPresente() != null);
        List<FrequenciaAlunoResponse> alunos = projections.stream()
            .map(p -> new FrequenciaAlunoResponse(
                p.getAlunoId(),
                p.getNome(),
                p.getMatricula(),
                p.getPresente()
            ))
            .toList();
        return new FrequenciaResponse(jaRegistrada, alunos);
    }
}
