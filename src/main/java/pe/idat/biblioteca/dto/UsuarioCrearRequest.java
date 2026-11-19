package pe.idat.biblioteca.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import pe.idat.biblioteca.entidad.NombreRol;

import java.util.Set;

/** Alta de usuarios por un administrador, con roles explícitos. */
public record UsuarioCrearRequest(
        @NotBlank(message = "Los nombres son obligatorios") @Size(max = 80) String nombres,
        @NotBlank(message = "Los apellidos son obligatorios") @Size(max = 80) String apellidos,
        @NotBlank(message = "El email es obligatorio") @Email(message = "El email no tiene un formato válido") @Size(max = 120) String email,
        @NotBlank(message = "El código universitario es obligatorio")
        @Pattern(regexp = "^[A-Za-z0-9]{6,20}$", message = "El código universitario debe tener de 6 a 20 letras o números")
        String codigoUniversitario,
        @NotBlank(message = "La contraseña es obligatoria")
        @Size(min = 8, max = 60, message = "La contraseña debe tener entre 8 y 60 caracteres")
        @Pattern(regexp = "^(?=.*[A-Za-z])(?=.*\\d).+$", message = "La contraseña debe contener letras y números")
        String password,
        @Schema(example = "[\"USUARIO\"]")
        @NotEmpty(message = "Debe asignar al menos un rol")
        Set<NombreRol> roles) {
}
