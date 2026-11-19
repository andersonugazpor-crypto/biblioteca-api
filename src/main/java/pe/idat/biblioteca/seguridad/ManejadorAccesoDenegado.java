package pe.idat.biblioteca.seguridad;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

/** Respuesta 403 personalizada: el usuario está autenticado pero su rol no permite la operación. */
@Component
public class ManejadorAccesoDenegado implements AccessDeniedHandler {

    private final ObjectMapper mapper;

    public ManejadorAccesoDenegado(ObjectMapper mapper) {
        this.mapper = mapper;
    }

    @Override
    public void handle(HttpServletRequest request, HttpServletResponse response, AccessDeniedException ex)
            throws IOException {
        RespuestaJson.escribir(mapper, request, response, HttpServletResponse.SC_FORBIDDEN, "Acceso denegado",
                "Su rol no tiene permiso para " + request.getMethod() + " " + request.getRequestURI());
    }
}
