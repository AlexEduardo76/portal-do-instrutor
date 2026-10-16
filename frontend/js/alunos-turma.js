// Alunos dentro de cada turma: visualizar, cadastrar, editar e remover.

let alunosTurmaId = null;
let alunosTurma = [];
let alunoEditandoId = null;

const backdropAlunos = () => el("modalAlunosBackdrop");

// ====== CPF ======
const soDigitos = (v) => String(v ?? "").replace(/\D/g, "");

function formatarCPF(valor) {
    const d = soDigitos(valor).slice(0, 11);
    return d
        .replace(/^(\d{3})(\d)/, "$1.$2")
        .replace(/^(\d{3})\.(\d{3})(\d)/, "$1.$2.$3")
        .replace(/\.(\d{3})(\d)/, ".$1-$2");
}

function cpfValido(valor) {
    const d = soDigitos(valor);
    if (d.length !== 11 || /^(\d)\1{10}$/.test(d)) return false;
    const digito = (base) => {
        let soma = 0;
        for (let i = 0; i < base; i++) soma += Number(d[i]) * (base + 1 - i);
        const resto = (soma * 10) % 11;
        return resto === 10 ? 0 : resto;
    };
    return digito(9) === Number(d[9]) && digito(10) === Number(d[10]);
}

// ====== ABRIR / FECHAR ======
function abrirAlunos(turmaId) {
    const turma = turmas.find((t) => String(t.id) === String(turmaId));
    if (!turma) return;

    alunosTurmaId = turma.id;
    alunosTurma = [];
    el("alunosTitulo").textContent = turma.nome;
    el("alunosSubtitulo").textContent = turma.codigo ? `Código ${turma.codigo}` : "Alunos matriculados nesta turma";
    limparFormAluno();

    const m = backdropAlunos();
    m.classList.add("open");
    m.style.display = "flex";
    setHTML("corpoAlunos", `<tr><td colspan="4" class="loading-state">Carregando alunos...</td></tr>`);
    carregarAlunos().then(() => el("alunoNome").focus());
}

function fecharAlunos() {
    const m = backdropAlunos();
    m.classList.remove("open");
    m.style.display = "none";
    alunosTurmaId = null;
    limparFormAluno();
}

function limparFormAluno() {
    alunoEditandoId = null;
    el("formAluno").reset();
    el("btnSalvarAluno").textContent = "Adicionar aluno";
    el("btnCancelarEdicaoAluno").hidden = true;
}

// ====== BUSCA ======
async function carregarAlunos() {
    try {
        const resultado = await api.get(`/turmas/${alunosTurmaId}/alunos`);
        alunosTurma = resultado;
    } catch (err) {
        console.error("Erro ao buscar alunos:", err);
        alunosTurma = [];
        setHTML("corpoAlunos", `<tr><td colspan="4">${estadoErro("Não foi possível carregar os alunos desta turma.")}</td></tr>`);
        return;
    }

    renderizarAlunos();
}

// ====== RENDERIZAÇÃO ======
function renderizarAlunos() {
    const total = alunosTurma.length;
    el("alunosResumo").textContent = total === 1 ? "1 aluno ativo" : `${total} alunos ativos`;

    if (!total) {
        setHTML("corpoAlunos", `<tr><td colspan="4">${estadoVazio("Esta turma ainda não tem alunos. Cadastre o primeiro acima.")}</td></tr>`);
        return;
    }

    setHTML("corpoAlunos", alunosTurma.map((a) => `
        <tr>
            <td><strong>${esc(a.nome)}</strong></td>
            <td>${esc(a.matricula || "—")}</td>
            <td>${a.cpf ? esc(formatarCPF(a.cpf)) : "—"}</td>
            <td style="text-align:right">
                <button type="button" class="btn btn-secondary btn-small" data-acao-aluno="editar" data-id="${esc(a.id)}">Editar</button>
                <button type="button" class="btn btn-secondary btn-small btn-danger" data-acao-aluno="remover" data-id="${esc(a.id)}">Remover</button>
            </td>
        </tr>`).join(""));
}

// ====== SALVAR ======
async function salvarAluno(evento) {
    evento.preventDefault();

    const nome = el("alunoNome").value.trim();
    const matricula = el("alunoMatricula").value.trim();
    const cpf = soDigitos(el("alunoCpf").value);

    if (nome.length < 3) return toast("Informe o nome do aluno (mínimo 3 caracteres).", "erro");
    if (!matricula) return toast("Informe a matrícula do aluno.", "erro");
    if (cpf && !cpfValido(cpf)) return toast("CPF inválido. Confira os 11 números.", "erro");

    const repetido = alunosTurma.find((a) =>
        String(a.id) !== String(alunoEditandoId) &&
        (String(a.matricula || "").toLowerCase() === matricula.toLowerCase() || (cpf && soDigitos(a.cpf) === cpf))
    );
    if (repetido) return toast(`Já existe um aluno com essa matrícula ou CPF nesta turma (${repetido.nome}).`, "erro");

    const dados = { nome, matricula, cpf: cpf || null };

    const botao = el("btnSalvarAluno");
    botao.disabled = true;

    try {
        if (alunoEditandoId) {
            await api.put(`/alunos/${alunoEditandoId}`, dados);
            toast("Aluno atualizado.", "sucesso");
        } else {
            await api.post(`/turmas/${alunosTurmaId}/alunos`, dados);
            toast("Aluno cadastrado.", "sucesso");
        }
        limparFormAluno();
        await carregarAlunos();
        el("alunoNome").focus();
        await carregarTurmas();
    } catch (err) {
        console.error("Erro ao salvar aluno:", err);
        toast(err.message || "Não foi possível salvar o aluno.", "erro");
    } finally {
        botao.disabled = false;
    }
}

function editarAluno(id) {
    const aluno = alunosTurma.find((a) => String(a.id) === String(id));
    if (!aluno) return;
    alunoEditandoId = aluno.id;
    el("alunoNome").value = aluno.nome || "";
    el("alunoMatricula").value = aluno.matricula || "";
    el("alunoCpf").value = aluno.cpf ? formatarCPF(aluno.cpf) : "";
    el("btnSalvarAluno").textContent = "Salvar alterações";
    el("btnCancelarEdicaoAluno").hidden = false;
    el("alunoNome").focus();
}

async function removerAluno(id) {
    const aluno = alunosTurma.find((a) => String(a.id) === String(id));
    if (!aluno) return;
    if (!confirm(`Remover "${aluno.nome}" desta turma?\n\nEle deixa de aparecer em Frequência, Notas e Relatórios. Os registros antigos são mantidos.`)) return;

    try {
        await api.delete(`/alunos/${aluno.id}`);
        toast("Aluno removido.", "sucesso");
        if (String(alunoEditandoId) === String(aluno.id)) limparFormAluno();
        await carregarAlunos();
        await carregarTurmas();
    } catch (err) {
        console.error("Erro ao remover aluno:", err);
        toast(err.message || "Não foi possível remover o aluno.", "erro");
    }
}

// ====== EVENTOS ======
document.addEventListener("DOMContentLoaded", () => {
    el("formAluno").addEventListener("submit", salvarAluno);
    el("btnCancelarEdicaoAluno").addEventListener("click", () => { limparFormAluno(); el("alunoNome").focus(); });
    el("alunosFechar").addEventListener("click", fecharAlunos);
    el("alunosConcluir").addEventListener("click", fecharAlunos);
    el("alunoCpf").addEventListener("input", (e) => { e.target.value = formatarCPF(e.target.value); });

    backdropAlunos().addEventListener("click", (e) => { if (e.target === backdropAlunos()) fecharAlunos(); });
    document.addEventListener("keydown", (e) => { if (e.key === "Escape" && backdropAlunos().classList.contains("open")) fecharAlunos(); });

    el("corpoAlunos").addEventListener("click", (e) => {
        const botao = e.target.closest("button[data-acao-aluno]");
        if (!botao) return;
        if (botao.dataset.acaoAluno === "editar") editarAluno(botao.dataset.id);
        if (botao.dataset.acaoAluno === "remover") removerAluno(botao.dataset.id);
    });
});