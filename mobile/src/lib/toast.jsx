import {
  createContext,
  useCallback,
  useContext,
  useMemo,
  useRef,
  useState
} from "react";

const ToastContext = createContext(null);

export function ToastProvider({ children }) {
  const [toasts, setToasts] = useState([]);
  const proximoId = useRef(1);
  const timers = useRef(new Map());

  const remover = useCallback((id) => {
    setToasts((atuais) => atuais.filter((t) => t.id !== id));
    const timer = timers.current.get(id);
    if (timer) {
      clearTimeout(timer);
      timers.current.delete(id);
    }
  }, []);

  const toast = useCallback(
    (mensagem, tipo = "info") => {
      const id = proximoId.current++;
      setToasts((atuais) => [...atuais, { id, mensagem, tipo }]);
      const timer = setTimeout(() => {
        setToasts((atuais) => atuais.filter((t) => t.id !== id));
        timers.current.delete(id);
      }, 5000);
      timers.current.set(id, timer);
    },
    []
  );

  const valor = useMemo(() => ({ toasts, toast, remover }), [toasts, toast, remover]);

  return (
    <ToastContext.Provider value={valor}>{children}</ToastContext.Provider>
  );
}

export function useToast() {
  const contexto = useContext(ToastContext);
  if (!contexto) {
    throw new Error("useToast precisa estar dentro de ToastProvider.");
  }
  return contexto;
}
