package br.senac.portalinstrutor.util;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class CpfUtilTest {

    @Test
    void removeMascaraMantendoApenasDigitos() {
        assertThat(CpfUtil.apenasDigitos("529.982.247-25")).isEqualTo("52998224725");
        assertThat(CpfUtil.apenasDigitos(null)).isEmpty();
        assertThat(CpfUtil.apenasDigitos(" 123 abc 456 ")).isEqualTo("123456");
    }

    @Test
    void aceitaCpfValidoComOuSemMascara() {
        assertThat(CpfUtil.isValido("52998224725")).isTrue();
        assertThat(CpfUtil.isValido("529.982.247-25")).isTrue();
    }

    @Test
    void rejeitaCpfComTamanhoInvalido() {
        assertThat(CpfUtil.isValido("123")).isFalse();
        assertThat(CpfUtil.isValido("123456789012")).isFalse();
    }

    @Test
    void rejeitaCpfComTodosOsDigitosIguais() {
        assertThat(CpfUtil.isValido("11111111111")).isFalse();
        assertThat(CpfUtil.isValido("00000000000")).isFalse();
    }

    @Test
    void rejeitaCpfComDigitoVerificadorErrado() {
        assertThat(CpfUtil.isValido("52998224724")).isFalse();
        assertThat(CpfUtil.isValido("12345678900")).isFalse();
    }
}
