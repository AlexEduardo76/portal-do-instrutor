// Monta a barra superior e o menu lateral, e abre o card de perfil sobreposto.
const svgIcone = (conteudo) => `<svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.8"
    stroke-linecap="round" stroke-linejoin="round" aria-hidden="true" focusable="false">${conteudo}</svg>`;

const ICONES = {
    dashboard: svgIcone(`<rect x="3" y="3" width="7" height="7" rx="1.5"></rect>
        <rect x="14" y="3" width="7" height="7" rx="1.5"></rect>
        <rect x="3" y="14" width="7" height="7" rx="1.5"></rect>
        <rect x="14" y="14" width="7" height="7" rx="1.5"></rect>`),
    escola: svgIcone(`<path d="M3 21h18"></path>
        <path d="M5 21V8l7-4 7 4v13"></path>
        <path d="M9 21v-5h6v5"></path>
        <path d="M9 11h.01M12 11h.01M15 11h.01M9 14h.01M12 14h.01M15 14h.01"></path>`),
    frequencia: svgIcone(`<rect x="3" y="3" width="18" height="18" rx="3"></rect>
        <path d="m8 12 2.5 2.5L16 9"></path>`),
    calendario: svgIcone(`<rect x="3" y="4" width="18" height="17" rx="3"></rect>
        <path d="M8 2v4M16 2v4M3 9h18"></path>`),
    grafico: svgIcone(`<path d="M4 20V10M10 20V4M16 20v-7M22 20H2"></path>`),
    ocorrencias: svgIcone(`<rect x="5" y="4" width="14" height="17" rx="2"></rect>
        <path d="M9 4V3h6v1"></path>
        <path d="M9 10h6M9 14h6M9 18h4"></path>`),
    perfil: svgIcone(`<circle cx="12" cy="8" r="4"></circle>
        <path d="M4 21c0-4 3.6-7 8-7s8 3 8 7"></path>`),
    sair: svgIcone(`<path d="M9 21H5a2 2 0 0 1-2-2V5a2 2 0 0 1 2-2h4"></path>
        <path d="m16 17 5-5-5-5"></path><path d="M21 12H9"></path>`),
    camera: svgIcone(`<path d="M4 7h3l2-3h6l2 3h3a1 1 0 0 1 1 1v11a1 1 0 0 1-1 1H4a1 1 0 0 1-1-1V8a1 1 0 0 1 1-1Z"></path>
        <circle cx="12" cy="13" r="4"></circle>`),
    lixeira: svgIcone(`<path d="M4 7h16"></path>
        <path d="M9 7V5a1 1 0 0 1 1-1h4a1 1 0 0 1 1 1v2"></path>
        <path d="M6 7l1 13a1 1 0 0 0 1 1h8a1 1 0 0 0 1-1l1-13"></path>
        <path d="M10 11v6M14 11v6"></path>`),
    fechar: svgIcone(`<path d="m6 6 12 12M18 6 6 18"></path>`),
    sol: svgIcone(`<circle cx="12" cy="12" r="4"></circle>
        <path d="M12 2v2M12 20v2M4.93 4.93l1.42 1.42M17.66 17.66l1.41 1.41M2 12h2M20 12h2M4.93 19.07l1.42-1.42M17.66 6.34l1.41-1.41"></path>`),
    lua: svgIcone(`<path d="M20.5 14.5A8.5 8.5 0 0 1 9.5 3.5 8.5 8.5 0 1 0 20.5 14.5Z"></path>`),
    monitor: svgIcone(`<rect x="3" y="4" width="18" height="12" rx="2"></rect>
        <path d="M8 20h8M12 16v4"></path>`)
};

const MENU = [
    { titulo: "Principal",    itens: [{ href: "dashboard.html",  icone: "dashboard", texto: "Dashboard" }] },
    { titulo: "Acadêmico",    itens: [
        { href: "turmas.html",     icone: "escola",     texto: "Turmas" },
        { href: "frequencia.html", icone: "frequencia", texto: "Frequência" },
    ] },
    { titulo: "Planejamento", itens: [{ href: "calendario.html", icone: "calendario", texto: "Calendário" }] },
    { titulo: "Gestão",       itens: [
        { href: "frequencias.html", icone: "grafico",     texto: "Frequência geral" },
        { href: "relatorios.html",  icone: "ocorrencias", texto: "Ocorrências" },
    ] },
    { titulo: "Conta",        itens: [{ acao: "abrir-perfil", icone: "perfil", texto: "Meu perfil" }] },
];

function itemMenu(item) {
    const conteudo = `
        <span class="nav-icon" aria-hidden="true">${ICONES[item.icone] || ""}</span>
        <span>${item.texto}</span>`;

    return item.acao
        ? `<button type="button" class="nav-item" data-action="${item.acao}">${conteudo}</button>`
        : `<a class="nav-item" href="${item.href}">${conteudo}</a>`;
}

function montarSidebar(sidebar) {
    const secoes = MENU.map((s) => `
        <div class="sidebar-section">
            <span class="sidebar-title">${s.titulo}</span>
            ${s.itens.map(itemMenu).join("")}
        </div>`).join("");

    sidebar.innerHTML = `
        <nav class="sidebar-navigation">
            ${secoes}
            <div class="sidebar-footer">
                <button type="button" class="nav-item nav-logout" data-action="logout">
                    <span class="nav-icon" aria-hidden="true">${ICONES.sair}</span><span>Sair</span>
                </button>
            </div>
        </nav>`;
}

function montarTopbar(topbar) {
    topbar.innerHTML = `
        <a class="senac-brand" href="dashboard.html" aria-label="Portal do Instrutor - Dashboard">
            <img class="brand-logo" src="img/logo-senac.png" alt="Senac" height="32">
            <strong>Portal do Instrutor</strong>
        </a>
        <button type="button" class="topbar-user topbar-profile" data-action="abrir-perfil"
                aria-label="Abrir meu perfil" aria-haspopup="dialog">
            <strong class="topbar-profile-nome" data-user-name>Usuário</strong>
            <span class="avatar" data-user-avatar aria-hidden="true">PI</span>
        </button>`;
}

function formatarCpfPerfil(valor) {
    const digitos = String(valor || "").replace(/\D/g, "");
    if (digitos.length !== 11) return valor || "---";
    return digitos.replace(/(\d{3})(\d{3})(\d{3})(\d{2})/, "$1.$2.$3-$4");
}

function montarPainelPerfil() {
    if (document.querySelector("[data-perfil-overlay]")) return;

    const overlay = document.createElement("div");
    overlay.className = "perfil-overlay";
    overlay.dataset.perfilOverlay = "";
    overlay.hidden = true;
    overlay.innerHTML = `
        <div class="perfil-panel" role="dialog" aria-modal="true" aria-labelledby="perfilTitulo" tabindex="-1">
            <div class="perfil-panel-topo">
                <div class="perfil-panel-identidade">
                    <div class="perfil-foto">
                        <span class="avatar avatar--grande" data-perfil-avatar>PI</span>
                        <button type="button" class="perfil-foto-btn" data-action="trocar-foto"
                                aria-label="Alterar foto de perfil" title="Alterar foto">
                            ${ICONES.camera}
                        </button>
                        <button type="button" class="perfil-foto-btn perfil-foto-remover" data-action="remover-foto"
                                aria-label="Remover foto de perfil" title="Remover foto" hidden>
                            ${ICONES.lixeira}
                        </button>
                        <input type="file" accept="image/png,image/jpeg,image/webp" data-perfil-foto-input hidden>
                    </div>
                    <div class="perfil-panel-texto">
                        <span class="eyebrow">MINHA CONTA</span>
                        <h2 id="perfilTitulo" data-perfil-nome>Carregando...</h2>
                        <span data-perfil-login>---</span>
                    </div>
                </div>
                <button type="button" class="perfil-fechar" data-action="fechar-perfil" aria-label="Fechar perfil">
                    ${ICONES.fechar}
                </button>
            </div>

            <div class="perfil-panel-corpo">
                <div class="perfil-info">
                    <div><span>E-mail</span><strong data-perfil-email>---</strong></div>
                    <div><span>CPF</span><strong data-perfil-cpf>---</strong></div>
                    <div><span>Situação</span><strong><span class="badge badge-success" data-perfil-status>Ativo</span></strong></div>
                </div>

                <form class="perfil-form-modal" id="formPerfilModal" novalidate>
                    <label for="perfilNomeModal">Nome do instrutor</label>
                    <div class="perfil-nome-linha">
                        <input type="text" id="perfilNomeModal" name="nome" autocomplete="name" maxlength="120" required>
                        <button type="submit" class="btn btn-primary btn-small">Salvar</button>
                    </div>
                    <p class="form-hint">Somente o nome pode ser alterado. E-mail, CPF e login são definidos pelo Senac.</p>
                </form>

                <div class="perfil-tema">
                    <span class="perfil-secao-titulo">Aparência</span>
                    <div class="tema-opcoes" role="radiogroup" aria-label="Tema da interface">
                        <button type="button" class="tema-opcao" data-tema="light" role="radio" aria-checked="false">
                            ${ICONES.sol}<span>Claro</span>
                        </button>
                        <button type="button" class="tema-opcao" data-tema="dark" role="radio" aria-checked="false">
                            ${ICONES.lua}<span>Escuro</span>
                        </button>
                        <button type="button" class="tema-opcao" data-tema="system" role="radio" aria-checked="false">
                            ${ICONES.monitor}<span>Sistema</span>
                        </button>
                    </div>
                </div>

                <button type="button" class="btn btn-secondary perfil-sair" data-action="logout">Sair da conta</button>
            </div>
        </div>`;

    document.body.appendChild(overlay);

    const form = overlay.querySelector("#formPerfilModal");
    form.addEventListener("submit", salvarNomePerfil);
    overlay.dataset.pronto = "1";
}

let perfilAtual = null;

function preencherPainelPerfil(perfil) {
    if (!perfil) return;

    document.querySelectorAll("[data-perfil-nome]").forEach((el) => (el.textContent = perfil.nome || "---"));
    document.querySelectorAll("[data-perfil-login]").forEach((el) => (el.textContent = perfil.login || "---"));
    document.querySelectorAll("[data-perfil-email]").forEach((el) => (el.textContent = perfil.email || "---"));
    document.querySelectorAll("[data-perfil-cpf]").forEach((el) => (el.textContent = formatarCpfPerfil(perfil.cpf)));
    document.querySelectorAll("[data-user-name]").forEach((el) => (el.textContent = perfil.nome || "Usuário"));
    document.querySelectorAll("[data-user-avatar], [data-perfil-avatar]").forEach((el) => montarAvatar(el, perfil.nome));

    const status = document.querySelector("[data-perfil-status]");
    if (status) {
        const ativo = perfil.ativo !== false;
        status.textContent = ativo ? "Ativo" : "Inativo";
        status.classList.toggle("badge-success", ativo);
        status.classList.toggle("badge-danger", !ativo);
    }

    const nomeInput = document.getElementById("perfilNomeModal");
    if (nomeInput) nomeInput.value = perfil.nome || "";

    atualizarBotaoRemoverFoto();
}

async function carregarPerfilPainel() {
    const usuario = usuarioLogado();
    if (usuario) preencherPainelPerfil({ nome: usuario.nome, login: usuario.login });

    try {
        const perfil = await api.get("/perfil");
        perfilAtual = perfil;
        preencherPainelPerfil(perfil);
    } catch (erro) {
        if (erro.status !== 401) console.warn("Não foi possível carregar o perfil:", erro);
    }
}

function atualizarBotaoRemoverFoto() {
    const botao = document.querySelector('[data-action="remover-foto"]');
    if (botao) botao.hidden = !fotoPerfilSalva();
}

function abrirPainelPerfil() {
    montarPainelPerfil();
    const overlay = document.querySelector("[data-perfil-overlay]");
    if (!overlay) return;

    overlay.hidden = false;
    document.body.classList.add("perfil-aberto");
    atualizarAvatares();
    atualizarBotaoRemoverFoto();
    if (typeof Tema !== "undefined") Tema.aplicar(Tema.preferido());
    overlay.querySelector(".perfil-panel")?.focus();
    carregarPerfilPainel();
}

function fecharPainelPerfil() {
    const overlay = document.querySelector("[data-perfil-overlay]");
    if (!overlay) return;
    overlay.hidden = true;
    document.body.classList.remove("perfil-aberto");
}

function redimensionarFoto(arquivo) {
    if (!arquivo) return;

    if (!arquivo.type.startsWith("image/")) {
        if (typeof toast === "function") toast("Selecione um arquivo de imagem.", "erro");
        return;
    }

    const leitor = new FileReader();
    leitor.onload = () => {
        const imagem = new Image();
        imagem.onload = () => {
            const tamanho = 320;
            const canvas = document.createElement("canvas");
            canvas.width = tamanho;
            canvas.height = tamanho;
            const ctx = canvas.getContext("2d");
            const lado = Math.min(imagem.width, imagem.height);
            ctx.drawImage(
                imagem,
                (imagem.width - lado) / 2,
                (imagem.height - lado) / 2,
                lado,
                lado,
                0,
                0,
                tamanho,
                tamanho
            );
            aplicarFotoPerfil(canvas.toDataURL("image/jpeg", 0.86));
            atualizarBotaoRemoverFoto();
            if (typeof toast === "function") toast("Foto de perfil atualizada.", "sucesso");
        };
        imagem.src = leitor.result;
    };
    leitor.readAsDataURL(arquivo);
}

async function salvarNomePerfil(evento) {
    evento.preventDefault();

    const form = evento.currentTarget;
    const input = document.getElementById("perfilNomeModal");
    const botao = form.querySelector('button[type="submit"]');
    if (!input || !botao) return;

    const nome = input.value.trim();
    if (nome.length < 3) {
        if (typeof toast === "function") toast("Informe um nome com pelo menos 3 caracteres.", "erro");
        input.focus();
        return;
    }

    botao.disabled = true;
    botao.textContent = "Salvando...";

    try {
        const perfil = await api.put("/perfil", { nome });
        perfilAtual = perfil;
        preencherPainelPerfil(perfil);
        atualizarSessaoNome(perfil.nome);
        atualizarIdentidadeLocal({ nome: perfil.nome, login: perfil.login });
        if (typeof toast === "function") toast("Nome atualizado com sucesso.", "sucesso");
    } catch (erro) {
        if (erro.status !== 401 && typeof toast === "function") {
            toast(erro.message || "Não foi possível salvar o nome.", "erro");
        }
    } finally {
        botao.disabled = false;
        botao.textContent = "Salvar";
    }
}

document.addEventListener("click", (evento) => {
    if (evento.target.closest('[data-action="abrir-perfil"]')) {
        evento.preventDefault();
        abrirPainelPerfil();
        return;
    }

    if (evento.target.closest('[data-action="fechar-perfil"]')) {
        evento.preventDefault();
        fecharPainelPerfil();
        return;
    }

    if (evento.target.closest('[data-action="trocar-foto"]')) {
        const input = document.querySelector("[data-perfil-foto-input]");
        if (input) input.click();
        return;
    }

    if (evento.target.closest('[data-action="remover-foto"]')) {
        if (!fotoPerfilSalva()) return;
        if (!window.confirm("Remover a foto de perfil?")) return;
        aplicarFotoPerfil(null);
        atualizarBotaoRemoverFoto();
        if (typeof toast === "function") toast("Foto de perfil removida.", "sucesso");
        return;
    }

    const overlay = evento.target.closest("[data-perfil-overlay]");
    if (overlay && evento.target === overlay) {
        fecharPainelPerfil();
        return;
    }

    const botaoTema = evento.target.closest("[data-perfil-overlay] [data-tema]");
    if (botaoTema && typeof Tema !== "undefined") {
        const preferencia = Tema.aplicar(botaoTema.dataset.tema, true);
        const rotulos = { light: "Tema claro", dark: "Tema escuro", system: "Tema do sistema" };
        if (typeof toast === "function") toast(`${rotulos[preferencia]} aplicado.`, "sucesso");
    }
});

document.addEventListener("change", (evento) => {
    if (evento.target.matches("[data-perfil-foto-input]")) {
        redimensionarFoto(evento.target.files?.[0]);
        evento.target.value = "";
    }
});

document.addEventListener("keydown", (evento) => {
    if (evento.key === "Escape") fecharPainelPerfil();
});

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
        montarTopbar(topbar);
        const usuario = typeof usuarioLogado === "function" ? usuarioLogado() : null;
        if (usuario) atualizarIdentidadeLocal(usuario);
    }

    montarPainelPerfil();
});
