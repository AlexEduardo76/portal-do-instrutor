// Wrapper fetch para a API do Portal do Instrutor
// Usa credentials: "same-origin" para enviar/receber cookie JSESSIONID

const API_BASE = "/api";

async function api(caminho, { metodo = "GET", corpo = null } = {}) {
    const url = `${API_BASE}${caminho}`;
    const opcoes = {
        method: metodo,
        credentials: "same-origin",
        headers: {
            "Content-Type": "application/json",
            "Accept": "application/json"
        }
    };

    if (corpo !== null) {
        opcoes.body = JSON.stringify(corpo);
    }

    const resposta = await fetch(url, opcoes);

    // 401 -> sessão expirada ou não autenticado
    if (resposta.status === 401) {
        limparSessaoLocal();
        window.location.href = "login.html";
        throw new Error("Sessão expirada. Faça login novamente.");
    }

    let dados = null;
    const contentType = resposta.headers.get("content-type");
    if (contentType && contentType.includes("application/json")) {
        dados = await resposta.json();
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

function limparSessaoLocal() {
    localStorage.removeItem("portal_sessao");
    sessionStorage.removeItem("portal_sessao");
}

// Helpers para métodos comuns
api.get = (caminho) => api(caminho, { metodo: "GET" });
api.post = (caminho, corpo) => api(caminho, { metodo: "POST", corpo });
api.put = (caminho, corpo) => api(caminho, { metodo: "PUT", corpo });
api.delete = (caminho) => api(caminho, { metodo: "DELETE" });
api.patch = (caminho, corpo) => api(caminho, { metodo: "PATCH", corpo });

// Exporta para uso global (não usa módulos ES6 para compatibilidade)
window.api = api;