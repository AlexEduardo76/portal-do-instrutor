// ====== ESTADO ======
let alunos = [];
let avaliacoes = [];
let cargaAtual = 0;

const formatarPercentual = (n) => n.toFixed(1).replace(".", ",") + "%";

// ====== TURMAS ======
async function carregarTurmas() {
    try {
        const data = await api.get("/turmas/resumo");
        el("turma").innerHTML = '<option value="">Selecione uma turma</option>' +
            data.map((t) => `<option value="${esc(t.id)}">${esc(t.nome)}${t.codigo ? " (" + esc(t.codigo) + ")" : ""}</option>`).join("");

        if (!data.length) return mostrarVazio("Você ainda não tem turmas cadastradas.");

        const turmaUrl = new URLSearchParams(window.location.search).get("turma");
        if (turmaUrl && data.some((t) => String(t.id) === turmaUrl)) {
            el("turma").value = turmaUrl;
            carregarAvaliacoes();
        }
    } catch (err) {
        console.error("Erro ao buscar turmas:", err);
        toast("Não foi possível carregar as turmas.", "erro");
        mostrarVazio("Não foi possível conectar ao servidor.");
    }
}

function mostrarVazio(mensagem) {
    alunos = [];
    setHTML("listaAlunos", "");
    const vazio = el("listaAlunosVazia");
    vazio.textContent = mensagem;
    vazio.hidden = false;
    el("btnSalvar").disabled = true;
}

async function carregarAvaliacoes(selecionarId = null) {
    const turmaId = el("turma").value;
    const select = el("avaliacao");

    select.innerHTML = '<option value="">Selecione uma avaliação</option>';
    select.disabled = true;
    el("btnNovaAvaliacao").disabled = !turmaId;
    el("resumoNotas").textContent = "Selecione uma turma para carregar os alunos.";

    if (!turmaId) return mostrarVazio("Nenhuma turma selecionada.");

    try {
        const data = await api.get(`/turmas/${turmaId}/avaliacoes`);
        avaliacoes = data;

        if (!data.length) {
            el("resumoNotas").textContent = "Esta turma ainda não tem avaliações.";
            return mostrarVazio("Crie uma avaliação em “+ Nova avaliação” para lançar as notas.");
        }

        select.innerHTML = '<option value="">Selecione uma avaliação</option>' +
            data.map((a) => `<option value="${esc(a.id)}">${esc(a.titulo)}</option>`).join("");
        select.disabled = false;
        select.value = String(selecionarId ?? data[0].id);

        carregarNotas();
    } catch (err) {
        console.error("Erro ao buscar avaliações:", err);
        toast("Não foi possível carregar as avaliações.", "erro");
        mostrarVazio("Erro ao carregar as avaliações desta turma.");
    }
}

async function criarAvaliacao() {
    const turmaId = el("turma").value;
    if (!turmaId) return toast("Selecione uma turma primeiro.", "erro");

    const titulo = (prompt("Nome da avaliação (ex.: Prova 1, Projeto Final):") || "").trim();
    if (!titulo) return;
    if (titulo.length < 3) return toast("O nome precisa ter pelo menos 3 caracteres.", "erro");

    try {
        const nova = await api.post(`/turmas/${turmaId}/avaliacoes`, { titulo });
        toast("Avaliação criada.", "sucesso");
        carregarAvaliacoes(nova.id);
    } catch (err) {
        console.error("Erro ao criar avaliação:", err);
        toast(err.message || "Não foi possível criar a avaliação.", "erro");
    }
}

// ====== NOTAS ======
async function carregarNotas() {
    const turmaId = el("turma").value;
    const avaliacaoId = el("avaliacao").value;
    const carga = ++cargaAtual;

    if (!turmaId || !avaliacaoId) return mostrarVazio("Selecione uma avaliação.");

    el("resumoNotas").textContent = "Carregando alunos...";
    el("listaAlunosVazia").hidden = true;

    try {
        const [resAlunos, resNotas] = await Promise.all([
            api.get(`/turmas/${turmaId}/alunos`),
            api.get(`/avaliacoes/${avaliacaoId}/notas`)
        ]);

        if (carga !== cargaAtual) return;

        alunos = resAlunos;
        const salvas = Object.fromEntries((resNotas.notas || []).map((n) => [n.alunoId, n.valor]));
        renderNotas(salvas);
    } catch (err) {
        if (carga !== cargaAtual) return;
        console.error("Erro ao carregar notas:", err);
        el("resumoNotas").textContent = "Não foi possível carregar os alunos.";
        toast("Não foi possível carregar as notas.", "erro");
        mostrarVazio("Erro ao carregar os alunos desta turma.");
    }
}

function renderNotas(salvas) {
    if (!alunos.length) {
        el("resumoNotas").textContent = "Esta turma não tem alunos ativos.";
        return mostrarVazio("Esta turma ainda não tem alunos cadastrados.");
    }

    el("listaAlunosVazia").hidden = true;

    setHTML("listaAlunos", alunos.map((a) => {
        const valor = salvas[a.id];
        return `
        <tr data-aluno-id="${esc(a.id)}">
            <td>
                <strong>${esc(a.nome)}</strong>
                ${a.matricula ? `<small class="aluno-matricula">${esc(a.matricula)}</small>` : ""}
            </td>
            <td>
                <div class="campo-nota-wrap">
                    <input type="text" class="campo-nota" inputmode="decimal" autocomplete="off"
                           placeholder="0 a 100" aria-label="Desempenho de ${esc(a.nome)} em %"
                           value="${valor === null || valor === undefined ? "" : esc(valor)}">
                    <span class="sufixo-nota">%</span>
                </div>
            </td>
        </tr>`;
    }).join(""));

    el("btnSalvar").disabled = false;
    atualizarResumo();
}

function lerLinhas() {
    let valido = true;
    const linhas = [];

    document.querySelectorAll("#listaAlunos tr[data-aluno-id]").forEach((tr) => {
        const campo = tr.querySelector(".campo-nota");
        const texto = campo.value.trim().replace(",", ".");
        let valor = null;
        let ok = true;

        if (texto !== "") {
            valor = Number(texto);
            if (Number.isNaN(valor) || valor < 0 || valor > 100) ok = false;
            else valor = Math.round(valor * 10) / 10;
        }

        tr.classList.toggle("linha-invalida", !ok);
        if (!ok) valido = false;
        linhas.push({ alunoId: Number(tr.dataset.alunoId), valor });
    });

    return valido ? linhas : null;
}

function atualizarResumo() {
    if (!alunos.length) return;

    const valores = [...document.querySelectorAll(".campo-nota")]
        .map((c) => c.value.trim().replace(",", "."))
        .filter((t) => t !== "" && !Number.isNaN(Number(t)) && Number(t) >= 0 && Number(t) <= 100)
        .map(Number);

    const lancadas = valores.length;
    const pendentes = alunos.length - lancadas;
    const media = lancadas ? valores.reduce((a, b) => a + b, 0) / lancadas : null;

    el("resumoNotas").textContent =
        `${alunos.length} aluno(s) · ${lancadas} lançada(s) · ${pendentes} pendente(s)` +
        (media === null ? "" : ` · Média ${formatarPercentual(media)}`);
}

// ====== SALVAR ======
async function salvarNotas() {
    const avaliacaoId = el("avaliacao").value;
    if (!avaliacaoId) return toast("Selecione uma avaliação.", "erro");
    if (!alunos.length) return toast("Não há alunos para registrar.", "erro");

    const linhas = lerLinhas();
    if (!linhas) return toast("Corrija os valores destacados: use números de 0 a 100.", "erro");

    const registros = linhas.map((l) => ({ alunoId: l.alunoId, valor: l.valor }));

    const botao = el("btnSalvar");
    botao.disabled = true;
    botao.textContent = "Salvando...";

    try {
        await api.put(`/avaliacoes/${avaliacaoId}/notas`, { registros });

        const lancadas = registros.filter((r) => r.valor !== null).length;
        toast(`Notas salvas: ${lancadas} lançada(s), ${registros.length - lancadas} pendente(s).`, "sucesso");
    } catch (err) {
        console.error("Erro ao salvar notas:", err);
        toast(err.message || "Não foi possível salvar as notas.", "erro");
    } finally {
        botao.disabled = false;
        botao.textContent = "Salvar notas";
    }
}

// ====== EVENTOS ======
document.addEventListener("DOMContentLoaded", () => {
    el("turma").addEventListener("change", () => carregarAvaliacoes());
    el("avaliacao").addEventListener("change", carregarNotas);
    el("btnNovaAvaliacao").addEventListener("click", criarAvaliacao);
    el("btnSalvar").addEventListener("click", salvarNotas);

    el("listaAlunos").addEventListener("input", (e) => {
        if (e.target.classList.contains("campo-nota")) {
            e.target.closest("tr").classList.remove("linha-invalida");
            atualizarResumo();
        }
    });

    mostrarVazio("Nenhuma turma selecionada.");
    carregarTurmas();
});