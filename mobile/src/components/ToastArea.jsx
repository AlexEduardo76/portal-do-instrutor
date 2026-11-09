import Icon from "./Icon";
import { useToast } from "../lib/toast";

export default function ToastArea() {
  const { toasts, remover } = useToast();

  return (
    <div className="toast-area" aria-live="polite" aria-atomic="true">
      {toasts.map((t) => (
        <button
          key={t.id}
          type="button"
          className={`toast toast--${t.tipo}`}
          onClick={() => remover(t.id)}
        >
          <span className="toast-icon" aria-hidden="true">
            <Icon
              nome={t.tipo === "sucesso" ? "check" : "alerta"}
              tamanho={18}
            />
          </span>
          <span>{t.mensagem}</span>
        </button>
      ))}
    </div>
  );
}
