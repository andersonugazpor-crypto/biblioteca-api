package pe.idat.biblioteca.integracion;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;

import java.util.List;
import java.util.Map;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.startsWith;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@DisplayName("Integración: control de acceso por rol")
class SeguridadIntegracionTest extends ApiIntegracionBase {

    private Map<String, Object> libroNuevo() {
        return Map.of("isbn", "978-1-4919-5038-8", "titulo", "Learning Spring Boot 3", "autor", "Greg Turnquist",
                "editorial", "Packt", "anioPublicacion", 2022, "ejemplaresTotales", 2, "categoriaIds", List.of());
    }

    @Test
    @DisplayName("Sin token: 401 con mensaje personalizado")
    void sinToken() throws Exception {
        mvc.perform(get("/api/libros"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.status").value(401))
                .andExpect(jsonPath("$.mensaje", containsString("Se requiere autenticación")));
    }

    @Test
    @DisplayName("Token alterado: 401 'Token inválido'")
    void tokenInvalido() throws Exception {
        mvc.perform(get("/api/libros").header("Authorization", "Bearer abc.def.ghi"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.mensaje", startsWith("Token inválido")));
    }

    @Test
    @DisplayName("USUARIO puede consultar el catálogo paginado")
    void usuarioConsultaCatalogo() throws Exception {
        mvc.perform(get("/api/libros").param("texto", "java").header("Authorization", token(CARLOS, CLAVE_USUARIO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.contenido[0].titulo").value("Effective Java"))
                .andExpect(jsonPath("$.totalElementos").value(1));
    }

    @Test
    @DisplayName("USUARIO no puede registrar libros: 403")
    void usuarioNoCreaLibros() throws Exception {
        mvc.perform(post("/api/libros").header("Authorization", token(CARLOS, CLAVE_USUARIO))
                        .contentType(MediaType.APPLICATION_JSON).content(json(libroNuevo())))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.status").value(403));
    }

    @Test
    @DisplayName("USUARIO no accede a la gestión de usuarios ni a préstamos vencidos: 403")
    void usuarioSinGestion() throws Exception {
        String token = token(CARLOS, CLAVE_USUARIO);
        mvc.perform(get("/api/usuarios").header("Authorization", token)).andExpect(status().isForbidden());
        mvc.perform(get("/api/prestamos/vencidos").header("Authorization", token)).andExpect(status().isForbidden());
        mvc.perform(get("/api/prestamos").header("Authorization", token)).andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("ADMIN registra un libro: 201 con cabecera Location")
    void adminCreaLibro() throws Exception {
        mvc.perform(post("/api/libros").header("Authorization", token(ADMIN, CLAVE_ADMIN))
                        .contentType(MediaType.APPLICATION_JSON).content(json(libroNuevo())))
                .andExpect(status().isCreated())
                .andExpect(header().exists("Location"))
                .andExpect(jsonPath("$.isbn").value("9781491950388"))
                .andExpect(jsonPath("$.ejemplaresDisponibles").value(2));
    }

    @Test
    @DisplayName("Validaciones del libro: 400 con detalle por campo (ISBN con dígito verificador incorrecto)")
    void validacionLibro() throws Exception {
        Map<String, Object> invalido = Map.of("isbn", "978-0134685990", "titulo", "", "autor", "X",
                "ejemplaresTotales", 0);
        mvc.perform(post("/api/libros").header("Authorization", token(ADMIN, CLAVE_ADMIN))
                        .contentType(MediaType.APPLICATION_JSON).content(json(invalido)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.detalles.isbn").exists())
                .andExpect(jsonPath("$.detalles.titulo").exists())
                .andExpect(jsonPath("$.detalles.ejemplaresTotales").exists());
    }

    @Test
    @DisplayName("ISBN duplicado: 409")
    void isbnDuplicado() throws Exception {
        Map<String, Object> duplicado = Map.of("isbn", "9780132350884", "titulo", "Otro", "autor", "Otro",
                "ejemplaresTotales", 1);
        mvc.perform(post("/api/libros").header("Authorization", token(ADMIN, CLAVE_ADMIN))
                        .contentType(MediaType.APPLICATION_JSON).content(json(duplicado)))
                .andExpect(status().isConflict());
    }

    @Test
    @DisplayName("Recurso inexistente: 404 con formato de error uniforme")
    void noEncontrado() throws Exception {
        mvc.perform(get("/api/libros/9999").header("Authorization", token(ADMIN, CLAVE_ADMIN)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.mensaje").value("Libro con id 9999 no existe"))
                .andExpect(jsonPath("$.ruta").value("/api/libros/9999"));
    }
}
