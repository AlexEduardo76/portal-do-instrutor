const API_URL = "/api";

function usuarioAtual() {
    const id = localStorage.getItem("usuarioId");
    const nome = localStorage.getItem("usuarioNome");
    const email = localStorage.getItem("usuarioEmail");
    return id ? { id: Number(id), nome: nome || "Usuário", email: email || "" } : null;
}

function salvarUsuario(usuario) {
    localStorage.setItem("usuarioId", usuario.id);
    localStorage.setItem("usuarioNome", usuario.nome);
    localStorage.setItem("usuarioEmail", usuario.email);
}

function limparSessaoLocal() {
    localStorage.removeItem("usuarioId");
    localStorage.removeItem("usuarioNome");
    localStorage.removeItem("usuarioEmail");
    localStorage.removeItem("turmaIdAtual");
}

function paginaAtual() {
    return window.location.pathname.split("/").pop() || "index.html";
}

function exigirLogin() {
    if (!usuarioAtual()) {
        window.location.href = "login.html";
        return false;
    }
    return true;
}

async function sincronizarSessao() {
    try {
        const usuario = await apiFetch("/usuarios/me", { silencioso401: true });
        salvarUsuario(usuario);
        return usuario;
    } catch {
        limparSessaoLocal();
        return null;
    }
}

async function logout() {
    try {
        await fetch(`${API_URL}/usuarios/logout`, { method: "POST", credentials: "same-origin" });
    } catch {}
    limparSessaoLocal();
    window.location.href = "login.html";
}

async function apiFetch(path, options = {}) {
    const { silencioso401, ...fetchOptions } = options;
    const headers = { ...(fetchOptions.headers || {}) };
    const config = { ...fetchOptions, headers, credentials: fetchOptions.credentials || "same-origin" };

    if (config.body && typeof config.body !== "string") {
        config.headers["Content-Type"] = "application/json";
        config.body = JSON.stringify(config.body);
    }

    const response = await fetch(`${API_URL}${path}`, config);
    const text = await response.text();
    let data = null;
    try {
        data = text ? JSON.parse(text) : null;
    } catch {
        data = text;
    }

    if (!response.ok) {
        const message = typeof data === "string" ? data : (data?.message || "Não foi possível concluir a operação.");
        if (response.status === 401 && !silencioso401) {
            limparSessaoLocal();
            if (paginaAtual() !== "login.html") {
                window.location.href = "login.html";
            }
        }
        throw new Error(message);
    }
    return data;
}

function initials(nome) {
    return (nome || "U")
        .trim()
        .split(/\s+/)
        .slice(0, 2)
        .map(parte => parte[0])
        .join("")
        .toUpperCase();
}

function setupUserArea() {
    const user = usuarioAtual();
    document.querySelectorAll("[data-user-name]").forEach(el => el.textContent = user?.nome || "Usuário");
    document.querySelectorAll("[data-user-email]").forEach(el => el.textContent = user?.email || "Instrutor");
    document.querySelectorAll("[data-user-initials]").forEach(el => el.textContent = initials(user?.nome));
    document.querySelectorAll("[data-action='logout']").forEach(el => el.addEventListener("click", logout));
}

function showToast(message, type = "success") {
    let area = document.querySelector(".toast-area");
    if (!area) {
        area = document.createElement("div");
        area.className = "toast-area";
        document.body.appendChild(area);
    }
    const toast = document.createElement("div");
    toast.className = `toast ${type}`;
    toast.style.cssText = "background:#fff;border:1px solid #e2e8f0;border-left:4px solid #10b981;border-radius:8px;box-shadow:0 10px 25px -5px rgba(0,0,0,.12);padding:12px 14px;max-width:360px;font-weight:600;color:#1e293b";
    if (type === "error") toast.style.borderLeftColor = "#ef4444";
    if (type === "warning") toast.style.borderLeftColor = "#f59e0b";
    toast.textContent = message;
    area.appendChild(toast);
    setTimeout(() => toast.remove(), 4200);
}

function escapeHtml(value) {
    return String(value ?? "").replace(/[&<>'"]/g, char => ({
        "&": "&amp;",
        "<": "&lt;",
        ">": "&gt;",
        "'": "&#39;",
        "\"": "&quot;"
    }[char]));
}

function formatarData(data) {
    if (!data) return "—";
    const [ano, mes, dia] = String(data).split("-");
    return dia && mes && ano ? `${dia}/${mes}/${ano}` : data;
}

function shellNav(active = "dashboard") {
    return `
        <div class="nav-label">Navegação</div>
        <a class="nav-link ${active === "dashboard" ? "active" : ""}" href="dashboard.html"><span class="nav-key">01</span>Visão geral</a>
        <a class="nav-link ${active === "turmas" ? "active" : ""}" href="turmas.html"><span class="nav-key">02</span>Turmas</a>
        <a class="nav-link ${active === "turma" ? "active" : ""}" href="turma.html"><span class="nav-key">03</span>Alunos e UCs</a>
        <a class="nav-link ${active === "frequencia" ? "active" : ""}" href="frequencia.html"><span class="nav-key">04</span>Frequência</a>
        <a class="nav-link ${active === "relatorio" ? "active" : ""}" href="relatorio.html"><span class="nav-key">05</span>Relatórios</a>
        <a class="nav-link ${active === "perfil" ? "active" : ""}" href="perfil.html"><span class="nav-key">06</span>Perfil</a>
    `;
}

function aplicarNavegacao(active) {
    document.querySelectorAll(".sidebar").forEach(sidebar => sidebar.innerHTML = shellNav(active));
}
