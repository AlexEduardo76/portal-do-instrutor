document.addEventListener("DOMContentLoaded", async () => {
    if (!exigirLogin()) return;
    aplicarNavegacao("dashboard");
    setupUserArea();

    const container = document.getElementById("listaTurmasDashboard");

    function preencherStats(dashboard) {
        document.getElementById("statTurmas").textContent = dashboard.turmas ?? 0;
        document.getElementById("statAlunos").textContent = dashboard.alunos ?? 0;
        document.getElementById("statUCs").textContent = dashboard.ucs ?? 0;
        document.getElementById("statAulas").textContent = dashboard.aulas ?? 0;
        document.getElementById("statAlertas").textContent = dashboard.alertas ?? 0;
    }

    function renderizarTurmas(turmas) {
        if (!turmas.length) {
            container.innerHTML = `<div class="empty" style="grid-column:1/-1"><strong>Nenhuma turma cadastrada</strong>Crie sua primeira turma para começar a organizar alunos, UCs e frequência.<div style="margin-top:14px"><a class="btn btn-primary" href="turmas.html">Criar primeira turma</a></div></div>`;
            return;
        }

        container.innerHTML = turmas.map(turma => `
            <article class="stat">
                <div class="label">${escapeHtml(turma.codigo || "Turma")}</div>
                <div class="value" style="font-size:18px">${escapeHtml(turma.nome)}</div>
                <div class="hint">${turma.totalAlunos ?? 0} aluno(s) · ${turma.totalUcs ?? 0} UC(s) · ${turma.aulasRegistradas ?? 0} aula(s)</div>
                <div style="margin-top:14px;display:flex;gap:8px;flex-wrap:wrap">
                    <a class="btn btn-secondary btn-small" href="turma.html?turmaId=${turma.id}">Administrar</a>
                    <a class="btn btn-secondary btn-small" href="frequencia.html?turmaId=${turma.id}">Frequência</a>
                </div>
            </article>
        `).join("");
    }

    try {
        const dashboard = await apiFetch("/dashboard");
        preencherStats(dashboard);
        renderizarTurmas(dashboard.listaTurmas || []);
    } catch (error) {
        showToast(error.message, "error");
        preencherStats({ turmas: 0, alunos: 0, ucs: 0, aulas: 0, alertas: 0 });
        container.innerHTML = `<div class="empty" style="grid-column:1/-1"><strong>Não foi possível carregar o painel</strong>Verifique se o servidor está em execução e tente novamente.</div>`;
    }
});
