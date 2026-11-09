import { useEffect, useRef } from "react";
import Icon from "./Icon";

export default function Sheet({
  open,
  onClose,
  title,
  subtitle,
  children,
  footer,
  full = false,
  id = "sheet"
}) {
  const painelRef = useRef(null);
  const inicioRef = useRef(null);
  const fecharRef = useRef(onClose);

  useEffect(() => {
    fecharRef.current = onClose;
  }, [onClose]);

  useEffect(() => {
    if (!open) return undefined;

    const overflowAnterior = document.body.style.overflow;
    document.body.style.overflow = "hidden";

    const aoTeclar = (evento) => {
      if (evento.key === "Escape") {
        fecharRef.current();
        return;
      }

      if (evento.key !== "Tab" || !painelRef.current) return;
      const focaveis = painelRef.current.querySelectorAll(
        'button, [href], input, select, textarea, [tabindex]:not([tabindex="-1"])'
      );
      if (!focaveis.length) return;

      const primeiro = focaveis[0];
      const ultimo = focaveis[focaveis.length - 1];

      if (evento.shiftKey && document.activeElement === primeiro) {
        evento.preventDefault();
        ultimo.focus();
      } else if (!evento.shiftKey && document.activeElement === ultimo) {
        evento.preventDefault();
        primeiro.focus();
      }
    };

    document.addEventListener("keydown", aoTeclar);
    const timer = window.setTimeout(() => {
      const alvo = inicioRef.current?.querySelector(
        "input:not([type=hidden]), select, textarea, button"
      );
      (alvo || inicioRef.current)?.focus();
    }, 60);

    return () => {
      document.removeEventListener("keydown", aoTeclar);
      document.body.style.overflow = overflowAnterior;
      window.clearTimeout(timer);
    };
  }, [open]);

  if (!open) return null;

  return (
    <div
      className="sheet-layer"
      onMouseDown={(evento) => {
        if (evento.target === evento.currentTarget) onClose();
      }}
    >
      <div
        ref={painelRef}
        className={`sheet-panel${full ? " sheet-panel--full" : ""}`}
        role="dialog"
        aria-modal="true"
        aria-labelledby={`${id}-titulo`}
      >
        <span className="sheet-grab" aria-hidden="true" />

        <header className="sheet-head">
          <div className="sheet-head-text">
            <h2 id={`${id}-titulo`}>{title}</h2>
            {subtitle && <p>{subtitle}</p>}
          </div>
          <button
            type="button"
            className="icon-button"
            onClick={onClose}
            aria-label="Fechar"
          >
            <Icon nome="fechar" tamanho={20} />
          </button>
        </header>

        <div className="sheet-body" ref={inicioRef} tabIndex={-1}>
          {children}
        </div>

        {footer && <div className="sheet-foot">{footer}</div>}
      </div>
    </div>
  );
}
