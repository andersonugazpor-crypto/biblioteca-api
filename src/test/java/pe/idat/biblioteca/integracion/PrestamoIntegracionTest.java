package pe.idat.biblioteca.integracion;

import com.fasterxml.jackson.databind.JsonNode;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import pe.idat.biblioteca.entidad.Libro;
import pe.idat.biblioteca.entidad.Prestamo;
import pe.idat.biblioteca.entidad.Usuario;
import pe.idat.biblioteca.repositorio.LibroRepositorio;
import pe.idat.biblioteca.repositorio.PrestamoRepositorio;
import pe.idat.biblioteca.repositorio.UsuarioRepositorio;

import java.util.Map;

import static org.hamcrest.Matchers.everyItem;
import static org.hamcrest.Matchers.is;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@DisplayName("Integración: préstamos, devoluciones e historial")
class PrestamoIntegracionTest extends ApiIntegracionBase {

    @Autowired UsuarioRepositorio usuarios;
    @Autowired LibroRepositorio libros;
    @Autowired PrestamoRepositorio prestamos;

    private Long idUsuario(String email) {
        return usuarios.findByEmailIgnoreCase(email).map(Usuario::getId).orElseThrow();
    }

    private Long idLibro(String isbn) {
        return libros.findAll().stream().filter(l -> l.getIsbn().equals(isbn)).map(Libro::getId).findFirst().orElseThrow();
    }

    private Long idPrestamoDe(String email) {
        Long usuarioId = idUsuario(email);
        return prestamos.findAll().stream().filter(p -> p.getUsuario().getId().equals(usuarioId))
                .map(Prestamo::getId).findFirst().orElseThrow();
    }

    @Test
    @DisplayName("ADMIN registra un préstamo y luego la devolución; el stock se actualiza")
    void prestamoYDevolucion() throws Exception {
        String admin = token(ADMIN, CLAVE_ADMIN);
        Long libroId = idLibro("9780073523323"); // 4 ejemplares

        String cuerpo = mvc.perform(post("/api/prestamos").header("Authorization", admin)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(Map.of("usuarioId", idUsuario(JORGE), "libroId", libroId, "diasPrestamo", 10))))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.estado").value("ACTIVO"))
                .andExpect(jsonPath("$.vencido").value(false))
                .andReturn().getResponse().getContentAsString();
        JsonNode prestamo = leer(cuerpo);

        mvc.perform(get("/api/libros/" + libroId).header("Authorization", admin))
                .andExpect(jsonPath("$.ejemplaresDisponibles").value(3));

        mvc.perform(post("/api/prestamos/" + prestamo.get("id").asLong() + "/devolucion").header("Authorization", admin)
                        .contentType(MediaType.APPLICATION_JSON).content("{}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.estado").value("DEVUELTO"))
                .andExpect(jsonPath("$.multa").value(0.0));

        mvc.perform(post("/api/prestamos/" + prestamo.get("id").asLong() + "/devolucion").header("Authorization", admin)
                        .contentType(MediaType.APPLICATION_JSON).content("{}"))
                .andExpect(status().isUnprocessableEntity());
    }

    @Test
    @DisplayName("No se presta a un usuario con préstamos vencidos: 422")
    void usuarioConVencidos() throws Exception {
        mvc.perform(post("/api/prestamos").header("Authorization", token(ADMIN, CLAVE_ADMIN))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(Map.of("usuarioId", idUsuario(MARIA), "libroId", idLibro("9780073523323")))))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.mensaje").value("El usuario tiene préstamos vencidos sin devolver"));
    }

    @Test
    @DisplayName("No se presta un libro sin ejemplares disponibles: 422")
    void sinEjemplares() throws Exception {
        String admin = token(ADMIN, CLAVE_ADMIN);
        Long microservicios = idLibro("9781492034025"); // 1 ejemplar
        mvc.perform(post("/api/prestamos").header("Authorization", admin).contentType(MediaType.APPLICATION_JSON)
                        .content(json(Map.of("usuarioId", idUsuario(JORGE), "libroId", microservicios))))
                .andExpect(status().isCreated());
        mvc.perform(post("/api/prestamos").header("Authorization", admin).contentType(MediaType.APPLICATION_JSON)
                        .content(json(Map.of("usuarioId", idUsuario(CARLOS), "libroId", microservicios))))
                .andExpect(status().isUnprocessableEntity());
    }

    @Test
    @DisplayName("USUARIO consulta solo su propio historial")
    void historialPropio() throws Exception {
        mvc.perform(get("/api/prestamos/mis-prestamos").header("Authorization", token(CARLOS, CLAVE_USUARIO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElementos").value(2))
                .andExpect(jsonPath("$.contenido[*].codigoUniversitario", everyItem(is("U20240001"))));
    }

    @Test
    @DisplayName("USUARIO no puede ver el préstamo de otra persona: 403")
    void prestamoAjeno() throws Exception {
        Long prestamoDeMaria = idPrestamoDe(MARIA);
        mvc.perform(get("/api/prestamos/" + prestamoDeMaria).header("Authorization", token(CARLOS, CLAVE_USUARIO)))
                .andExpect(status().isForbidden());
        mvc.perform(get("/api/prestamos/" + prestamoDeMaria).header("Authorization", token(MARIA, CLAVE_USUARIO)))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("ADMIN lista los préstamos vencidos con días de retraso")
    void vencidos() throws Exception {
        mvc.perform(get("/api/prestamos/vencidos").header("Authorization", token(ADMIN, CLAVE_ADMIN)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].usuarioNombre").value("María Flores Huamán"))
                .andExpect(jsonPath("$[0].vencido").value(true))
                .andExpect(jsonPath("$[0].diasRetraso").value(13));
    }

    @Test
    @DisplayName("Un usuario desactivado ya no puede usar su token: 401")
    void usuarioDesactivado() throws Exception {
        String tokenJorge = token(JORGE, CLAVE_USUARIO);
        mvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders
                        .patch("/api/usuarios/" + idUsuario(JORGE) + "/estado")
                        .header("Authorization", token(ADMIN, CLAVE_ADMIN))
                        .contentType(MediaType.APPLICATION_JSON).content("{\"activo\": false}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.activo").value(false));
        mvc.perform(get("/api/usuarios/me").header("Authorization", tokenJorge))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.mensaje").value("La cuenta del usuario está inactiva"));
    }
}
