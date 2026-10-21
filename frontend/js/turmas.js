// ====== ESTADO ======
let turmas = [];
let contagens = { alunos: null, ucs: null };
let editandoId = null;

const backdrop = () => el("modalBackdrop");

// ====== BUSCA NO BANCO ======
async function carregarTurmas() {
    try {
        const resultado = await api.get("/turmas");
        turmas = resultado;
    } catch (err) {
        console.error("Erro ao buscar turmas:", err);
        return mostrarErro("Não foi possível conectar ao servidor.");
    }

    contagens = { alunos: null, ucs: null };

    if (turmas.length) {
        const ids = turmas.map((t) => t.id);
        try {
            const [alunos, ucs] = await Promise.all([
                api.get(`/turmas/alunos/contagem?ids=${ids.join(",")}`),
                api.get(`/turmas/ucs/contagem?ids=${ids.join(",")}`)
            ]);
            contagens.alunos = alunos;
            contagens.ucs = ucs;
        } catch (err) {
            console.warn("Erro ao buscar contagens:", err);
        }
    }

    renderizar();
}

function mostrarErro(mensagem) {
    setHTML("cardsTurmas", estadoErro(mensagem));
    setHTML("corpoTurmas", `<tr><td colspan="6">${estadoErro(mensagem)}</td></tr>`);
    toast(mensagem, "erro");
}

// ====== RENDERIZAÇÃO ======
const numero = (grupo, id) => (grupo === null ? "—" : grupo[id] || 0);

function textoAlunos(t) {
    const cadastrados = numero(contagens.alunos, t.id);
    const previstos = t.qtdAlunos ?? null;
    if (previstos === null) return cadastrados;
    return cadastrados === "—" ? previstos : `${cadastrados}/${previstos}`;
}

const textoUcs = (t) => t.qtdUcs ?? numero(contagens.ucs, t.id);
const textoHoras = (t) => t.horas ?? "—";

function renderizar() {
    if (!turmas.length) {
        setHTML("cardsTurmas", estadoVazio("Você ainda não tem turmas. Clique em “+ Nova turma” para começar."));
        setHTML("corpoTurmas", `<tr><td colspan="6">${estadoVazio("Nenhuma turma cadastrada.")}</td></tr>`);
        return;
    }

    setHTML("cardsTurmas", turmas.map((t) => `
        <article class="card turma-card" data-turma-id="${esc(t.id)}" title="Clique para ver os alunos">
            <span class="turma-codigo">${esc(t.codigo || "Sem código")}</span>
            <h3>${esc(t.nome)}</h3>
            <p class="turma-meta">
                <strong>${textoAlunos(t)}</strong> alunos ·
                <strong>${textoUcs(t)}</strong> UCs ·
                <strong>${textoHoras(t)}</strong> horas
            </p>
            <div class="turma-links">
                <button type="button" class="btn btn-primary btn-small" data-acao="alunos" data-id="${esc(t.id)}">Alunos</button>
                <a class="btn btn-secondary btn-small" href="frequencia.html?turma=${encodeURIComponent(t.id)}">Frequência</a>
                <a class="btn btn-secondary btn-small" href="notas.html?turma=${encodeURIComponent(t.id)}">Notas</a>
            </div>
        </article>`).join(""));

    setHTML("corpoTurmas", turmas.map((t) => `
        <tr>
            <td>${esc(t.codigo || "—")}</td>
            <td><strong>${esc(t.nome)}</strong></td>
            <td>${textoAlunos(t)}</td>
            <td>${textoUcs(t)}</td>
            <td>${textoHoras(t)}</td>
            <td style="text-align:right">
                <button type="button" class="btn btn-secondary btn-small" data-acao="alunos" data-id="${esc(t.id)}">Alunos</button>
                <button type="button" class="btn btn-secondary btn-small" data-acao="editar" data-id="${esc(t.id)}">Editar</button>
                <button type="button" class="btn btn-secondary btn-small btn-danger" data-acao="excluir" data-id="${esc(t.id)}">Excluir</button>
            </td>
        </tr>`).join(""));
}

// ====== MODAL ======
function abrirModal(turma = null) {
    editandoId = turma ? turma.id : null;
    el("modalTitulo").textContent = turma ? "Editar turma" : "Nova turma";
    el("nomeTurma").value = turma ? turma.nome : "";
    el("codigoTurma").value = turma?.codigo || "";
    el("qtdAlunosTurma").value = turma?.qtdAlunos ?? "";
    el("qtdUcsTurma").value = turma?.qtdUcs ?? "";
    el("horasTurma").value = turma?.horas ?? "";

    const m = backdrop();
    m.classList.add("open");
    m.style.display = "flex";
    el("nomeTurma").focus();
}

function fecharModal() {
    const m = backdrop();
    m.classList.remove("open");
    m.style.display = "none";
    el("formTurma").reset();
    editandoId = null;
}

// ====== SALVAR / EXCLUIR ======
async function salvarTurma(evento) {
    evento.preventDefault();

    const nome = el("nomeTurma").value.trim();
    const codigo = el("codigoTurma").value.trim() || null;

    if (nome.length < 3) return toast("O nome da turma precisa ter pelo menos 3 caracteres.", "erro");

    const lerNumero = (id) => { const v = el(id).value.trim(); return v === "" ? null : Number(v); };
    const qtdAlunos = lerNumero("qtdAlunosTurma");
    const qtdUcs = lerNumero("qtdUcsTurma");
    const horas = lerNumero("horasTurma");
    for (const [valor, rotulo] of [[qtdAlunos, "quantidade de alunos"], [qtdUcs, "quantidade de UCs"], [horas, "horas"]]) {
        if (valor !== null && (!Number.isInteger(valor) || valor < 0 || valor > 9999)) {
            return toast(`Informe um número inteiro válido em ${rotulo} (de 0 a 9999).`, "erro");
        }
    }
    const atual = editandoId ? turmas.find((t) => String(t.id) === String(editandoId)) : null;
    const dados = {
        nome, codigo, qtdAlunos, qtdUcs, horas,
        curso: atual?.curso ?? null, turno: atual?.turno ?? null, sala: atual?.sala ?? null
    };

    const botao = el("modalSalvar");
    botao.disabled = true;

    let turmaCriadaId = null;
    try {
        if (editandoId) {
            await api.put(`/turmas/${editandoId}`, dados);
            toast("Turma atualizada.", "sucesso");
        } else {
            const criada = await api.post("/turmas", dados);
            toast("Turma criada.", "sucesso");
            turmaCriadaId = criada.id;
        }
        fecharModal();
        await carregarTurmas();
        if (turmaCriadaId) abrirAlunos(turmaCriadaId);
    } catch (err) {
        console.error("Erro ao salvar turma:", err);
        toast(err.message || "Não foi possível salvar a turma.", "erro");
    } finally {
        botao.disabled = false;
    }
}

async function excluirTurma(id) {
    const turma = turmas.find((t) => String(t.id) === String(id));
    if (!turma) return;

    const alunos = contagens.alunos ? contagens.alunos[turma.id] || 0 : null;
    const aviso = alunos
        ? `\n\nOs ${alunos} aluno(s), frequências, avaliações e notas desta turma também serão apagados.`
        : "\n\nAlunos, frequências, avaliações e notas desta turma também serão apagados.";

    if (!confirm(`Excluir a turma "${turma.nome}"?${aviso}\n\nEsta ação não pode ser desfeita.`)) return;

    try {
        await api.delete(`/turmas/${id}`);
        toast("Turma excluída.", "sucesso");
        await carregarTurmas();
    } catch (err) {
        console.error("Erro ao excluir turma:", err);
        toast(err.message || "Não foi possível excluir a turma.", "erro");
    }
}

// ====== EVENTOS ======
document.addEventListener("DOMContentLoaded", () => {
    el("btnNovaTurma").addEventListener("click", () => abrirModal());
    el("modalFechar").addEventListener("click", fecharModal);
    el("modalCancelar").addEventListener("click", fecharModal);
    el("formTurma").addEventListener("submit", salvarTurma);

    backdrop().addEventListener("click", (e) => { if (e.target === backdrop()) fecharModal(); });
    document.addEventListener("keydown", (e) => { if (e.key === "Escape" && backdrop().classList.contains("open")) fecharModal(); });

    el("cardsTurmas").addEventListener("click", (e) => {
        const botao = e.target.closest("button[data-acao]");
        if (botao) return abrirAlunos(botao.dataset.id);
        if (e.target.closest("a")) return;
        const card = e.target.closest(".turma-card[data-turma-id]");
        if (card) abrirAlunos(card.dataset.turmaId);
    });

    el("corpoTurmas").addEventListener("click", (e) => {
        const botao = e.target.closest("button[data-acao]");
        if (!botao) return;
        const id = botao.dataset.id;
        if (botao.dataset.acao === "alunos") abrirAlunos(id);
        if (botao.dataset.acao === "editar") {
            const turma = turmas.find((t) => String(t.id) === String(id));
            if (!turma) return toast("Turma não encontrada. Atualize a página.", "erro");
            abrirModal(turma);
        }
        if (botao.dataset.acao === "excluir") excluirTurma(id);
    });

    carregarTurmas();
});