package br.com.portal.exception;
import br.com.portal.dto.ApiMessage; import org.springframework.dao.DataIntegrityViolationException; import org.springframework.http.*; import org.springframework.web.bind.MethodArgumentNotValidException; import org.springframework.web.bind.annotation.*; import java.util.stream.Collectors;
@RestControllerAdvice public class GlobalExceptionHandler {
 @ExceptionHandler(RecursoNaoEncontradoException.class) ResponseEntity<ApiMessage> notFound(RecursoNaoEncontradoException e){return ResponseEntity.status(404).body(new ApiMessage(e.getMessage()));}
 @ExceptionHandler(RegraNegocioException.class) ResponseEntity<ApiMessage> business(RegraNegocioException e){return ResponseEntity.badRequest().body(new ApiMessage(e.getMessage()));}
 @ExceptionHandler(MethodArgumentNotValidException.class) ResponseEntity<ApiMessage> validation(MethodArgumentNotValidException e){String m=e.getBindingResult().getFieldErrors().stream().map(x->x.getField()+": "+x.getDefaultMessage()).collect(Collectors.joining("; "));return ResponseEntity.badRequest().body(new ApiMessage(m));}
 @ExceptionHandler(DataIntegrityViolationException.class) ResponseEntity<ApiMessage> data(DataIntegrityViolationException e){return ResponseEntity.status(409).body(new ApiMessage("Operação recusada: existem dados relacionados ou duplicados."));}
 @ExceptionHandler(Exception.class) ResponseEntity<ApiMessage> generic(Exception e){return ResponseEntity.status(500).body(new ApiMessage("Erro interno do servidor."));}
}
