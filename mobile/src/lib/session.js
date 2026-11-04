export const CHAVE_SESSAO = "portal_sessao";

export function lerSessao() {
  try {
    const bruto =
      localStorage.getItem(CHAVE_SESSAO) ||
      sessionStorage.getItem(CHAVE_SESSAO);
    return bruto ? JSON.parse(bruto) : null;
  } catch {
    return null;
  }
}

export function salvarSessao(usuario, lembrar) {
  const sessao = JSON.stringify({
    nome: usuario?.nome,
    login: usuario?.login
  });
  const armazenamento = lembrar ? localStorage : sessionStorage;
  localStorage.removeItem(CHAVE_SESSAO);
  sessionStorage.removeItem(CHAVE_SESSAO);
  armazenamento.setItem(CHAVE_SESSAO, sessao);
}

export function limparSessao() {
  localStorage.removeItem(CHAVE_SESSAO);
  sessionStorage.removeItem(CHAVE_SESSAO);
}

export function sessaoLembrada() {
  return Boolean(localStorage.getItem(CHAVE_SESSAO));
}
