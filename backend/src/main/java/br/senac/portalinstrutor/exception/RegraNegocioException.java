package br.senac.portalinstrutor.exception;

import java.util.Map;

public class RegraNegocioException extends RuntimeException {
    private final Map<String, String> campos;

    public RegraNegocioException(String mensagem) {
        this(mensagem, Map.of());
    }

    public RegraNegocioException(String mensagem, Map<String, String> campos) {
        super(mensagem);
        this.campos = campos;
    }

    public Map<String, String> getCampos() {
        return campos;
    }
}