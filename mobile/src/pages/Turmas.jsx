import { useEffect, useMemo, useState } from "react";
import { Link, useNavigate } from "react-router-dom";
import AppShell from "../components/AppShell";
import Icon from "../components/Icon";
import PageHeader from "../components/PageHeader";
import Sheet from "../components/Sheet";
import { useConfirm } from "../components/ConfirmDialog";
import {
  EmptyState,
  ErrorState,
  LoadingState
} from "../components/StateViews";
import { api } from "../lib/api";
import { useToast } from "../lib/toast";

const FORM_VAZIO = {
  nome: "",
  codigo: "",
  curso: "",
  turno: "",
  sala: "",
  qtdAlunos: "",
  qtdUcs: "",
  horas: ""
};

const TURNOS = ["Manhã", "Tarde", "Noite", "Integral", "EAD"];

function numero(grupo, id) {
  if (grupo === null || grupo === undefined) return "—";
  return grupo[id] || 0;
}

export default function Turmas() {
  const { toast } = useToast();
  const confirmar = useConfirm();
  const navigate = useNavigate();

  const [turmas, setTurmas] = useState([]);
  const [contagens, setContagens] = useState({ alunos: null, ucs: null });
  const [carregando, setCarregando] = useState(true);
  const [erro, setErro] = useState("");
  const [busca, setBusca] = useState("");

  const [sheetAberto, setSheetAberto] = useState(false);
  const [editandoId, setEditandoId] = useState(null);
  const [formTurma, setFormTurma] = useState(FORM_VAZIO);
  const [salvando, setSalvando] = useState(false);

  async function carregarTurmas() {
    try {
      const resultado = await api.get("/turmas");
      setTurmas(resultado);
      setContagens({ alunos: null, ucs: null });

      if (resultado.length) {
        const ids = resultado.map((t) => t.id);
        try {
          const [alunosContagem, ucsContagem] = await Promise.all([
            api.get(`/turmas/alunos/contagem?ids=${ids.join(",")}`),
            api.get(`/turmas/ucs/contagem?ids=${ids.join(",")}`)
          ]);
          setContagens({ alunos: alunosContagem, ucs: ucsContagem });
        } catch (err) {
          console.warn("Erro ao buscar contagens:", err);
        }
      }

      setErro("");
      return resultado;
    } catch (err) {
      console.error("Erro ao buscar turmas:", err);
      setErro("Não foi possível conectar ao servidor.");
      toast("Não foi possível conectar ao servidor.", "erro");
      return [];
    } finally {
      setCarregando(false);
    }
  }

  useEffect(() => {
    carregarTurmas();
  }, []);

  const filtradas = useMemo(() => {
    const termo = busca.trim().toLowerCase();
    if (!termo) return turmas;
    return turmas.filter((turma) =>
      `${turma.nome} ${turma.codigo || ""} ${turma.curso || ""}`
        .toLowerCase()
        .includes(termo)
    );
  }, [turmas, busca]);

  function textoAlunos(turma) {
    const cadastrados = numero(contagens.alunos, turma.id);
    const previstos = turma.qtdAlunos ?? null;
    if (previstos === null) return cadastrados;
    return cadastrados === "—" ? previstos : `${cadastrados}/${previstos}`;
  }

  const textoUcs = (turma) => turma.qtdUcs ?? numero(contagens.ucs, turma.id);
  const textoHoras = (turma) => turma.horas ?? "—";

  function abrirSheet(turma = null) {
    setEditandoId(turma?.id ?? null);
    setFormTurma({
      nome: turma?.nome ?? "",
      codigo: turma?.codigo ?? "",
      curso: turma?.curso ?? "",
      turno: turma?.turno ?? "",
      sala: turma?.sala ?? "",
      qtdAlunos: turma?.qtdAlunos ?? "",
      qtdUcs: turma?.qtdUcs ?? "",
      horas: turma?.horas ?? ""
    });
    setSheetAberto(true);
  }

  function fecharSheet() {
    setSheetAberto(false);
    setEditandoId(null);
    setFormTurma(FORM_VAZIO);
  }

  async function salvarTurma(evento) {
    evento.preventDefault();
    const nome = formTurma.nome.trim();
    const codigo = formTurma.codigo.trim() || null;
    const curso = formTurma.curso.trim() || null;
    const turno = formTurma.turno.trim() || null;
    const sala = formTurma.sala.trim() || null;

    if (nome.length < 3) {
      toast("O nome da turma precisa ter pelo menos 3 caracteres.", "erro");
      return;
    }

    const lerNumero = (valor) => {
      const texto = String(valor).trim();
      return texto === "" ? null : Number(texto);
    };
    const qtdAlunos = lerNumero(formTurma.qtdAlunos);
    const qtdUcs = lerNumero(formTurma.qtdUcs);
    const horas = lerNumero(formTurma.horas);

    for (const [valor, rotulo] of [
      [qtdAlunos, "quantidade de alunos"],
      [qtdUcs, "quantidade de UCs"],
      [horas, "horas"]
    ]) {
      if (
        valor !== null &&
        (!Number.isInteger(valor) || valor < 0 || valor > 9999)
      ) {
        toast(
          `Informe um número inteiro válido em ${rotulo} (de 0 a 9999).`,
          "erro"
        );
        return;
      }
    }

    const dados = {
      nome,
      codigo,
      curso,
      turno,
      sala,
      qtdAlunos,
      qtdUcs,
      horas
    };

    setSalvando(true);
    try {
      if (editandoId) {
        await api.put(`/turmas/${editandoId}`, dados);
        toast("Turma atualizada.", "sucesso");
        fecharSheet();
        await carregarTurmas();
      } else {
        const criada = await api.post("/turmas", dados);
        toast("Turma criada.", "sucesso");
        fecharSheet();
        const lista = await carregarTurmas();
        const turmaCriada =
          lista.find((t) => String(t.id) === String(criada.id)) || criada;
        navigate(`/turmas/${criada.id}/alunos`, {
          state: { turma: turmaCriada }
        });
      }
    } catch (err) {
      console.error("Erro ao salvar turma:", err);
      toast(err.message || "Não foi possível salvar a turma.", "erro");
    } finally {
      setSalvando(false);
    }
  }

  async function excluirTurma(turma) {
    const alunosContagem = contagens.alunos
      ? contagens.alunos[turma.id] || 0
      : null;

    const confirmado = await confirmar({
      titulo: "Excluir turma?",
      mensagem: `A turma "${turma.nome}" será excluída permanentemente.`,
      detalhe: alunosContagem
        ? `Os ${alunosContagem} aluno(s), frequências, avaliações e notas desta turma também serão apagados.`
        : "Alunos, frequências, avaliações e notas desta turma também serão apagados.",
      confirmarTexto: "Excluir turma",
      perigo: true
    });
    if (!confirmado) return;

    try {
      await api.delete(`/turmas/${turma.id}`);
      toast("Turma excluída.", "sucesso");
      fecharSheet();
      await carregarTurmas();
    } catch (err) {
      console.error("Erro ao excluir turma:", err);
      toast(err.message || "Não foi possível excluir a turma.", "erro");
    }
  }

  return (
    <AppShell>
      <PageHeader
        eyebrow="Gestão acadêmica"
        title="Turmas"
        subtitle={
          turmas.length
            ? `${turmas.length} turma(s) vinculada(s) ao seu perfil.`
            : "Crie e acompanhe suas turmas."
        }
        action={
          <button
            type="button"
            className="btn btn-primary btn-sm"
            onClick={() => abrirSheet()}
          >
            <Icon nome="adicionar" tamanho={17} />
            Nova
          </button>
        }
      />

      <div className="search-box">
        <Icon nome="buscar" tamanho={18} />
        <input
          type="search"
          placeholder="Buscar por nome ou código"
          aria-label="Buscar turmas"
          value={busca}
          onChange={(evento) => setBusca(evento.target.value)}
        />
        {busca && (
          <button
            type="button"
            className="search-clear"
            onClick={() => setBusca("")}
            aria-label="Limpar busca"
          >
            <Icon nome="fechar" tamanho={16} />
          </button>
        )}
      </div>

      {carregando && <LoadingState mensagem="Carregando turmas..." />}
      {!carregando && erro && <ErrorState mensagem={erro} />}
      {!carregando && !erro && !turmas.length && (
        <EmptyState
          icone="turmas"
          titulo="Nenhuma turma ainda"
          mensagem="Crie sua primeira turma para começar a cadastrar alunos e registrar aulas."
        >
          <button
            type="button"
            className="btn btn-primary btn-sm"
            onClick={() => abrirSheet()}
          >
            <Icon nome="adicionar" tamanho={17} />
            Criar turma
          </button>
        </EmptyState>
      )}
      {!carregando && !erro && turmas.length > 0 && !filtradas.length && (
        <EmptyState
          icone="buscar"
          mensagem={`Nenhuma turma encontrada para "${busca}".`}
        />
      )}

      <div className="turma-list">
        {filtradas.map((turma) => (
          <article className="turma-card" key={turma.id}>
            <div className="turma-card-head">
              <span className="chip">{turma.codigo || "Sem código"}</span>
              <button
                type="button"
                className="icon-button icon-button--soft"
                onClick={() => abrirSheet(turma)}
                aria-label={`Editar turma ${turma.nome}`}
              >
                <Icon nome="editar" tamanho={17} />
              </button>
            </div>

            <Link
              className="turma-card-body"
              to={`/turmas/${turma.id}/alunos`}
              state={{ turma }}
            >
              <h3>{turma.nome}</h3>
              <p>
                {[turma.curso, turma.turno].filter(Boolean).join(" · ") ||
                  "Sem curso informado"}
              </p>
            </Link>

            <div className="turma-meta">
              <span>
                <strong>{textoAlunos(turma)}</strong> alunos
              </span>
              <span>
                <strong>{textoUcs(turma)}</strong> UCs
              </span>
              <span>
                <strong>{textoHoras(turma)}</strong> horas
              </span>
            </div>

            <div className="turma-actions">
              <Link
                className="btn btn-primary btn-sm"
                to={`/turmas/${turma.id}/alunos`}
                state={{ turma }}
              >
                Alunos
              </Link>
              <Link
                className="btn btn-soft btn-sm"
                to={`/frequencia?turma=${encodeURIComponent(turma.id)}`}
              >
                Frequência
              </Link>
              <Link
                className="btn btn-soft btn-sm"
                to={`/notas?turma=${encodeURIComponent(turma.id)}`}
              >
                Notas
              </Link>
            </div>
          </article>
        ))}
      </div>

      <Sheet
        id="turma"
        open={sheetAberto}
        onClose={fecharSheet}
        title={editandoId ? "Editar turma" : "Nova turma"}
        subtitle={
          editandoId
            ? "Atualize os dados principais da turma."
            : "Preencha os dados para criar uma nova turma."
        }
        footer={
          <>
            {editandoId && (
              <button
                type="button"
                className="btn btn-danger-ghost btn--start"
                onClick={() => {
                  const alvo = turmas.find(
                    (t) => String(t.id) === String(editandoId)
                  );
                  if (alvo) excluirTurma(alvo);
                }}
              >
                <Icon nome="excluir" tamanho={17} />
                Excluir
              </button>
            )}
            <button
              type="button"
              className="btn btn-ghost"
              onClick={fecharSheet}
            >
              Cancelar
            </button>
            <button
              type="submit"
              className="btn btn-primary"
              form="form-turma"
              disabled={salvando}
            >
              {salvando ? "Salvando..." : "Salvar"}
            </button>
          </>
        }
      >
        <form id="form-turma" className="form" onSubmit={salvarTurma}>
          <div className="field">
            <label htmlFor="nomeTurma">Nome da turma</label>
            <input
              id="nomeTurma"
              className="input"
              type="text"
              minLength="3"
              maxLength="100"
              value={formTurma.nome}
              onChange={(evento) =>
                setFormTurma({ ...formTurma, nome: evento.target.value })
              }
              required
            />
          </div>

          <div className="field">
            <label htmlFor="codigoTurma">Código</label>
            <input
              id="codigoTurma"
              className="input"
              type="text"
              maxLength="30"
              placeholder="Opcional"
              value={formTurma.codigo}
              onChange={(evento) =>
                setFormTurma({ ...formTurma, codigo: evento.target.value })
              }
            />
          </div>

          <div className="field">
            <label htmlFor="cursoTurma">Curso</label>
            <input
              id="cursoTurma"
              className="input"
              type="text"
              maxLength="100"
              placeholder="Opcional"
              value={formTurma.curso}
              onChange={(evento) =>
                setFormTurma({ ...formTurma, curso: evento.target.value })
              }
            />
          </div>

          <div className="form-row">
            <div className="field">
              <label htmlFor="turnoTurma">Turno</label>
              <input
                id="turnoTurma"
                className="input"
                type="text"
                list="turnosTurma"
                maxLength="20"
                placeholder="Opcional"
                value={formTurma.turno}
                onChange={(evento) =>
                  setFormTurma({ ...formTurma, turno: evento.target.value })
                }
              />
              <datalist id="turnosTurma">
                {TURNOS.map((turno) => (
                  <option value={turno} key={turno} />
                ))}
              </datalist>
            </div>
            <div className="field">
              <label htmlFor="salaTurma">Sala</label>
              <input
                id="salaTurma"
                className="input"
                type="text"
                maxLength="30"
                placeholder="Opcional"
                value={formTurma.sala}
                onChange={(evento) =>
                  setFormTurma({ ...formTurma, sala: evento.target.value })
                }
              />
            </div>
          </div>

          <div className="form-row">
            <div className="field">
              <label htmlFor="qtdAlunosTurma">Alunos</label>
              <input
                id="qtdAlunosTurma"
                className="input"
                type="number"
                min="0"
                max="9999"
                step="1"
                inputMode="numeric"
                placeholder="0"
                value={formTurma.qtdAlunos}
                onChange={(evento) =>
                  setFormTurma({
                    ...formTurma,
                    qtdAlunos: evento.target.value
                  })
                }
              />
            </div>
            <div className="field">
              <label htmlFor="qtdUcsTurma">UCs</label>
              <input
                id="qtdUcsTurma"
                className="input"
                type="number"
                min="0"
                max="9999"
                step="1"
                inputMode="numeric"
                placeholder="0"
                value={formTurma.qtdUcs}
                onChange={(evento) =>
                  setFormTurma({ ...formTurma, qtdUcs: evento.target.value })
                }
              />
            </div>
            <div className="field">
              <label htmlFor="horasTurma">Horas</label>
              <input
                id="horasTurma"
                className="input"
                type="number"
                min="0"
                max="9999"
                step="1"
                inputMode="numeric"
                placeholder="0"
                value={formTurma.horas}
                onChange={(evento) =>
                  setFormTurma({ ...formTurma, horas: evento.target.value })
                }
              />
            </div>
          </div>
        </form>
      </Sheet>
    </AppShell>
  );
}
