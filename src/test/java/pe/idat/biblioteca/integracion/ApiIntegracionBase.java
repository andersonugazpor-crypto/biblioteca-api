package pe.idat.biblioteca.integracion;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Base de las pruebas de integración: levanta la aplicación completa con H2 (perfil "test")
 * y los datos de ejemplo. Cada prueba corre en una transacción que se revierte al final.
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
abstract class ApiIntegracionBase {

    static final String ADMIN = "admin@biblioteca.edu.pe";
    static final String CLAVE_ADMIN = "Admin123*";
    static final String CARLOS = "carlos.quispe@idat.edu.pe";
    static final String MARIA = "maria.flores@idat.edu.pe";
    static final String JORGE = "jorge.ramos@idat.edu.pe";
    static final String CLAVE_USUARIO = "Usuario123*";

    @Autowired MockMvc mvc;
    @Autowired ObjectMapper mapper;

    JsonNode login(String email, String clave) throws Exception {
        String cuerpo = mvc.perform(post("/api/auth/login").contentType(MediaType.APPLICATION_JSON)
                        .content(json(Map.of("email", email, "password", clave))))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        return mapper.readTree(cuerpo);
    }

    String token(String email, String clave) throws Exception {
        return "Bearer " + login(email, clave).get("accessToken").asText();
    }

    String json(Object objeto) throws Exception {
        return mapper.writeValueAsString(objeto);
    }

    JsonNode leer(String cuerpo) throws Exception {
        return mapper.readTree(cuerpo);
    }
}
