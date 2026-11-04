import Icon from "./Icon";

export default function PageHeader({
  eyebrow,
  title,
  subtitle,
  action,
  onBack,
  backLabel = "Voltar"
}) {
  return (
    <header className="page-head">
      {onBack && (
        <button type="button" className="page-back" onClick={onBack}>
          <Icon nome="voltar" tamanho={18} />
          {backLabel}
        </button>
      )}

      <div className="page-head-row">
        <div className="page-head-text">
          {eyebrow && <span className="eyebrow">{eyebrow}</span>}
          <h1>{title}</h1>
          {subtitle && <p>{subtitle}</p>}
        </div>
        {action && <div className="page-head-action">{action}</div>}
      </div>
    </header>
  );
}
