package br.senac.portalinstrutor.service;

import br.senac.portalinstrutor.exception.MuitasTentativasException;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class LoginRateLimiter {

    private static final int MAX_TENTATIVAS = 5;
    private static final Duration JANELA = Duration.ofMinutes(15);

    private final Map<String, Deque<Instant>> falhas = new ConcurrentHashMap<>();

    public void verificar(HttpServletRequest request, String identificador) {
        String chave = chave(request, identificador);
        Instant limite = Instant.now().minus(JANELA);
        Deque<Instant> tentativas = falhas.get(chave);
        if (tentativas == null) {
            return;
        }
        synchronized (tentativas) {
            while (!tentativas.isEmpty() && tentativas.peekFirst().isBefore(limite)) {
                tentativas.removeFirst();
            }
            if (tentativas.size() >= MAX_TENTATIVAS) {
                throw new MuitasTentativasException(
                    "Muitas tentativas de login. Aguarde alguns minutos e tente novamente.");
            }
        }
    }

    public void registrarFalha(HttpServletRequest request, String identificador) {
        String chave = chave(request, identificador);
        Deque<Instant> tentativas = falhas.computeIfAbsent(chave, k -> new ArrayDeque<>());
        synchronized (tentativas) {
            tentativas.addLast(Instant.now());
        }
    }

    public void limpar(HttpServletRequest request, String identificador) {
        falhas.remove(chave(request, identificador));
    }

    private String chave(HttpServletRequest request, String identificador) {
        String origem = request.getRemoteAddr() != null ? request.getRemoteAddr() : "desconhecido";
        return origem + "|" + identificador.trim().toLowerCase();
    }
}
