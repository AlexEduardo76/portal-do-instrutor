import { limparSessao } from "./session";

const API_BASE = import.meta.env.VITE_API_BASE || "/api";

export async function api(
  caminho,
  { metodo = "GET", corpo = null, redirecionar401 = true } = {}
) {
  const url = `${API_BASE}${caminho}`;
  const opcoes = {
    method: metodo,
    credentials: "include",
    headers: {
      "Content-Type": "application/json",
      Accept: "application/json"
    }
  };

  if (corpo !== null) {
    opcoes.body = JSON.stringify(corpo);
  }

  const resposta = await fetch(url, opcoes);

  let dados = null;
  const texto = await resposta.text();
  if (texto) {
    try {
      dados = JSON.parse(texto);
    } catch {
      dados = null;
    }
  }

  if (resposta.status === 401 && redirecionar401) {
    limparSessao();
    window.dispatchEvent(new Event("portal:unauthorized"));
    const erro = new Error("Sessão expirada. Faça login novamente.");
    erro.status = 401;
    throw erro;
  }

  if (!resposta.ok) {
    const mensagem =
      dados?.mensagem || dados?.message || `Erro ${resposta.status}`;
    const erro = new Error(mensagem);
    erro.status = resposta.status;
    erro.codigo = dados?.codigo;
    erro.campos = dados?.campos;
    throw erro;
  }

  return dados;
}

api.get = (caminho, opcoes = {}) =>
  api(caminho, { metodo: "GET", ...opcoes });
api.post = (caminho, corpo, opcoes = {}) =>
  api(caminho, { metodo: "POST", corpo, ...opcoes });
api.put = (caminho, corpo, opcoes = {}) =>
  api(caminho, { metodo: "PUT", corpo, ...opcoes });
api.delete = (caminho, opcoes = {}) =>
  api(caminho, { metodo: "DELETE", ...opcoes });
api.patch = (caminho, corpo, opcoes = {}) =>
  api(caminho, { metodo: "PATCH", corpo, ...opcoes });
