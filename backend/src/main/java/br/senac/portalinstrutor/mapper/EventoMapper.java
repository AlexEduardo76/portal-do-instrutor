package br.senac.portalinstrutor.mapper;

import br.senac.portalinstrutor.dto.request.EventoRequest;
import br.senac.portalinstrutor.dto.response.EventoResponse;
import br.senac.portalinstrutor.model.Evento;
import br.senac.portalinstrutor.model.Turma;
import br.senac.portalinstrutor.util.TextoUtil;

public class EventoMapper {

    public static Evento toEntity(EventoRequest request, String instrutorLogin, Turma turma) {
        Evento evento = new Evento(
            instrutorLogin,
            TextoUtil.trim(request.titulo()),
            request.tipo(),
            request.data(),
            request.hora(),
            turma != null ? turma.getNome() : TextoUtil.trimParaNulo(request.turmaNome()),
            TextoUtil.trimParaNulo(request.descricao())
        );
        evento.setTurma(turma);
        return evento;
    }

    public static void updateEntity(Evento evento, EventoRequest request, Turma turma) {
        evento.setTitulo(TextoUtil.trim(request.titulo()));
        evento.setTipo(request.tipo());
        evento.setData(request.data());
        evento.setHora(request.hora());
        evento.setTurma(turma);
        evento.setTurmaNome(turma != null ? turma.getNome() : TextoUtil.trimParaNulo(request.turmaNome()));
        evento.setDescricao(TextoUtil.trimParaNulo(request.descricao()));
    }

    public static EventoResponse toResponse(Evento evento) {
        return new EventoResponse(
            evento.getId(),
            evento.getTitulo(),
            evento.getTipo(),
            evento.getData(),
            evento.getHora(),
            evento.getTurma() != null ? evento.getTurma().getId() : null,
            evento.getTurmaNome(),
            evento.getDescricao()
        );
    }
}
