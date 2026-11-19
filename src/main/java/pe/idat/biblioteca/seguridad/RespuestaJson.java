package pe.idat.biblioteca.seguridad;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.MediaType;
import pe.idat.biblioteca.dto.ErrorResponse;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

/** Escribe errores de seguridad (401/403) con el mismo formato JSON que el resto de la API. */
final class RespuestaJson {

    private RespuestaJson() {
    }

    static void escribir(ObjectMapper mapper, HttpServletRequest request, HttpServletResponse response,
                         int status, String error, String mensaje) throws IOException {
        response.setStatus(status);
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding(StandardCharsets.UTF_8.name());
        mapper.writeValue(response.getOutputStream(), ErrorResponse.de(status, error, mensaje, request.getRequestURI()));
    }
}
