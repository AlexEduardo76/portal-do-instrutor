package br.senac.portalinstrutor.mapper;

import br.senac.portalinstrutor.dto.response.AvaliacaoComNotasResponse;
import br.senac.portalinstrutor.dto.response.AvaliacaoResponse;
import br.senac.portalinstrutor.dto.response.NotaAlunoResponse;
import br.senac.portalinstrutor.model.Avaliacao;
import br.senac.portalinstrutor.model.Turma;

import java.time.LocalDate;
import java.util.List;

public class AvaliacaoMapper {

    public static Avaliacao toEntity(String titulo, LocalDate data, Turma turma) {
        return new Avaliacao(titulo, data, turma);
    }

    public static AvaliacaoResponse toResponse(Avaliacao avaliacao) {
        return new AvaliacaoResponse(avaliacao.getId(), avaliacao.getTitulo(), avaliacao.getData());
    }

    public static AvaliacaoComNotasResponse toComNotasResponse(Avaliacao avaliacao, List<NotaAlunoResponse> notas) {
        return new AvaliacaoComNotasResponse(avaliacao.getId(), avaliacao.getTitulo(), avaliacao.getData(), notas);
    }
}
