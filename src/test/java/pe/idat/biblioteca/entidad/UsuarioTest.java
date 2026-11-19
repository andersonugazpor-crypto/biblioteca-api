package pe.idat.biblioteca.entidad;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DisplayName("Usuario: roles (relación muchos a muchos)")
class UsuarioTest {

    @Test
    @DisplayName("Asignar roles reemplaza los anteriores")
    void roles() {
        Usuario u = new Usuario("Ana", "Torres", "ana@idat.edu.pe", "ADM000001", "hash");
        u.asignarRoles(Set.of(new Rol(NombreRol.USUARIO, null)));
        assertTrue(u.tieneRol(NombreRol.USUARIO));
        u.asignarRoles(Set.of(new Rol(NombreRol.ADMIN, null)));
        assertTrue(u.tieneRol(NombreRol.ADMIN));
        assertFalse(u.tieneRol(NombreRol.USUARIO));
        assertEquals("Ana Torres", u.getNombreCompleto());
    }
}
