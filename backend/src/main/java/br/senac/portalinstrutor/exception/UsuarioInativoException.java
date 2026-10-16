package br.senac.portalinstrutor.exception;

public class UsuarioInativoException extends RuntimeException {
    public UsuarioInativoException(String mensagem) {
        super(mensagem);
    }
}