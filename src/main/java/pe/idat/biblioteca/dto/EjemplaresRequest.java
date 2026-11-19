package pe.idat.biblioteca.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record EjemplaresRequest(
        @Schema(example = "5")
        @NotNull(message = "La cantidad de ejemplares es obligatoria")
        @Min(value = 0, message = "La cantidad no puede ser negativa")
        @Max(value = 500, message = "Máximo 500 ejemplares por título")
        Integer ejemplaresTotales) {
}
