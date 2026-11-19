package pe.idat.biblioteca.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import pe.idat.biblioteca.validacion.Isbn;

import java.util.Set;

public record LibroRequest(
        @Schema(example = "978-0134685991")
        @NotBlank(message = "El ISBN es obligatorio")
        @Isbn
        @Size(max = 17, message = "El ISBN admite máximo 17 caracteres")
        String isbn,

        @Schema(example = "Effective Java")
        @NotBlank(message = "El título es obligatorio")
        @Size(max = 200, message = "El título admite máximo 200 caracteres")
        String titulo,

        @Schema(example = "Joshua Bloch")
        @NotBlank(message = "El autor es obligatorio")
        @Size(max = 120, message = "El autor admite máximo 120 caracteres")
        String autor,

        @Schema(example = "Addison-Wesley")
        @Size(max = 100, message = "La editorial admite máximo 100 caracteres")
        String editorial,

        @Schema(example = "2018")
        @Min(value = 1450, message = "El año de publicación no es válido")
        @Max(value = 2100, message = "El año de publicación no es válido")
        Integer anioPublicacion,

        @Schema(example = "3")
        @NotNull(message = "La cantidad de ejemplares es obligatoria")
        @Min(value = 1, message = "Debe haber al menos 1 ejemplar")
        @Max(value = 500, message = "Máximo 500 ejemplares por título")
        Integer ejemplaresTotales,

        @Schema(example = "[1, 2]")
        @Size(max = 5, message = "Un libro admite máximo 5 categorías")
        Set<Long> categoriaIds) {
}
