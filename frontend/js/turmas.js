let turmas = [];
let turmaEditando = null;

function abrirModalTurma(turma = null) {
    turmaEditando = turma;
    document.getElementById("modalTitulo").textContent = turma ? "Editar turma" : "Nova turma";
    document.getElementById("nomeTurma").value = turma?.nome || "";
    document.getElementById("codigoTurma").value = turma?.codigo || "";
    document.getElementById("modalBackdrop").classList.add("active");
    document.getElementById("nomeTurma").focus();
}

function fecharModalTurma() {
    turmaEditando = null;
    document.getElementById("formTurma").reset();
    document.getElementById("modalBackdrop").classList.remove("active");
}

function renderizarCards() {
    const container = document.getElementById("cardsTurmas");
    if (!turmas.length) {
        container.innerHTML = `<div class="card" style="grid-column:1/-1"><div class="empty"><strong>Nenhuma turma cadastrada</strong>Crie a primeira turma para iniciar o cadastro de alunos, unidades curriculares e frequência.</div></div>`;
        return;
    }

    container.innerHTML = turmas.map(turma => `
        <article class="stat">
            <div class="label">${escapeHtml(turma.codigo || "Turma")}</div>
            <div class="value" style="font-size:20px">${escapeHtml(turma.nome)}</div>
            <div class="hint">${turma.totalAlunos ?? 0} aluno(s) · ${turma.totalUcs ?? 0} UC(s) · ${turma.aulasRegistradas ?? 0} aula(s)</div>
            <div class="actions" style="margin-top:14px;flex-wrap:wrap">
                <a class="btn btn-secondary btn-small" href="turma.html?turmaId=${turma.id}">Administrar</a>
                <a class="btn btn-accent btn-small" href="frequencia.html?turmaId=${turma.id}">Frequência</a>
            </div>
        </article>
    `).join("");
}

function renderizarTabela() {
    const tbody = document.getElementById("corpoTurmas");
    if (!turmas.length) {
        tbody.innerHTML = `<tr><td colspan="6"><div class="empty">Nenhuma turma encontrada.</div></td></tr>`;
        return;
    }

    tbody.innerHTML = turmas.map(turma => `
        <tr>
            <td>${escapeHtml(turma.codigo || "—")}</td>
            <td><strong>${escapeHtml(turma.nome)}</strong></td>
            <td>${turma.totalAlunos ?? 0}</td>
            <td>${turma.totalUcs ?? 0}</td>
            <td>${turma.aulasRegistradas ?? 0}</td>
            <td><div class="row-actions" style="justify-content:flex-end;display:flex;gap:8px;flex-wrap:wrap">
                <a class="btn btn-secondary btn-small" href="turma.html?turmaId=${turma.id}">Abrir</a>
                <button class="btn btn-secondary btn-small" data-edit="${turma.id}">Editar</button>
                <button class="btn btn-danger btn-small" data-delete="${turma.id}">Excluir</button>
            </div></td>
        </tr>
    `).join("");

    tbody.querySelectorAll("[data-edit]").forEach(button => button.addEventListener("click", () => {
        abrirModalTurma(turmas.find(turma => Number(turma.id) === Number(button.dataset.edit)));
    }));
    tbody.querySelectorAll("[data-delete]").forEach(button => button.addEventListener("click", () => excluirTurma(Number(button.dataset.delete))));
}

async function carregarTurmas() {
    turmas = await apiFetch("/turmas");
    renderizarCards();
    renderizarTabela();
}

async function salvarTurma(event) {
    event.preventDefault();
    const body = {
        nome: document.getElementById("nomeTurma").value.trim(),
        codigo: document.getElementById("codigoTurma").value.trim() || null
    };
    const button = document.getElementById("modalSalvar");
    button.disabled = true;
    button.textContent = "Salvando...";

    try {
        const turma = turmaEditando
            ? await apiFetch(`/turmas/${turmaEditando.id}`, { method: "PUT", body })
            : await apiFetch("/turmas", { method: "POST", body });
        localStorage.setItem("turmaIdAtual", turma.id);
        fecharModalTurma();
        await carregarTurmas();
        showToast(turmaEditando ? "Turma atualizada." : "Turma criada.");
    } catch (error) {
        showToast(error.message, "error");
    } finally {
        button.disabled = false;
        button.textContent = "Salvar";
    }
}

async function excluirTurma(id) {
    const turma = turmas.find(item => Number(item.id) === Number(id));
    if (!confirm(`Excluir a turma "${turma?.nome || id}"? O sistema bloqueará se houver alunos, UCs ou histórico.`)) return;

    try {
        await apiFetch(`/turmas/${id}`, { method: "DELETE" });
        if (Number(localStorage.getItem("turmaIdAtual")) === Number(id)) localStorage.removeItem("turmaIdAtual");
        await carregarTurmas();
        showToast("Turma excluída.");
    } catch (error) {
        showToast(error.message, "error");
    }
}

document.addEventListener("DOMContentLoaded", async () => {
    if (!exigirLogin()) return;
    aplicarNavegacao("turmas");
    setupUserArea();

    document.getElementById("btnNovaTurma").addEventListener("click", () => abrirModalTurma());
    document.getElementById("modalFechar").addEventListener("click", fecharModalTurma);
    document.getElementById("modalCancelar").addEventListener("click", fecharModalTurma);
    document.getElementById("modalBackdrop").addEventListener("click", event => {
        if (event.target.id === "modalBackdrop") fecharModalTurma();
    });
    document.getElementById("formTurma").addEventListener("submit", salvarTurma);

    try {
        await sincronizarSessao();
        setupUserArea();
        await carregarTurmas();
    } catch (error) {
        showToast(error.message, "error");
        document.getElementById("cardsTurmas").innerHTML = `<div class="card" style="grid-column:1/-1"><div class="empty"><strong>Erro ao carregar turmas</strong>${escapeHtml(error.message)}</div></div>`;
    }
});
