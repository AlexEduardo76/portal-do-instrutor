import Icon from "./Icon";

export function LoadingState({ mensagem = "Carregando..." }) {
  return (
    <div className="state-view" role="status">
      <span className="spinner" aria-hidden="true" />
      <span>{mensagem}</span>
    </div>
  );
}

export function EmptyState({
  mensagem,
  titulo,
  icone = "livro",
  children
}) {
  return (
    <div className="state-view state-view--empty">
      <span className="state-icon" aria-hidden="true">
        <Icon nome={icone} tamanho={24} />
      </span>
      {titulo && <strong>{titulo}</strong>}
      <span>{mensagem}</span>
      {children}
    </div>
  );
}

export function ErrorState({
  mensagem = "Não foi possível carregar estes dados.",
  children
}) {
  return (
    <div className="state-view state-view--error" role="alert">
      <span className="state-icon" aria-hidden="true">
        <Icon nome="alerta" tamanho={24} />
      </span>
      <span>{mensagem}</span>
      {children}
    </div>
  );
}
