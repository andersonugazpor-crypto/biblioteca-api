package pe.idat.biblioteca.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record PrestamoRequest(
        @Schema(example = "2")
        @NotNull(message = "El usuario es obligatorio")
        Long usuarioId,

        @Schema(example = "1")
        @NotNull(message = "El libro es obligatorio")
        Long libroId,

        @Schema(example = "7", description = "Días de préstamo (1 a 30). Por defecto 7")
        @Min(value = 1, message = "El préstamo debe durar al menos 1 día")
        @Max(value = 30, message = "El préstamo no puede superar 30 días")
        Integer diasPrestamo,

        @Size(max = 250, message = "Las observaciones admiten máximo 250 caracteres")
        String observaciones) {
}
