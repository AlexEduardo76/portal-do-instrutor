import { useEffect, useMemo, useState } from "react";
import { useLocation, useNavigate, useParams } from "react-router-dom";
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
import { cpfValido, formatarCPF, soDigitos } from "../lib/utils";

const FORM_VAZIO = { nome: "", matricula: "", cpf: "" };

function iniciais(nome) {
  const partes = String(nome || "")
    .trim()
    .split(/\s+/)
    .filter(Boolean);
  if (!partes.length) return "?";
  return `${partes[0][0]}${partes.length > 1 ? partes[partes.length - 1][0] : ""}`.toUpperCase();
}

export default function AlunosTurma() {
  const { turmaId } = useParams();
  const location = useLocation();
  const navigate = useNavigate();
  const { toast } = useToast();
  const confirmar = useConfirm();

  const [turma, setTurma] = useState(location.state?.turma || null);
  const [alunos, setAlunos] = useState([]);
  const [carregando, setCarregando] = useState(true);
  const [erro, setErro] = useState("");
  const [busca, setBusca] = useState("");

  const [sheetAberto, setSheetAberto] = useState(false);
  const [editandoId, setEditandoId] = useState(null);
  const [formAluno, setFormAluno] = useState(FORM_VAZIO);
  const [salvando, setSalvando] = useState(false);

  useEffect(() => {
    if (turma) return undefined;
    let ativo = true;

    api
      .get("/turmas")
      .then((lista) => {
        if (!ativo) return;
        const achada = lista.find(
          (item) => String(item.id) === String(turmaId)
        );
        if (achada) setTurma(achada);
      })
      .catch((err) => {
        console.error("Erro ao carregar turma:", err);
      });

    return () => {
      ativo = false;
    };
  }, [turma, turmaId]);

  useEffect(() => {
    let ativo = true;
    setCarregando(true);
    setErro("");

    api
      .get(`/turmas/${turmaId}/alunos`)
      .then((resultado) => {
        if (ativo) setAlunos(resultado);
      })
      .catch((err) => {
        if (!ativo) return;
        console.error("Erro ao buscar alunos:", err);
        setErro("Não foi possível carregar os alunos desta turma.");
        toast("Não foi possível carregar os alunos.", "erro");
      })
      .finally(() => {
        if (ativo) setCarregando(false);
      });

    return () => {
      ativo = false;
    };
  }, [turmaId, toast]);

  const filtrados = useMemo(() => {
    const termo = busca.trim().toLowerCase();
    if (!termo) return alunos;
    return alunos.filter((aluno) =>
      `${aluno.nome} ${aluno.matricula || ""}`
        .toLowerCase()
        .includes(termo)
    );
  }, [alunos, busca]);

  async function recarregar() {
    try {
      const resultado = await api.get(`/turmas/${turmaId}/alunos`);
      setAlunos(resultado);
    } catch (err) {
      console.error("Erro ao buscar alunos:", err);
      toast("Não foi possível atualizar a lista.", "erro");
    }
  }

  function abrirSheet(aluno = null) {
    setEditandoId(aluno?.id ?? null);
    setFormAluno({
      nome: aluno?.nome || "",
      matricula: aluno?.matricula || "",
      cpf: aluno?.cpf ? formatarCPF(aluno.cpf) : ""
    });
    setSheetAberto(true);
  }

  function fecharSheet() {
    setSheetAberto(false);
    setEditandoId(null);
    setFormAluno(FORM_VAZIO);
  }

  async function salvarAluno(evento) {
    evento.preventDefault();
    const nome = formAluno.nome.trim();
    const matricula = formAluno.matricula.trim();
    const cpf = soDigitos(formAluno.cpf);

    if (nome.length < 3) {
      toast("Informe o nome do aluno (mínimo 3 caracteres).", "erro");
      return;
    }
    if (!matricula) {
      toast("Informe a matrícula do aluno.", "erro");
      return;
    }
    if (cpf && !cpfValido(cpf)) {
      toast("CPF inválido. Confira os 11 números.", "erro");
      return;
    }

    const repetido = alunos.find(
      (aluno) =>
        String(aluno.id) !== String(editandoId) &&
        (String(aluno.matricula || "").toLowerCase() ===
          matricula.toLowerCase() ||
          (cpf && soDigitos(aluno.cpf) === cpf))
    );
    if (repetido) {
      toast(
        `Já existe um aluno com essa matrícula ou CPF nesta turma (${repetido.nome}).`,
        "erro"
      );
      return;
    }

    const dados = { nome, matricula, cpf: cpf || null };
    setSalvando(true);
    try {
      if (editandoId) {
        await api.put(`/alunos/${editandoId}`, dados);
        toast("Aluno atualizado.", "sucesso");
      } else {
        await api.post(`/turmas/${turmaId}/alunos`, dados);
        toast("Aluno cadastrado.", "sucesso");
      }
      fecharSheet();
      await recarregar();
    } catch (err) {
      console.error("Erro ao salvar aluno:", err);
      toast(err.message || "Não foi possível salvar o aluno.", "erro");
    } finally {
      setSalvando(false);
    }
  }

  async function removerAluno(aluno) {
    const confirmado = await confirmar({
      titulo: "Remover aluno?",
      mensagem: `"${aluno.nome}" deixará de aparecer nesta turma.`,
      detalhe:
        "Ele deixa de aparecer em Frequência, Notas e Ocorrências. Os registros antigos são mantidos.",
      confirmarTexto: "Remover aluno",
      perigo: true
    });
    if (!confirmado) return;

    try {
      await api.delete(`/alunos/${aluno.id}`);
      toast("Aluno removido.", "sucesso");
      if (String(editandoId) === String(aluno.id)) fecharSheet();
      await recarregar();
    } catch (err) {
      console.error("Erro ao remover aluno:", err);
      toast(err.message || "Não foi possível remover o aluno.", "erro");
    }
  }

  return (
    <AppShell>
      <PageHeader
        eyebrow={turma?.codigo ? `Código ${turma.codigo}` : "Turma"}
        title={turma?.nome || "Alunos"}
        subtitle={
          carregando
            ? "Carregando alunos..."
            : alunos.length === 1
            ? "1 aluno ativo"
            : `${alunos.length} alunos ativos`
        }
        onBack={() => navigate("/turmas")}
        backLabel="Turmas"
        action={
          <button
            type="button"
            className="btn btn-primary btn-sm"
            onClick={() => abrirSheet()}
          >
            <Icon nome="adicionar" tamanho={17} />
            Aluno
          </button>
        }
      />

      {!carregando && alunos.length > 2 && (
        <div className="search-box">
          <Icon nome="buscar" tamanho={18} />
          <input
            type="search"
            placeholder="Buscar aluno ou matrícula"
            aria-label="Buscar alunos"
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
      )}

      {carregando && <LoadingState mensagem="Carregando alunos..." />}
      {!carregando && erro && <ErrorState mensagem={erro} />}
      {!carregando && !erro && !alunos.length && (
        <EmptyState
          icone="turmas"
          titulo="Turma sem alunos"
          mensagem="Cadastre o primeiro aluno para usar frequência, notas e ocorrências."
        >
          <button
            type="button"
            className="btn btn-primary btn-sm"
            onClick={() => abrirSheet()}
          >
            <Icon nome="adicionar" tamanho={17} />
            Cadastrar aluno
          </button>
        </EmptyState>
      )}
      {!carregando && !erro && alunos.length > 0 && !filtrados.length && (
        <EmptyState
          icone="buscar"
          mensagem={`Nenhum aluno encontrado para "${busca}".`}
        />
      )}

      <div className="aluno-list">
        {filtrados.map((aluno) => (
          <article className="aluno-card" key={aluno.id}>
            <span className="aluno-avatar" aria-hidden="true">
              {iniciais(aluno.nome)}
            </span>
            <div className="aluno-copy">
              <strong>{aluno.nome}</strong>
              <small>Matrícula {aluno.matricula || "—"}</small>
              {aluno.cpf && <small>CPF {formatarCPF(aluno.cpf)}</small>}
            </div>
            <div className="aluno-actions">
              <button
                type="button"
                className="icon-button icon-button--soft"
                onClick={() => abrirSheet(aluno)}
                aria-label={`Editar ${aluno.nome}`}
              >
                <Icon nome="editar" tamanho={16} />
              </button>
              <button
                type="button"
                className="icon-button icon-button--danger"
                onClick={() => removerAluno(aluno)}
                aria-label={`Remover ${aluno.nome}`}
              >
                <Icon nome="excluir" tamanho={16} />
              </button>
            </div>
          </article>
        ))}
      </div>

      <p className="list-footnote">
        {turma?.nome ? `${turma.nome} · ` : ""}
        {alunos.length} aluno(s) cadastrado(s)
      </p>

      <Sheet
        id="aluno"
        open={sheetAberto}
        onClose={fecharSheet}
        title={editandoId ? "Editar aluno" : "Novo aluno"}
        subtitle={turma?.nome}
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
              form="form-aluno"
              disabled={salvando}
            >
              {salvando
                ? "Salvando..."
                : editandoId
                ? "Salvar alterações"
                : "Cadastrar aluno"}
            </button>
          </>
        }
      >
        <form
          id="form-aluno"
          className="form"
          autoComplete="off"
          onSubmit={salvarAluno}
        >
          <div className="field">
            <label htmlFor="alunoNome">Nome completo</label>
            <input
              id="alunoNome"
              className="input"
              type="text"
              minLength="3"
              maxLength="120"
              value={formAluno.nome}
              onChange={(evento) =>
                setFormAluno({ ...formAluno, nome: evento.target.value })
              }
              required
            />
          </div>
          <div className="field">
            <label htmlFor="alunoMatricula">Matrícula</label>
            <input
              id="alunoMatricula"
              className="input"
              type="text"
              inputMode="numeric"
              maxLength="30"
              value={formAluno.matricula}
              onChange={(evento) =>
                setFormAluno({
                  ...formAluno,
                  matricula: evento.target.value
                })
              }
              required
            />
          </div>
          <div className="field">
            <label htmlFor="alunoCpf">CPF</label>
            <input
              id="alunoCpf"
              className="input"
              type="text"
              inputMode="numeric"
              maxLength="14"
              placeholder="000.000.000-00"
              value={formAluno.cpf}
              onChange={(evento) =>
                setFormAluno({
                  ...formAluno,
                  cpf: formatarCPF(evento.target.value)
                })
              }
            />
            <small className="field-hint">Opcional</small>
          </div>
        </form>
      </Sheet>
    </AppShell>
  );
}
