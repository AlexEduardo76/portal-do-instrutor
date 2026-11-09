import { createContext, useCallback, useContext, useRef, useState } from "react";
import Sheet from "./Sheet";

const ConfirmContext = createContext(null);

export function ConfirmProvider({ children }) {
  const [pedido, setPedido] = useState(null);
  const resolverRef = useRef(null);

  const confirmar = useCallback((opcoes) => {
    const configuracao =
      typeof opcoes === "string" ? { mensagem: opcoes } : opcoes || {};

    resolverRef.current?.(false);

    return new Promise((resolve) => {
      resolverRef.current = resolve;
      setPedido(configuracao);
    });
  }, []);

  function responder(resultado) {
    const resolver = resolverRef.current;
    resolverRef.current = null;
    setPedido(null);
    resolver?.(resultado);
  }

  return (
    <ConfirmContext.Provider value={confirmar}>
      {children}
      <Sheet
        id="confirmacao"
        open={Boolean(pedido)}
        onClose={() => responder(false)}
        title={pedido?.titulo || "Tem certeza?"}
        subtitle={pedido?.subtitulo}
        footer={
          <>
            <button
              type="button"
              className="btn btn-ghost"
              onClick={() => responder(false)}
            >
              {pedido?.cancelarTexto || "Cancelar"}
            </button>
            <button
              type="button"
              className={`btn ${
                pedido?.perigo ? "btn-danger" : "btn-primary"
              }`}
              onClick={() => responder(true)}
            >
              {pedido?.confirmarTexto || "Confirmar"}
            </button>
          </>
        }
      >
        <p className="confirm-text">{pedido?.mensagem}</p>
        {pedido?.detalhe && (
          <p className="confirm-detail">{pedido.detalhe}</p>
        )}
      </Sheet>
    </ConfirmContext.Provider>
  );
}

export function useConfirm() {
  const contexto = useContext(ConfirmContext);
  if (!contexto) {
    throw new Error("useConfirm precisa estar dentro de ConfirmProvider.");
  }
  return contexto;
}
