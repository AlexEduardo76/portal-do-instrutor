package br.senac.portalinstrutor.exception;

import jakarta.validation.ConstraintViolationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.dao.PessimisticLockingFailureException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.validation.FieldError;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.HandlerMethodValidationException;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    public record ErrorResponse(
        int status,
        String codigo,
        String mensagem,
        Map<String, String> campos
    ) {}

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidation(MethodArgumentNotValidException ex) {
        Map<String, String> campos = new HashMap<>();
        for (FieldError error : ex.getBindingResult().getFieldErrors()) {
            campos.put(error.getField(), error.getDefaultMessage());
        }
        return buildResponse(HttpStatus.BAD_REQUEST, "VALIDACAO", "Dados inválidos", campos);
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ErrorResponse> handleConstraintViolation(ConstraintViolationException ex) {
        Map<String, String> campos = new HashMap<>();
        ex.getConstraintViolations().forEach(v -> {
            String path = v.getPropertyPath().toString();
            String field = path.contains(".") ? path.substring(path.lastIndexOf('.') + 1) : path;
            campos.put(field, v.getMessage());
        });
        return buildResponse(HttpStatus.BAD_REQUEST, "VALIDACAO", "Dados inválidos", campos);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ErrorResponse> handleHttpMessageNotReadable(HttpMessageNotReadableException ex) {
        return buildResponse(HttpStatus.BAD_REQUEST, "VALIDACAO", "Corpo da requisição inválido ou malformado", Map.of());
    }

    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ResponseEntity<ErrorResponse> handleMissingServletRequestParameter(MissingServletRequestParameterException ex) {
        Map<String, String> campos = new HashMap<>();
        campos.put(ex.getParameterName(), "Parâmetro obrigatório: " + ex.getParameterName());
        return buildResponse(HttpStatus.BAD_REQUEST, "VALIDACAO", "Parâmetro obrigatório ausente", campos);
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ErrorResponse> handleMethodArgumentTypeMismatch(MethodArgumentTypeMismatchException ex) {
        Map<String, String> campos = new HashMap<>();
        campos.put(ex.getName(), "Tipo inválido para parâmetro: " + ex.getName());
        return buildResponse(HttpStatus.BAD_REQUEST, "VALIDACAO", "Tipo de parâmetro inválido", campos);
    }

    @ExceptionHandler(HandlerMethodValidationException.class)
    public ResponseEntity<ErrorResponse> handleHandlerMethodValidation(HandlerMethodValidationException ex) {
        Map<String, String> campos = new HashMap<>();
        ex.getParameterValidationResults().forEach(result -> {
            String nomeParametro = result.getMethodParameter() != null
                ? result.getMethodParameter().getParameterName() : null;
            final String field = (nomeParametro != null) ? nomeParametro : "parâmetro";
            result.getResolvableErrors().forEach(err -> campos.put(field, err.getDefaultMessage()));
        });
        return buildResponse(HttpStatus.BAD_REQUEST, "VALIDACAO", "Parâmetros inválidos", campos);
    }

    @ExceptionHandler(NoResourceFoundException.class)
    public ResponseEntity<ErrorResponse> handleNoResourceFound(NoResourceFoundException ex) {
        return buildResponse(HttpStatus.NOT_FOUND, "NAO_ENCONTRADO", "Recurso não encontrado", Map.of());
    }

    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<ErrorResponse> handleMethodNotSupported(HttpRequestMethodNotSupportedException ex) {
        return buildResponse(HttpStatus.METHOD_NOT_ALLOWED, "METODO_NAO_PERMITIDO", "Método HTTP não permitido para este recurso", Map.of());
    }

    @ExceptionHandler(HttpMediaTypeNotSupportedException.class)
    public ResponseEntity<ErrorResponse> handleMediaTypeNotSupported(HttpMediaTypeNotSupportedException ex) {
        return buildResponse(HttpStatus.UNSUPPORTED_MEDIA_TYPE, "MIDIA_NAO_SUPORTADA", "Tipo de conteúdo não suportado. Use application/json.", Map.of());
    }

    @ExceptionHandler(RecursoNaoEncontradoException.class)
    public ResponseEntity<ErrorResponse> handleRecursoNaoEncontrado(RecursoNaoEncontradoException ex) {
        return buildResponse(HttpStatus.NOT_FOUND, "NAO_ENCONTRADO", ex.getMessage(), Map.of());
    }

    @ExceptionHandler(RegraNegocioException.class)
    public ResponseEntity<ErrorResponse> handleRegraNegocio(RegraNegocioException ex) {
        return buildResponse(HttpStatus.BAD_REQUEST, "REGRA_NEGOCIO", ex.getMessage(), ex.getCampos());
    }

    @ExceptionHandler(ConflitoException.class)
    public ResponseEntity<ErrorResponse> handleConflito(ConflitoException ex) {
        return buildResponse(HttpStatus.CONFLICT, "CONFLITO", ex.getMessage(), Map.of());
    }

    @ExceptionHandler(CredenciaisInvalidasException.class)
    public ResponseEntity<ErrorResponse> handleCredenciaisInvalidas(CredenciaisInvalidasException ex) {
        return buildResponse(HttpStatus.UNAUTHORIZED, "NAO_AUTENTICADO", ex.getMessage(), Map.of());
    }

    @ExceptionHandler(NaoAutenticadoException.class)
    public ResponseEntity<ErrorResponse> handleNaoAutenticado(NaoAutenticadoException ex) {
        return buildResponse(HttpStatus.UNAUTHORIZED, "NAO_AUTENTICADO", ex.getMessage(), Map.of());
    }

    @ExceptionHandler(UsuarioInativoException.class)
    public ResponseEntity<ErrorResponse> handleUsuarioInativo(UsuarioInativoException ex) {
        return buildResponse(HttpStatus.FORBIDDEN, "USUARIO_INATIVO", ex.getMessage(), Map.of());
    }

    @ExceptionHandler({OptimisticLockingFailureException.class, PessimisticLockingFailureException.class})
    public ResponseEntity<ErrorResponse> handleLocking(RuntimeException ex) {
        return buildResponse(HttpStatus.CONFLICT, "CONFLITO", "A operação entrou em conflito com outra alteração simultânea. Tente novamente.", Map.of());
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ErrorResponse> handleDataIntegrityViolation(DataIntegrityViolationException ex) {
        String mensagem = ex.getMostSpecificCause() != null ? ex.getMostSpecificCause().getMessage() : ex.getMessage();
        String msg = mensagem != null ? mensagem.toLowerCase() : "";

        if (msg.contains("uk_turma_codigo_instrutor")) {
            return buildResponse(HttpStatus.CONFLICT, "CONFLITO", "Já existe uma turma com esse código.", Map.of());
        }
        if (msg.contains("uk_aluno_turma_matricula")) {
            return buildResponse(HttpStatus.CONFLICT, "CONFLITO", "Já existe um aluno com essa matrícula nesta turma.", Map.of());
        }
        if (msg.contains("uk_aluno_turma_cpf")) {
            return buildResponse(HttpStatus.CONFLICT, "CONFLITO", "Já existe um aluno com esse CPF nesta turma.", Map.of());
        }
        if (msg.contains("uk_frequencia_aluno_data")) {
            return buildResponse(HttpStatus.CONFLICT, "CONFLITO", "Já existe frequência para este aluno nesta data.", Map.of());
        }
        if (msg.contains("uk_nota_avaliacao_aluno")) {
            return buildResponse(HttpStatus.CONFLICT, "CONFLITO", "Já existe nota para este aluno nesta avaliação.", Map.of());
        }
        if (msg.contains("not-null") || msg.contains("null value in column") || msg.contains("violates not-null")) {
            return buildResponse(HttpStatus.BAD_REQUEST, "VALIDACAO", "Campo obrigatório ausente.", Map.of());
        }
        if (msg.contains("foreign key") || msg.contains("violates foreign key")) {
            return buildResponse(HttpStatus.CONFLICT, "CONFLITO", "Operação não permitida: existem registros vinculados.", Map.of());
        }
        log.error("Violação de integridade não tratada.", ex);
        return buildResponse(HttpStatus.CONFLICT, "CONFLITO", "Não foi possível concluir a operação por conflito de dados.", Map.of());
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ErrorResponse> handleAccessDenied(AccessDeniedException ex) {
        return buildResponse(HttpStatus.FORBIDDEN, "ACESSO_NEGADO", "Acesso negado", Map.of());
    }

    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<ErrorResponse> handleAuthentication(AuthenticationException ex) {
        return buildResponse(HttpStatus.UNAUTHORIZED, "NAO_AUTENTICADO", "Usuário ou senha incorretos.", Map.of());
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGeneric(Exception ex) {
        log.error("Erro interno não tratado.", ex);
        return buildResponse(HttpStatus.INTERNAL_SERVER_ERROR, "ERRO_INTERNO", "Erro interno do servidor", Map.of());
    }

    private ResponseEntity<ErrorResponse> buildResponse(HttpStatus status, String codigo, String mensagem, Map<String, String> campos) {
        return ResponseEntity.status(status).body(new ErrorResponse(status.value(), codigo, mensagem, campos));
    }
}
