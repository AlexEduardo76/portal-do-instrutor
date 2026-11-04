import { useEffect, useMemo, useRef, useState } from "react";
import AppShell from "../components/AppShell";
import Icon from "../components/Icon";
import PageHeader from "../components/PageHeader";
import Sheet from "../components/Sheet";
import { useConfirm } from "../components/ConfirmDialog";
import { EmptyState } from "../components/StateViews";
import { api } from "../lib/api";
import { useToast } from "../lib/toast";
import { hojeISO } from "../lib/utils";

const TIPOS = {
  AULA: "Aula",
  CONTEUDO: "Conteúdo novo",
  PROVA: "Prova / Avaliação",
  ENTREGA: "Entrega de trabalho",
  FERIADO: "Feriado",
  RECESSO: "Recesso",
  REUNIAO: "Reunião",
  ANOTACAO: "Anotação",
  OUTRO: "Outro"
};

const pad = (n) => String(n).padStart(2, "0");
const iso = (d) =>
  `${d.getFullYear()}-${pad(d.getMonth() + 1)}-${pad(d.getDate())}`;
const deISO = (s) => {
  const [a, m, d] = s.split("-").map(Number);
  return new Date(a, m - 1, d);
};
const maiuscula = (t) => t.charAt(0).toUpperCase() + t.slice(1);
const tipoSeguro = (t) => (TIPOS[t] ? t : "OUTRO");

function intervaloGrade(ano, mes) {
  const deslocamento = (new Date(ano, mes, 1).getDay() + 6) % 7;
  const diasNoMes = new Date(ano, mes + 1, 0).getDate();
  const semanas = Math.ceil((deslocamento + diasNoMes) / 7);
  const inicio = new Date(ano, mes, 1 - deslocamento);
  const total = semanas * 7;
  return { inicio, total };
}

export default function Calendario() {
  const { toast } = useToast();
  const confirmar = useConfirm();

  const hojeData = new Date();
  const [ano, setAno] = useState(hojeData.getFullYear());
  const [mes, setMes] = useState(hojeData.getMonth());
  const [selecionada, setSelecionada] = useState(hojeISO());
  const [turmas, setTurmas] = useState([]);
  const [eventos, setEventos] = useState([]);
  const [aulas, setAulas] = useState([]);
  const [versao, setVersao] = useState(0);
  const [modal, setModal] = useState(null);
  const [formEvento, setFormEvento] = useState({
    titulo: "",
    tipo: "AULA",
    data: hojeISO(),
    hora: "",
    turma: "",
    descricao: ""
  });
  const [salvandoEvento, setSalvandoEvento] = useState(false);
  const [anotacao, setAnotacao] = useState("");
  const cargaAtual = useRef(0);

  useEffect(() => {
    let ativo = true;
    api
      .get("/turmas/resumo")
      .then((resultado) => {
        if (ativo) setTurmas(resultado);
      })
      .catch((err) => {
        console.warn("Não foi possível carregar as turmas:", err);
      });
    return () => {
      ativo = false;
    };
  }, []);

  useEffect(() => {
    const token = ++cargaAtual.current;
    let ativo = true;
    const { inicio, total } = intervaloGrade(ano, mes);
    const fim = new Date(
      inicio.getFullYear(),
      inicio.getMonth(),
      inicio.getDate() + total
    );
    const ids = turmas.map((t) => t.id);

    const buscaEventos = api.get(
      `/eventos?inicio=${iso(inicio)}&fim=${iso(
        new Date(fim.getFullYear(), fim.getMonth(), fim.getDate() - 1)
      )}`
    );
    const buscaAulas = ids.length
      ? api.get(
          `/aulas?inicio=${inicio.toISOString()}&fim=${fim.toISOString()}`
        )
      : Promise.resolve([]);

    Promise.allSettled([buscaEventos, buscaAulas]).then(
      ([resEventos, resAulas]) => {
        if (!ativo || token !== cargaAtual.current) return;

        if (resEventos.status === "fulfilled") {
          setEventos(resEventos.value);
        } else {
          setEventos([]);
          console.error(resEventos.reason);
          toast("Não foi possível carregar os eventos.", "erro");
        }

        setAulas(resAulas.status === "fulfilled" ? resAulas.value : []);
        if (resAulas.status === "rejected") {
          console.error(resAulas.reason);
        }
      }
    );

    return () => {
      ativo = false;
    };
  }, [ano, mes, turmas, versao, toast]);

  const porDia = useMemo(() => {
    const mapa = {};
    const adicionar = (dataISO, item) => {
      if (!mapa[dataISO]) mapa[dataISO] = [];
      mapa[dataISO].push(item);
    };

    eventos.forEach((evento) =>
      adicionar(evento.data, {
        origem: "evento",
        id: evento.id,
        titulo: evento.titulo,
        tipo: tipoSeguro(evento.tipo),
        hora: evento.hora ? evento.hora.slice(0, 5) : "",
        turma: evento.turmaNome || "",
        descricao: evento.descricao || ""
      })
    );

    const nomeTurma = Object.fromEntries(turmas.map((t) => [t.id, t.nome]));
    aulas.forEach((aula) => {
      const d = new Date(aula.dataHora);
      adicionar(iso(d), {
        origem: "aula",
        titulo: aula.titulo,
        tipo: "AULA",
        hora: `${pad(d.getHours())}:${pad(d.getMinutes())}`,
        turma: nomeTurma[aula.turmaId] || "",
        descricao: aula.sala ? `Sala ${aula.sala}` : ""
      });
    });

    Object.values(mapa).forEach((lista) =>
      lista.sort((a, b) => a.hora.localeCompare(b.hora))
    );
    return mapa;
  }, [eventos, aulas, turmas]);

  function irPara(dataISO) {
    const d = deISO(dataISO);
    setSelecionada(dataISO);
    if (d.getFullYear() !== ano || d.getMonth() !== mes) {
      setAno(d.getFullYear());
      setMes(d.getMonth());
    }
  }

  function mudarMes(delta) {
    const d = new Date(ano, mes + delta, 1);
    const hoje = hojeISO();
    const primeiro = iso(d);
    irPara(hoje.slice(0, 7) === primeiro.slice(0, 7) ? hoje : primeiro);
  }

  function abrirModal({ evento = null, data = selecionada } = {}) {
    setFormEvento({
      titulo: evento?.titulo || "",
      tipo: evento ? tipoSeguro(evento.tipo) : "AULA",
      data: evento?.data || data,
      hora: evento?.hora ? evento.hora.slice(0, 5) : "",
      turma: evento?.turmaNome || "",
      descricao: evento?.descricao || ""
    });
    setModal({ evento, data });
  }

  function fecharModal() {
    setModal(null);
  }

  async function salvarEvento(evento) {
    evento.preventDefault();

    const dados = {
      titulo: formEvento.titulo.trim(),
      tipo: formEvento.tipo,
      data: formEvento.data,
      hora: formEvento.hora || null,
      turmaNome: formEvento.turma.trim() || null,
      descricao: formEvento.descricao.trim() || null
    };

    if (!dados.titulo) {
      toast("Informe o título do evento.", "erro");
      return;
    }
    if (dados.titulo.length < 3) {
      toast("O título precisa ter pelo menos 3 caracteres.", "erro");
      return;
    }
    if (!dados.data) {
      toast("Informe a data do evento.", "erro");
      return;
    }

    setSalvandoEvento(true);
    try {
      if (modal?.evento) {
        await api.put(`/eventos/${modal.evento.id}`, dados);
        toast("Evento atualizado.", "sucesso");
      } else {
        await api.post("/eventos", dados);
        toast("Evento criado.", "sucesso");
      }
      fecharModal();
      irPara(dados.data);
      setVersao((v) => v + 1);
    } catch (err) {
      console.error("Erro ao salvar evento:", err);
      toast(err.message || "Não foi possível salvar o evento.", "erro");
    } finally {
      setSalvandoEvento(false);
    }
  }

  async function excluirEvento() {
    if (!modal?.evento) return;
    const evento = eventos.find(
      (x) => String(x.id) === String(modal.evento.id)
    );

    const confirmado = await confirmar({
      titulo: "Excluir evento?",
      mensagem: `O evento "${evento?.titulo || ""}" será excluído.`,
      detalhe: "Esta ação não pode ser desfeita.",
      confirmarTexto: "Excluir evento",
      perigo: true
    });
    if (!confirmado) return;

    try {
      await api.delete(`/eventos/${modal.evento.id}`);
      toast("Evento excluído.", "sucesso");
      fecharModal();
      setVersao((v) => v + 1);
    } catch (err) {
      console.error("Erro ao excluir evento:", err);
      toast(err.message || "Não foi possível excluir o evento.", "erro");
    }
  }

  async function salvarAnotacao(evento) {
    evento.preventDefault();
    const titulo = anotacao.trim();
    if (!titulo) return;
    if (titulo.length < 3) {
      toast("A anotação precisa ter pelo menos 3 caracteres.", "erro");
      return;
    }

    try {
      await api.post("/eventos", {
        titulo,
        tipo: "ANOTACAO",
        data: selecionada
      });
      setAnotacao("");
      setVersao((v) => v + 1);
      toast("Anotação salva.", "sucesso");
    } catch (err) {
      console.error("Erro ao salvar anotação:", err);
      toast(err.message || "Não foi possível salvar a anotação.", "erro");
    }
  }

  const { inicio, total } = intervaloGrade(ano, mes);
  const hoje = hojeISO();
  const celulas = [];

  for (let i = 0; i < total; i++) {
    const d = new Date(
      inicio.getFullYear(),
      inicio.getMonth(),
      inicio.getDate() + i
    );
    const dataISO = iso(d);
    const itens = porDia[dataISO] || [];
    const tipos = [...new Set(itens.map((item) => item.tipo))].slice(0, 3);

    const classes = ["cal-cell"];
    if (d.getMonth() !== mes) classes.push("cal-cell--fora");
    if (i % 7 >= 5) classes.push("cal-cell--fds");
    if (dataISO === hoje) classes.push("cal-cell--hoje");
    if (dataISO === selecionada) classes.push("cal-cell--sel");

    celulas.push(
      <button
        type="button"
        className={classes.join(" ")}
        key={dataISO}
        onClick={() => irPara(dataISO)}
        aria-pressed={dataISO === selecionada}
        aria-label={`${d.getDate()} de ${d.toLocaleDateString("pt-BR", {
          month: "long"
        })}, ${itens.length} evento(s)`}
      >
        <span className="cal-num">{d.getDate()}</span>
        <span className="cal-dots" aria-hidden="true">
          {tipos.map((tipo) => (
            <span className={`cal-dot tipo-${tipo}`} key={tipo} />
          ))}
        </span>
      </button>
    );
  }

  const itensDia = porDia[selecionada] || [];
  const dataSelecionada = deISO(selecionada);

  return (
    <AppShell>
      <PageHeader
        eyebrow="Planejamento"
        title="Calendário"
        subtitle="Aulas, avaliações, entregas e anotações em um só lugar."
        action={
          <button
            type="button"
            className="btn btn-primary btn-sm"
            onClick={() => abrirModal()}
          >
            <Icon nome="adicionar" tamanho={17} />
            Evento
          </button>
        }
      />

      <section className="calendar-card" aria-label="Calendário mensal">
        <header className="calendar-head">
          <div>
            <h2>
              {maiuscula(
                new Date(ano, mes, 1).toLocaleDateString("pt-BR", {
                  month: "long",
                  year: "numeric"
                })
              )}
            </h2>
            <p>Toque em um dia para ver os detalhes.</p>
          </div>
          <div className="calendar-nav">
            <button
              type="button"
              className="icon-button icon-button--soft"
              onClick={() => mudarMes(-1)}
              aria-label="Mês anterior"
            >
              <Icon nome="voltar" tamanho={18} />
            </button>
            <button
              type="button"
              className="btn btn-soft btn-xs"
              onClick={() => irPara(hojeISO())}
            >
              Hoje
            </button>
            <button
              type="button"
              className="icon-button icon-button--soft"
              onClick={() => mudarMes(1)}
              aria-label="Próximo mês"
            >
              <Icon nome="seta" tamanho={18} />
            </button>
          </div>
        </header>

        <div className="cal-weekdays" aria-hidden="true">
          {["S", "T", "Q", "Q", "S", "S", "D"].map((dia, indice) => (
            <span key={`${dia}-${indice}`}>{dia}</span>
          ))}
        </div>

        <div className="cal-grid" role="group" aria-label="Dias do mês">
          {celulas}
        </div>

        <div className="cal-legend" aria-label="Legenda">
          {Object.entries(TIPOS).map(([chave, rotulo]) => (
            <span className="cal-legend-item" key={chave}>
              <span className={`cal-dot tipo-${chave}`} />
              {rotulo}
            </span>
          ))}
        </div>
      </section>

      <section className="section" aria-labelledby="dia-selecionado">
        <header className="section-head">
          <div>
            <h2 id="dia-selecionado">
              {maiuscula(
                dataSelecionada.toLocaleDateString("pt-BR", {
                  weekday: "long",
                  day: "numeric",
                  month: "long"
                })
              )}
            </h2>
            <span className="section-sub">
              {itensDia.length
                ? `${itensDia.length} compromisso(s) neste dia`
                : "Nenhum compromisso neste dia"}
            </span>
          </div>
          <button
            type="button"
            className="icon-button icon-button--primary"
            onClick={() => abrirModal()}
            aria-label="Adicionar evento neste dia"
          >
            <Icon nome="adicionar" tamanho={18} />
          </button>
        </header>

        <form className="quick-add" onSubmit={salvarAnotacao}>
          <input
            type="text"
            className="input"
            maxLength="100"
            placeholder="Anotação rápida para este dia"
            aria-label="Anotação rápida para o dia selecionado"
            value={anotacao}
            onChange={(evento) => setAnotacao(evento.target.value)}
          />
          <button type="submit" className="btn btn-soft">
            Salvar
          </button>
        </form>

        {!itensDia.length ? (
          <EmptyState
            icone="calendario"
            mensagem="Nenhum evento neste dia. Use o botão + para adicionar."
          />
        ) : (
          <div className="event-list">
            {itensDia.map((item, indice) => (
              <article
                className={`event-card tipo-${item.tipo}`}
                key={`${item.origem}-${item.id ?? indice}-${item.titulo}`}
              >
                <span className="event-time">
                  {item.hora || "Dia todo"}
                </span>
                <div className="event-main">
                  <strong>{item.titulo}</strong>
                  <span className="event-kind">
                    {TIPOS[item.tipo]}
                    {item.origem === "aula" ? " · agenda" : ""}
                  </span>
                  {item.turma && <small>{item.turma}</small>}
                  {item.descricao && <p>{item.descricao}</p>}
                </div>
                {item.origem === "evento" && (
                  <button
                    type="button"
                    className="icon-button icon-button--soft"
                    onClick={() => {
                      const achado = eventos.find(
                        (x) => String(x.id) === String(item.id)
                      );
                      if (achado) abrirModal({ evento: achado });
                    }}
                    aria-label={`Editar ${item.titulo}`}
                  >
                    <Icon nome="editar" tamanho={16} />
                  </button>
                )}
              </article>
            ))}
          </div>
        )}
      </section>

      <Sheet
        id="evento"
        open={Boolean(modal)}
        onClose={fecharModal}
        title={modal?.evento ? "Editar evento" : "Novo evento"}
        subtitle={modal?.evento ? "Atualize as informações." : "Agende um compromisso."}
        footer={
          <>
            {modal?.evento && (
              <button
                type="button"
                className="btn btn-danger-ghost btn--start"
                onClick={excluirEvento}
              >
                <Icon nome="excluir" tamanho={17} />
                Excluir
              </button>
            )}
            <button
              type="button"
              className="btn btn-ghost"
              onClick={fecharModal}
            >
              Cancelar
            </button>
            <button
              type="submit"
              className="btn btn-primary"
              form="form-evento"
              disabled={salvandoEvento}
            >
              {salvandoEvento ? "Salvando..." : "Salvar"}
            </button>
          </>
        }
      >
        <form id="form-evento" className="form" onSubmit={salvarEvento}>
          <div className="field">
            <label htmlFor="evtTitulo">Título</label>
            <input
              id="evtTitulo"
              className="input"
              type="text"
              maxLength="100"
              placeholder="Ex.: Aplicação de prova"
              value={formEvento.titulo}
              onChange={(evento) =>
                setFormEvento({ ...formEvento, titulo: evento.target.value })
              }
              required
            />
          </div>

          <div className="field">
            <label htmlFor="evtTipo">Tipo</label>
            <select
              id="evtTipo"
              className="input"
              value={formEvento.tipo}
              onChange={(evento) =>
                setFormEvento({ ...formEvento, tipo: evento.target.value })
              }
            >
              {Object.entries(TIPOS).map(([chave, rotulo]) => (
                <option value={chave} key={chave}>
                  {rotulo}
                </option>
              ))}
            </select>
          </div>

          <div className="form-row">
            <div className="field">
              <label htmlFor="evtData">Data</label>
              <input
                id="evtData"
                className="input"
                type="date"
                value={formEvento.data}
                onChange={(evento) =>
                  setFormEvento({ ...formEvento, data: evento.target.value })
                }
                required
              />
            </div>
            <div className="field">
              <label htmlFor="evtHora">Horário</label>
              <input
                id="evtHora"
                className="input"
                type="time"
                value={formEvento.hora}
                onChange={(evento) =>
                  setFormEvento({ ...formEvento, hora: evento.target.value })
                }
              />
            </div>
          </div>

          <div className="field">
            <label htmlFor="evtTurma">Turma</label>
            <input
              id="evtTurma"
              className="input"
              type="text"
              list="listaTurmasEvento"
              maxLength="60"
              placeholder="Todas as turmas"
              value={formEvento.turma}
              onChange={(evento) =>
                setFormEvento({ ...formEvento, turma: evento.target.value })
              }
            />
            <datalist id="listaTurmasEvento">
              {turmas.map((turma) => (
                <option value={turma.nome} key={turma.id} />
              ))}
            </datalist>
          </div>

          <div className="field">
            <label htmlFor="evtDescricao">Descrição</label>
            <textarea
              id="evtDescricao"
              className="input"
              rows="3"
              maxLength="500"
              placeholder="Informações do evento (opcional)"
              value={formEvento.descricao}
              onChange={(evento) =>
                setFormEvento({
                  ...formEvento,
                  descricao: evento.target.value
                })
              }
            />
          </div>
        </form>
      </Sheet>
    </AppShell>
  );
}
