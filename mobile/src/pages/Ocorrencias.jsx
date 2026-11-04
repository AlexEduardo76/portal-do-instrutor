import { useEffect, useState } from "react";
import { useSearchParams } from "react-router-dom";
import AppShell from "../components/AppShell";
import Icon from "../components/Icon";
import PageHeader from "../components/PageHeader";
import Sheet from "../components/Sheet";
import {
  EmptyState,
  ErrorState,
  LoadingState
} from "../components/StateViews";
import { api } from "../lib/api";
import { useToast } from "../lib/toast";
import { dataBR, hojeISO } from "../lib/utils";

const LIMITE_HISTORICO = 200;

function iniciais(nome) {
  const partes = String(nome || "")
    .trim()
    .split(/\s+/)
    .filter(Boolean);
  if (!partes.length) return "?";
  return `${partes[0][0]}${partes.length > 1 ? partes[partes.length - 1][0] : ""}`.toUpperCase();
}

export default function Ocorrencias() {
  const { toast } = useToast();
  const [searchParams] = useSearchParams();

  const [turmas, setTurmas] = useState([]);
  const [turmaId, setTurmaId] = useState("");
  const [alunos, setAlunos] = useState([]);
  const [ocorrencias, setOcorrencias] = useState([]);
  const [resumo, setResumo] = useState(
    "Selecione uma turma para visualizar as ocorrências registradas."
  );
  const [vazio, setVazio] = useState("");
  const [carregando, setCarregando] = useState(false);

  const [sheetAberto, setSheetAberto] = useState(false);
  const [editandoId, setEditandoId] = useState(null);
  const [alunoId, setAlunoId] = useState("");
  const [data, setData] = useState(hojeISO());
  const [descricao, setDescricao] = useState("");
  const [salvando, setSalvando] = useState(false);

  useEffect(() => {
    let ativo = true;
    api
      .get("/turmas/resumo")
      .then((resultado) => {
        if (!ativo) return;
        setTurmas(resultado);
        if (!resultado.length) {
          setResumo("Você ainda não tem turmas cadastradas.");
          return;
        }
        const turmaUrl = searchParams.get("turma");
        if (
          turmaUrl &&
          resultado.some((t) => String(t.id) === String(turmaUrl))
        ) {
          setTurmaId(turmaUrl);
        }
      })
      .catch((err) => {
        if (!ativo) return;
        console.error("Erro ao buscar turmas:", err);
        toast("Não foi possível carregar as turmas.", "erro");
        setResumo("Não foi possível conectar ao servidor.");
      });
    return () => {
      ativo = false;
    };
  }, [searchParams, toast]);

  useEffect(() => {
    let ativo = true;
    setAlunos([]);
    setOcorrencias([]);

    if (!turmaId) {
      setCarregando(false);
      setVazio("");
      setResumo(
        "Selecione uma turma para visualizar as ocorrências registradas."
      );
      return () => {
        ativo = false;
      };
    }

    setCarregando(true);
    setResumo("Carregando ocorrências...");
    setVazio("");

    Promise.all([
      api.get(`/turmas/${turmaId}/alunos`),
      api.get(`/turmas/${turmaId}/ocorrencias`)
    ])
      .then(([alunosData, historico]) => {
        if (!ativo) return;
        setAlunos(alunosData);
        const lista = historico.slice(0, LIMITE_HISTORICO);
        setOcorrencias(lista);

        if (!lista.length) {
          setResumo("Nenhuma ocorrência registrada nesta turma.");
          setVazio("Nenhuma ocorrência registrada nesta turma.");
        } else {
          setResumo(
            `${lista.length} ocorrência(s) registrada(s) nesta turma` +
              (historico.length >= LIMITE_HISTORICO
                ? ` (mostrando as ${LIMITE_HISTORICO} mais recentes)`
                : "")
          );
          setVazio("");
        }
      })
      .catch((err) => {
        if (!ativo) return;
        console.error("Erro ao buscar ocorrências:", err);
        toast("Não foi possível carregar o histórico.", "erro");
        setResumo("Não foi possível carregar as ocorrências.");
      })
      .finally(() => {
        if (ativo) setCarregando(false);
      });

    return () => {
      ativo = false;
    };
  }, [turmaId, toast]);

  async function carregarHistorico(id) {
    setResumo("Carregando ocorrências...");
    setVazio("");

    try {
      const historico = await api.get(`/turmas/${id}/ocorrencias`);
      const lista = historico.slice(0, LIMITE_HISTORICO);
      setOcorrencias(lista);

      if (!lista.length) {
        setResumo("Nenhuma ocorrência registrada nesta turma.");
        setVazio("Nenhuma ocorrência registrada nesta turma.");
      } else {
        setResumo(
          `${lista.length} ocorrência(s) registrada(s) nesta turma` +
            (historico.length >= LIMITE_HISTORICO
              ? ` (mostrando as ${LIMITE_HISTORICO} mais recentes)`
              : "")
        );
        setVazio("");
      }
    } catch (err) {
      console.error("Erro ao buscar ocorrências:", err);
      toast("Não foi possível carregar o histórico.", "erro");
      setResumo("Não foi possível carregar as ocorrências.");
    }
  }

  function limparFormulario() {
    setEditandoId(null);
    setAlunoId("");
    setDescricao("");
    setData(hojeISO());
  }

  function abrirNova() {
    if (!turmaId) {
      toast("Selecione uma turma primeiro.", "erro");
      return;
    }
    limparFormulario();
    setSheetAberto(true);
  }

  function editarOcorrencia(ocorrencia) {
    setAlunos((atuais) =>
      atuais.some((a) => String(a.id) === String(ocorrencia.aluno.id))
        ? atuais
        : [...atuais, ocorrencia.aluno]
    );
    setEditandoId(ocorrencia.id);
    setAlunoId(String(ocorrencia.aluno.id));
    setData(ocorrencia.data);
    setDescricao(ocorrencia.descricao);
    setSheetAberto(true);
  }

  function fecharSheet() {
    setSheetAberto(false);
    limparFormulario();
  }

  async function salvarOcorrencia(evento) {
    evento.preventDefault();

    if (!turmaId) {
      toast("Selecione uma turma.", "erro");
      return;
    }
    if (!alunoId) {
      toast("Selecione o aluno.", "erro");
      return;
    }
    if (!data) {
      toast("Informe a data da ocorrência.", "erro");
      return;
    }
    if (data > hojeISO()) {
      toast("A data da ocorrência não pode ser no futuro.", "erro");
      return;
    }
    if (descricao.trim().length < 3) {
      toast("Descreva a ocorrência (mínimo de 3 caracteres).", "erro");
      return;
    }

    const editando = editandoId !== null;
    const dados = {
      alunoId: Number(alunoId),
      data,
      descricao: descricao.trim()
    };

    setSalvando(true);
    try {
      if (editando) {
        await api.put(`/turmas/${turmaId}/ocorrencias/${editandoId}`, dados);
        toast("Ocorrência atualizada.", "sucesso");
      } else {
        await api.post(`/turmas/${turmaId}/ocorrencias`, dados);
        toast("Ocorrência registrada.", "sucesso");
      }

      fecharSheet();
      await carregarHistorico(turmaId);
    } catch (err) {
      console.error("Erro ao salvar ocorrência:", err);
      toast(err.message || "Não foi possível salvar a ocorrência.", "erro");
    } finally {
      setSalvando(false);
    }
  }

  return (
    <AppShell>
      <PageHeader
        eyebrow="Gestão"
        title="Ocorrências"
        subtitle="Registre observações e ocorrências dos alunos."
        action={
          <button
            type="button"
            className="btn btn-primary btn-sm"
            onClick={abrirNova}
          >
            <Icon nome="adicionar" tamanho={17} />
            Registrar
          </button>
        }
      />

      <section className="filter-card" aria-label="Selecionar turma">
        <div className="field">
          <label htmlFor="turmaOcorrencia">Turma</label>
          <select
            id="turmaOcorrencia"
            className="input"
            value={turmaId}
            onChange={(evento) => setTurmaId(evento.target.value)}
          >
            <option value="">Selecione uma turma</option>
            {turmas.map((turma) => (
              <option value={turma.id} key={turma.id}>
                {turma.nome}
                {turma.codigo ? ` (${turma.codigo})` : ""}
              </option>
            ))}
          </select>
        </div>
      </section>

      <p className="summary-note">{resumo}</p>

      {carregando && <LoadingState mensagem="Carregando ocorrências..." />}

      {!carregando && resumo.startsWith("Não foi possível") && (
        <ErrorState mensagem={resumo} />
      )}

      {!carregando && !turmaId && (
        <EmptyState
          icone="ocorrencias"
          mensagem="Escolha uma turma acima para ver o histórico."
        />
      )}

      {!carregando && turmaId && !ocorrencias.length && vazio && (
        <EmptyState
          icone="ocorrencias"
          titulo="Nenhuma ocorrência"
          mensagem={vazio}
        >
          <button
            type="button"
            className="btn btn-primary btn-sm"
            onClick={abrirNova}
          >
            <Icon nome="adicionar" tamanho={17} />
            Registrar ocorrência
          </button>
        </EmptyState>
      )}

      {!carregando && ocorrencias.length > 0 && (
        <div className="ocorrencia-list">
          {ocorrencias.map((ocorrencia) => (
            <article className="ocorrencia-card" key={ocorrencia.id}>
              <header className="ocorrencia-head">
                <span className="chip chip--soft">
                  {dataBR(ocorrencia.data)}
                </span>
                <button
                  type="button"
                  className="icon-button icon-button--soft"
                  onClick={() => editarOcorrencia(ocorrencia)}
                  aria-label="Editar ocorrência"
                >
                  <Icon nome="editar" tamanho={16} />
                </button>
              </header>

              <div className="ocorrencia-student">
                <span className="aluno-avatar aluno-avatar--sm" aria-hidden="true">
                  {iniciais(ocorrencia.aluno.nome)}
                </span>
                <span>
                  <strong>{ocorrencia.aluno.nome}</strong>
                  <small>
                    Matrícula {ocorrencia.aluno.matricula || "—"}
                  </small>
                </span>
              </div>

              <p className="ocorrencia-text">{ocorrencia.descricao}</p>
            </article>
          ))}
        </div>
      )}

      <Sheet
        id="ocorrencia"
        open={sheetAberto}
        onClose={fecharSheet}
        title={editandoId ? "Editar ocorrência" : "Nova ocorrência"}
        subtitle="Registre a observação sobre o aluno."
        footer={
          <>
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
              form="form-ocorrencia"
              disabled={salvando}
            >
              {salvando ? "Salvando..." : "Salvar"}
            </button>
          </>
        }
      >
        <form
          id="form-ocorrencia"
          className="form"
          onSubmit={salvarOcorrencia}
        >
          <div className="field">
            <label htmlFor="alunoOcorrencia">Aluno</label>
            <select
              id="alunoOcorrencia"
              className="input"
              value={alunoId}
              onChange={(evento) => setAlunoId(evento.target.value)}
              required
            >
              <option value="">
                {alunos.length
                  ? "Selecione um aluno"
                  : "Esta turma não tem alunos"}
              </option>
              {alunos.map((aluno) => (
                <option value={aluno.id} key={aluno.id}>
                  {aluno.nome}
                  {aluno.matricula ? ` (${aluno.matricula})` : ""}
                </option>
              ))}
            </select>
          </div>

          <div className="field">
            <label htmlFor="dataOcorrencia">Data</label>
            <input
              id="dataOcorrencia"
              className="input"
              type="date"
              value={data}
              max={hojeISO()}
              onChange={(evento) => setData(evento.target.value)}
              required
            />
          </div>

          <div className="field">
            <label htmlFor="descricaoOcorrencia">Ocorrência</label>
            <textarea
              id="descricaoOcorrencia"
              className="input"
              rows="5"
              maxLength="2000"
              placeholder="Descreva o que aconteceu..."
              value={descricao}
              onChange={(evento) => setDescricao(evento.target.value)}
              required
            />
            <small className="field-hint">
              {descricao.length}/2000 caracteres
            </small>
          </div>
        </form>
      </Sheet>
    </AppShell>
  );
}
