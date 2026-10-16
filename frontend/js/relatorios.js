// ====== ESTADO ======
let cargaHistorico = 0;

const dataBR = (iso) => iso.split("-").reverse().join("/");
const LIMITE_HISTORICO = 200;

// ====== TURMAS ======
async function carregarTurmas() {
    try {
        const data = await api.get("/turmas/resumo");
        el("turma").innerHTML = '<option value="">Selecione uma turma</option>' +
            data.map((t) => `<option value="${esc(t.id)}">${esc(t.nome)}${t.codigo ? " (" + esc(t.codigo) + ")" : ""}</option>`).join("");

        if (!data.length) return limparHistorico("Você ainda não tem turmas cadastradas.");

        const turmaUrl = new URLSearchParams(window.location.search).get("turma");
        if (turmaUrl && data.some((t) => String(t.id) === turmaUrl)) {
            el("turma").value = turmaUrl;
            aoMudarTurma();
        }
    } catch (err) {
        console.error("Erro ao buscar turmas:", err);
        toast("Não foi possível carregar as turmas.", "erro");
        limparHistorico("Não foi possível conectar ao servidor.");
    }
}

async function aoMudarTurma() {
    const turmaId = el("turma").value;
    const select = el("aluno");

    if (!turmaId) {
        select.innerHTML = '<option value="">Selecione uma turma primeiro</option>';
        select.disabled = true;
        return limparHistorico("Selecione uma turma para visualizar as ocorrências registradas.");
    }

    select.innerHTML = '<option value="">Carregando alunos...</option>';
    select.disabled = true;

    await Promise.all([carregarAlunos(turmaId), carregarHistorico(turmaId)]);
}

// ====== ALUNOS ======
async function carregarAlunos(turmaId) {
    const select = el("aluno");

    try {
        const data = await api.get(`/turmas/${turmaId}/alunos`);
        if (el("turma").value !== turmaId) return;

        if (!data.length) {
            select.innerHTML = '<option value="">Esta turma não tem alunos</option>';
            return;
        }

        select.innerHTML = '<option value="">Selecione um aluno</option>' +
            data.map((a) => `<option value="${esc(a.id)}">${esc(a.nome)}${a.matricula ? " (" + esc(a.matricula) + ")" : ""}</option>`).join("");
        select.disabled = false;
    } catch (err) {
        console.error("Erro ao buscar alunos:", err);
        select.innerHTML = '<option value="">Erro ao carregar alunos</option>';
        toast("Não foi possível carregar os alunos.", "erro");
    }
}

// ====== HISTÓRICO ======
function limparHistorico(mensagem) {
    setHTML("listaOcorrencias", "");
    el("resumoOcorrencias").textContent = mensagem;
    el("listaOcorrenciasVazia").hidden = true;
}

async function carregarHistorico(turmaId) {
    const carga = ++cargaHistorico;
    el("resumoOcorrencias").textContent = "Carregando ocorrências...";
    el("listaOcorrenciasVazia").hidden = true;

    try {
        const data = await api.get(`/turmas/${turmaId}/ocorrencias`);
        if (carga !== cargaHistorico) return;

        if (!data.length) {
            setHTML("listaOcorrencias", "");
            el("resumoOcorrencias").textContent = "Nenhuma ocorrência registrada nesta turma.";
            const vazio = el("listaOcorrenciasVazia");
            vazio.textContent = "Nenhuma ocorrência registrada nesta turma.";
            vazio.hidden = false;
            return;
        }

        el("resumoOcorrencias").textContent =
            `${data.length} ocorrência(s) registrada(s) nesta turma` +
            (data.length >= LIMITE_HISTORICO ? ` (mostrando as ${LIMITE_HISTORICO} mais recentes)` : "");

        setHTML("listaOcorrencias", data.map((o) => `
            <tr>
                <td>${esc(dataBR(o.data))}</td>
                <td><strong>${esc(o.aluno.nome)}</strong></td>
                <td>${esc(o.aluno.matricula || "—")}</td>
                <td class="ocorrencia-texto">${esc(o.descricao)}</td>
            </tr>`).join(""));
    } catch (err) {
        if (carga !== cargaHistorico) return;
        console.error("Erro ao buscar ocorrências:", err);
        setHTML("listaOcorrencias", "");
        el("resumoOcorrencias").textContent = "Não foi possível carregar as ocorrências.";
        toast("Não foi possível carregar o histórico.", "erro");
    }
}

// ====== FORMULÁRIO ======
function atualizarContador() {
    const n = el("descricao").value.length;
    el("contadorDescricao").textContent = `Registre aqui qualquer informação relevante sobre o aluno. (${n}/2000)`;
}

function limparFormulario() {
    el("aluno").value = "";
    el("descricao").value = "";
    el("dataOcorrencia").value = hojeISO();
    atualizarContador();
}

async function salvarOcorrencia() {
    const turmaId = el("turma").value;
    const alunoId = el("aluno").value;
    const data = el("dataOcorrencia").value;
    const descricao = el("descricao").value.trim();

    if (!turmaId) return toast("Selecione uma turma.", "erro");
    if (!alunoId) return toast("Selecione o aluno.", "erro");
    if (!data) return toast("Informe a data da ocorrência.", "erro");
    if (data > hojeISO()) return toast("A data da ocorrência não pode ser no futuro.", "erro");
    if (descricao.length < 3) return toast("Descreva a ocorrência (mínimo de 3 caracteres).", "erro");

    const botao = el("btnSalvarOcorrencia");
    botao.disabled = true;
    botao.textContent = "Salvando...";

    try {
        await api.post(`/turmas/${turmaId}/ocorrencias`, { alunoId: Number(alunoId), data, descricao });
        toast("Ocorrência registrada.", "sucesso");

        const nomeAluno = el("aluno").selectedOptions[0].textContent.replace(/\s*\(.*\)$/, "");
        el("aluno").value = "";
        el("descricao").value = "";
        atualizarContador();
        await carregarHistorico(turmaId);
    } catch (err) {
        console.error("Erro ao salvar ocorrência:", err);
        toast(err.message || "Não foi possível salvar a ocorrência.", "erro");
    } finally {
        botao.disabled = false;
        botao.textContent = "Salvar ocorrência";
    }
}

// ====== EVENTOS ======
document.addEventListener("DOMContentLoaded", () => {
    const campoData = el("dataOcorrencia");
    campoData.value = hojeISO();
    campoData.max = hojeISO();

    el("turma").addEventListener("change", aoMudarTurma);
    el("descricao").addEventListener("input", atualizarContador);
    el("btnLimpar").addEventListener("click", limparFormulario);
    el("btnSalvarOcorrencia").addEventListener("click", salvarOcorrencia);

    carregarTurmas();
});