// ====== CONFIGURAÇÃO ======
const PAGINA_PAINEL = "dashboard.html";
const CHAVE_SESSAO = "portal_sessao";
const TEXTO_BOTAO = "Entrar no Portal";

// Aquece o backend enquanto o usuário digita. Em instâncias que hibernam
// (Render free), isso evita pagar o cold start só no clique de "Entrar".
fetch("/api/auth/me", { cache: "no-store", credentials: "same-origin", priority: "low" })
    .catch(() => { /* o backend pode estar acordando; ignoramos o 401/erro */ });

// ====== FUNÇÕES AUXILIARES ======
function salvarSessao(usuario) {
    const sessao = JSON.stringify({ nome: usuario.nome, login: usuario.login });
    localStorage.removeItem(CHAVE_SESSAO);
    sessionStorage.removeItem(CHAVE_SESSAO);
    sessionStorage.setItem(CHAVE_SESSAO, sessao);
}

function mostrarErro(mensagem) {
    const caixa = document.getElementById("login-erro");
    caixa.textContent = mensagem;
    caixa.hidden = false;
}

function configurarTemaLogin() {
    const botao = document.getElementById("btn-tema-login");
    if (!botao) return;

    const atualizarRotulo = () => {
        const escuro = document.documentElement.dataset.theme === "dark";
        const rotulo = escuro ? "Ativar tema claro" : "Ativar tema escuro";
        botao.setAttribute("aria-label", rotulo);
        botao.title = rotulo;
    };

    atualizarRotulo();

    botao.addEventListener("click", () => {
        if (typeof Tema === "undefined") return;
        const escuro = document.documentElement.dataset.theme === "dark";
        Tema.aplicar(escuro ? "light" : "dark", true);
        atualizarRotulo();
    });
}

// ====== EVENTOS ======
document.addEventListener("DOMContentLoaded", () => {
    configurarTemaLogin();

    if (localStorage.getItem(CHAVE_SESSAO) || sessionStorage.getItem(CHAVE_SESSAO)) {
        window.location.replace(PAGINA_PAINEL);
        return;
    }

    const form = document.getElementById("form-login");
    const botao = form.querySelector('button[type="submit"]');
    const caixaErro = document.getElementById("login-erro");

    form.addEventListener("submit", async (e) => {
        e.preventDefault();
        if (botao.disabled) return;

        const entrada = document.getElementById("usuario").value;
        const senha = document.getElementById("senha").value;

        if (!entrada.trim() || !senha) {
            mostrarErro("Preencha usuário e senha.");
            return;
        }

        caixaErro.hidden = true;
        botao.disabled = true;
        botao.setAttribute("aria-busy", "true");
        botao.textContent = "Entrando...";

        try {
            const usuario = await api.post("/auth/login", {
                identificador: entrada,
                senha: senha,
                lembrar: false
            }, { redirecionar401: false });

            salvarSessao(usuario);
            window.location.replace(PAGINA_PAINEL);
        } catch (err) {
            mostrarErro(err.message || "Usuário ou senha incorretos.");
            botao.disabled = false;
            botao.removeAttribute("aria-busy");
            botao.textContent = TEXTO_BOTAO;
        }
    });
});
