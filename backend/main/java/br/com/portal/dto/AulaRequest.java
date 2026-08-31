package br.com.portal.dto; import jakarta.validation.Valid; import jakarta.validation.constraints.*; import java.time.LocalDate; import java.util.List;
public record AulaRequest(@NotNull Long ucId,@NotNull @Min(1) Integer numeroAula,@NotNull LocalDate data,@Size(max=500) String observacao,@NotEmpty List<@Valid PresencaRequest> presencas) {}
