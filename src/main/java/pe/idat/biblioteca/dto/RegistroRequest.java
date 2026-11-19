package pe.idat.biblioteca.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/** Autorregistro de un estudiante o docente: siempre recibe el rol USUARIO. */
public record RegistroRequest(
        @Schema(example = "Lucía")
        @NotBlank(message = "Los nombres son obligatorios")
        @Size(max = 80, message = "Los nombres admiten máximo 80 caracteres")
        String nombres,

        @Schema(example = "Ramírez Soto")
        @NotBlank(message = "Los apellidos son obligatorios")
        @Size(max = 80, message = "Los apellidos admiten máximo 80 caracteres")
        String apellidos,

        @Schema(example = "lucia.ramirez@idat.edu.pe")
        @NotBlank(message = "El email es obligatorio")
        @Email(message = "El email no tiene un formato válido")
        @Size(max = 120)
        String email,

        @Schema(example = "U20251234")
        @NotBlank(message = "El código universitario es obligatorio")
        @Pattern(regexp = "^[A-Za-z0-9]{6,20}$", message = "El código universitario debe tener de 6 a 20 letras o números")
        String codigoUniversitario,

        @Schema(example = "Clave2025*")
        @NotBlank(message = "La contraseña es obligatoria")
        @Size(min = 8, max = 60, message = "La contraseña debe tener entre 8 y 60 caracteres")
        @Pattern(regexp = "^(?=.*[A-Za-z])(?=.*\\d).+$", message = "La contraseña debe contener letras y números")
        String password) {
}
