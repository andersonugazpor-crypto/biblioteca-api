package pe.idat.biblioteca.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CategoriaRequest(
        @Schema(example = "Inteligencia Artificial")
        @NotBlank(message = "El nombre de la categoría es obligatorio")
        @Size(min = 3, max = 60, message = "El nombre debe tener entre 3 y 60 caracteres")
        String nombre,

        @Size(max = 200, message = "La descripción admite máximo 200 caracteres")
        String descripcion) {
}
