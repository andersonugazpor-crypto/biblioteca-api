package pe.idat.biblioteca.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.PastOrPresent;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

public record DevolucionRequest(
        @Schema(example = "2026-11-25", description = "Opcional. Si se omite se usa la fecha actual")
        @PastOrPresent(message = "La fecha de devolución no puede ser futura")
        LocalDate fechaDevolucion,

        @Size(max = 250, message = "Las observaciones admiten máximo 250 caracteres")
        String observaciones) {
}
