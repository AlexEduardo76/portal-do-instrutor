// Wrapper fetch para a API do Portal do Instrutor
// Usa credentials: "same-origin" para enviar/receber cookie JSESSIONID

const API_BASE = "/api";

async function api(caminho, { metodo = "GET", corpo = null, redirecionar401 = true } = {}) {
    const url = `${API_BASE}${caminho}`;
    const opcoes = {
        method: metodo,
        credentials: "same-origin",
        headers: {
            "Accept": "application/json"
        }
    };

    if (corpo !== null) {
        opcoes.headers["Content-Type"] = "application/json";
        opcoes.body = JSON.stringify(corpo);
    }

    // Requisições que alteram estado exigem o token CSRF enviado pelo backend
    // no cookie XSRF-TOKEN (headers são lidos pelo CookieCsrfTokenRepository).
    if (!["GET", "HEAD", "OPTIONS", "TRACE"].includes(metodo.toUpperCase())) {
        const token = lerCookie("XSRF-TOKEN");
        if (token) {
            opcoes.headers["X-XSRF-TOKEN"] = token;
        }
    }

    const resposta = await fetch(url, opcoes);

    // Alguns PUT/DELETE respondem sem corpo; não chame json() às cegas.
    let dados = null;
    const texto = await resposta.text();
    if (texto) {
        try {
            dados = JSON.parse(texto);
        } catch {
            dados = null;
        }
    }

    // 401 -> sessão expirada ou não autenticado. No login, o 401 é uma
    // credencial inválida e deve chegar ao formulário em vez de redirecionar.
    if (resposta.status === 401 && redirecionar401) {
        limparSessaoLocal();
        window.location.href = "login.html";
        const erro = new Error("Sessão expirada. Faça login novamente.");
        erro.status = 401;
        throw erro;
    }

    if (!resposta.ok) {
        const mensagem = dados?.mensagem || `Erro ${resposta.status}`;
        const erro = new Error(mensagem);
        erro.status = resposta.status;
        erro.codigo = dados?.codigo;
        erro.campos = dados?.campos;
        throw erro;
    }

    return dados;
}

function lerCookie(nome) {
    const prefixo = `${nome}=`;
    const item = document.cookie.split("; ").find((parte) => parte.startsWith(prefixo));
    return item ? decodeURIComponent(item.slice(prefixo.length)) : null;
}

function limparSessaoLocal() {
    localStorage.removeItem("portal_sessao");
    sessionStorage.removeItem("portal_sessao");
}

// Helpers para métodos comuns (opções extras, como redirecionar401, são repassadas)
api.get = (caminho, opcoes = {}) => api(caminho, { metodo: "GET", ...opcoes });
api.post = (caminho, corpo, opcoes = {}) => api(caminho, { metodo: "POST", corpo, ...opcoes });
api.put = (caminho, corpo, opcoes = {}) => api(caminho, { metodo: "PUT", corpo, ...opcoes });
api.delete = (caminho, opcoes = {}) => api(caminho, { metodo: "DELETE", ...opcoes });
api.patch = (caminho, corpo, opcoes = {}) => api(caminho, { metodo: "PATCH", corpo, ...opcoes });

// Exporta para uso global (não usa módulos ES6 para compatibilidade)
window.api = api;