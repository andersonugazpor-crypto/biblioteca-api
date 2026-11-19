package pe.idat.biblioteca.dto;

import java.time.LocalDateTime;
import java.util.Set;

/** Datos públicos del usuario (nunca incluye la contraseña). */
public record UsuarioResponse(
        Long id,
        String nombres,
        String apellidos,
        String email,
        String codigoUniversitario,
        boolean activo,
        Set<String> roles,
        LocalDateTime fechaRegistro) {
}
