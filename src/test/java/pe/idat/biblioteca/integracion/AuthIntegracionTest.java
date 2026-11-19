package pe.idat.biblioteca.integracion;

import com.fasterxml.jackson.databind.JsonNode;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;

import java.util.Map;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.hasItem;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@DisplayName("Integración: autenticación JWT y refresh token")
class AuthIntegracionTest extends ApiIntegracionBase {

    @Test
    @DisplayName("Login correcto devuelve access token, refresh token y roles")
    void loginCorrecto() throws Exception {
        mvc.perform(post("/api/auth/login").contentType(MediaType.APPLICATION_JSON)
                        .content(json(Map.of("email", ADMIN, "password", CLAVE_ADMIN))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.tipoToken").value("Bearer"))
                .andExpect(jsonPath("$.accessToken").isNotEmpty())
                .andExpect(jsonPath("$.refreshToken").isNotEmpty())
                .andExpect(jsonPath("$.expiraEnSegundos").value(1800))
                .andExpect(jsonPath("$.usuario.roles", hasItem("ADMIN")))
                .andExpect(jsonPath("$.usuario.password").doesNotExist());
    }

    @Test
    @DisplayName("Credenciales incorrectas devuelven 401")
    void credencialesIncorrectas() throws Exception {
        mvc.perform(post("/api/auth/login").contentType(MediaType.APPLICATION_JSON)
                        .content(json(Map.of("email", ADMIN, "password", "otra"))))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.mensaje").value("Email o contraseña incorrectos"));
    }

    @Test
    @DisplayName("Login con datos inválidos devuelve 400 con detalle por campo")
    void loginInvalido() throws Exception {
        mvc.perform(post("/api/auth/login").contentType(MediaType.APPLICATION_JSON)
                        .content(json(Map.of("email", "no-es-email", "password", ""))))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.detalles.email").exists())
                .andExpect(jsonPath("$.detalles.password").exists());
    }

    @Test
    @DisplayName("Registro crea un USUARIO (201) y un email repetido devuelve 409")
    void registro() throws Exception {
        Map<String, String> datos = Map.of("nombres", "Lucía", "apellidos", "Ramírez Soto",
                "email", "lucia.ramirez@idat.edu.pe", "codigoUniversitario", "U20251234", "password", "Clave2025*");
        mvc.perform(post("/api/auth/registro").contentType(MediaType.APPLICATION_JSON).content(json(datos)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.usuario.roles[0]").value("USUARIO"))
                .andExpect(jsonPath("$.accessToken").isNotEmpty());
        mvc.perform(post("/api/auth/registro").contentType(MediaType.APPLICATION_JSON).content(json(datos)))
                .andExpect(status().isConflict());
    }

    @Test
    @DisplayName("Refresh rota el token: el nuevo funciona y el anterior queda revocado")
    void refreshRotacion() throws Exception {
        String refreshOriginal = login(CARLOS, CLAVE_USUARIO).get("refreshToken").asText();

        String cuerpo = mvc.perform(post("/api/auth/refresh").contentType(MediaType.APPLICATION_JSON)
                        .content(json(Map.of("refreshToken", refreshOriginal))))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        JsonNode nuevo = leer(cuerpo);
        assertNotEquals(refreshOriginal, nuevo.get("refreshToken").asText());

        mvc.perform(get("/api/usuarios/me").header("Authorization", "Bearer " + nuevo.get("accessToken").asText()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value(CARLOS));

        mvc.perform(post("/api/auth/refresh").contentType(MediaType.APPLICATION_JSON)
                        .content(json(Map.of("refreshToken", refreshOriginal))))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("Logout revoca el refresh token")
    void logout() throws Exception {
        String refresh = login(CARLOS, CLAVE_USUARIO).get("refreshToken").asText();
        mvc.perform(post("/api/auth/logout").contentType(MediaType.APPLICATION_JSON)
                        .content(json(Map.of("refreshToken", refresh))))
                .andExpect(status().isOk());
        mvc.perform(post("/api/auth/refresh").contentType(MediaType.APPLICATION_JSON)
                        .content(json(Map.of("refreshToken", refresh))))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.mensaje", containsString("revocado")));
    }
}
