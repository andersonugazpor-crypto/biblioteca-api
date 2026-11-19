package pe.idat.biblioteca.seguridad;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import java.io.IOException;

/** Respuesta 401 personalizada cuando falta el token o no es válido. */
@Component
public class JwtPuntoEntrada implements AuthenticationEntryPoint {

    private final ObjectMapper mapper;

    public JwtPuntoEntrada(ObjectMapper mapper) {
        this.mapper = mapper;
    }

    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response, AuthenticationException ex)
            throws IOException {
        Object motivo = request.getAttribute(JwtFiltroAutenticacion.ATRIBUTO_ERROR);
        String mensaje = motivo != null ? motivo.toString()
                : "Se requiere autenticación: envíe el header Authorization: Bearer <token>";
        RespuestaJson.escribir(mapper, request, response, HttpServletResponse.SC_UNAUTHORIZED, "No autorizado", mensaje);
    }
}
