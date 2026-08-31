package br.com.portal.exception;

public class SessaoExpiradaException extends RuntimeException {
    public SessaoExpiradaException() {
        super("Sessão expirada. Faça login novamente.");
    }
}
