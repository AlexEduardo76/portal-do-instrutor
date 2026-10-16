// Monta a barra superior e o menu lateral (se estiverem vazios) e marca o item ativo.
const MENU = [
    { titulo: "Principal",    itens: [{ href: "dashboard.html",  icone: "⌂", texto: "Dashboard" }] },
    { titulo: "Acadêmico",    itens: [
        { href: "turmas.html",     icone: "▣", texto: "Turmas" },
        { href: "frequencia.html", icone: "✓", texto: "Frequência" },
        { href: "notas.html",      icone: "✎", texto: "Notas" },
    ] },
    { titulo: "Planejamento", itens: [{ href: "calendario.html", icone: "▦", texto: "Calendário" }] },
    { titulo: "Gestão",       itens: [{ href: "relatorios.html",  icone: "▥", texto: "Relatórios" }] },
];

function montarSidebar(sidebar) {
    const secoes = MENU.map((s) => `
        <div class="sidebar-section">
            <span class="sidebar-title">${s.titulo}</span>
            ${s.itens.map((i) => `
            <a class="nav-item" href="${i.href}">
                <span class="nav-icon" aria-hidden="true">${i.icone}</span><span>${i.texto}</span>
            </a>`).join("")}
        </div>`).join("");

    sidebar.innerHTML = `
        <nav class="sidebar-navigation">
            ${secoes}
            <div class="sidebar-footer">
                <button type="button" class="nav-item nav-logout" data-action="logout">
                    <span class="nav-icon" aria-hidden="true">↪</span><span>Sair</span>
                </button>
            </div>
        </nav>`;

    sidebar.querySelector('[data-action="logout"]').addEventListener("click", () => sair());
}

document.addEventListener("DOMContentLoaded", () => {
    const sidebar = document.querySelector(".sidebar");
    if (sidebar && !sidebar.children.length) montarSidebar(sidebar);

    const paginaAtual = window.location.pathname.split("/").pop() || "index.html";
    document.querySelectorAll(".sidebar .nav-item[href]").forEach((link) => {
        const ativo = link.getAttribute("href") === paginaAtual;
        link.classList.toggle("active", ativo);
        if (ativo) link.setAttribute("aria-current", "page");
        else link.removeAttribute("aria-current");
    });

    const topbar = document.querySelector(".topbar");
    if (topbar && !topbar.children.length) {
        topbar.innerHTML = `
            <a class="senac-brand" href="dashboard.html" aria-label="Portal do Instrutor - Dashboard">
                <img class="brand-logo" src="https://www.ap.senac.br/img/logo-white.png" alt="Senac" height="32">
                <strong>Portal do Instrutor</strong>
            </a>
            <div class="topbar-user"><span data-user-name></span></div>`;
        const usuario = typeof usuarioLogado === "function" ? usuarioLogado() : null;
        const alvo = topbar.querySelector("[data-user-name]");
        if (alvo && usuario) alvo.textContent = usuario.nome;
    }
});