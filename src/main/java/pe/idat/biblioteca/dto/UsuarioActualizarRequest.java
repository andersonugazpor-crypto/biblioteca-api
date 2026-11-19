package pe.idat.biblioteca.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import pe.idat.biblioteca.entidad.NombreRol;

import java.util.Set;

public record UsuarioActualizarRequest(
        @NotBlank(message = "Los nombres son obligatorios") @Size(max = 80) String nombres,
        @NotBlank(message = "Los apellidos son obligatorios") @Size(max = 80) String apellidos,
        @NotBlank(message = "El email es obligatorio") @Email(message = "El email no tiene un formato válido") @Size(max = 120) String email,
        @NotEmpty(message = "Debe asignar al menos un rol") Set<NombreRol> roles) {
}
