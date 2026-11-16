// ====== CONFIGURAÇÃO ======
const PAGINA_PAINEL = "dashboard.html";
const CHAVE_SESSAO = "portal_sessao";
const TEXTO_BOTAO = "Entrar no Portal";

// Aquece o backend enquanto o usuário digita. Em instâncias que hibernam
// (Render free), isso evita pagar o cold start só no clique de "Entrar".
fetch("/api/auth/me", { cache: "no-store", credentials: "same-origin", priority: "low" })
    .catch(() => { /* o backend pode estar acordando; ignoramos o 401/erro */ });

// ====== FUNÇÕES AUXILIARES ======
function salvarSessao(usuario, lembrar) {
    const sessao = JSON.stringify({ nome: usuario.nome, login: usuario.login });
    const armazenamento = lembrar ? localStorage : sessionStorage;
    localStorage.removeItem(CHAVE_SESSAO);
    sessionStorage.removeItem(CHAVE_SESSAO);
    armazenamento.setItem(CHAVE_SESSAO, sessao);
}

function mostrarErro(mensagem) {
    const caixa = document.getElementById("login-erro");
    caixa.textContent = mensagem;
    caixa.hidden = false;
}

// ====== EVENTOS ======
document.addEventListener("DOMContentLoaded", () => {
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
        const lembrar = document.getElementById("lembrar").checked;

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
                lembrar: lembrar
            }, { redirecionar401: false });

            salvarSessao(usuario, lembrar);
            window.location.replace(PAGINA_PAINEL);
        } catch (err) {
            mostrarErro(err.message || "Usuário ou senha incorretos.");
            botao.disabled = false;
            botao.removeAttribute("aria-busy");
            botao.textContent = TEXTO_BOTAO;
        }
    });
});
