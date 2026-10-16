package br.senac.portalinstrutor.util;

public class CpfUtil {

    public static String apenasDigitos(String cpf) {
        return cpf == null ? "" : cpf.replaceAll("\\D", "");
    }

    public static boolean isValido(String cpf) {
        String digitos = apenasDigitos(cpf);
        if (digitos.length() != 11) return false;
        if (digitos.chars().distinct().count() == 1) return false;

        int digito1 = calcularDigito(digitos, 9);
        int digito2 = calcularDigito(digitos, 10);

        return digito1 == Character.getNumericValue(digitos.charAt(9))
            && digito2 == Character.getNumericValue(digitos.charAt(10));
    }

    private static int calcularDigito(String base, int pesoInicial) {
        int soma = 0;
        for (int i = 0; i < pesoInicial; i++) {
            soma += Character.getNumericValue(base.charAt(i)) * (pesoInicial + 1 - i);
        }
        int resto = (soma * 10) % 11;
        return resto == 10 ? 0 : resto;
    }
}