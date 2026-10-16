package br.senac.portalinstrutor.util;

public final class TextoUtil {

    private TextoUtil() {
    }

    public static String trim(String valor) {
        return valor == null ? null : valor.trim();
    }

    public static String trimParaNulo(String valor) {
        if (valor == null) {
            return null;
        }
        String limpo = valor.trim();
        return limpo.isEmpty() ? null : limpo;
    }
}
