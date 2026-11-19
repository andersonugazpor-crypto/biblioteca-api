package pe.idat.biblioteca.seguridad;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * Filtro de seguridad personalizado: lee "Authorization: Bearer &lt;token&gt;", valida el JWT
 * y, si es correcto, autentica la petición sin sesión (stateless).
 * Si el token falla, guarda el motivo en la petición para que {@link JwtPuntoEntrada}
 * responda 401 con un mensaje preciso (expirado, inválido, cuenta inactiva...).
 * No es un @Component a propósito: así Spring Boot no lo registra dos veces como filtro de servlet.
 */
public class JwtFiltroAutenticacion extends OncePerRequestFilter {

    public static final String ATRIBUTO_ERROR = "jwt.error";
    private static final String PREFIJO = "Bearer ";

    private final JwtServicio jwtServicio;
    private final UserDetailsService userDetailsService;

    public JwtFiltroAutenticacion(JwtServicio jwtServicio, UserDetailsService userDetailsService) {
        this.jwtServicio = jwtServicio;
        this.userDetailsService = userDetailsService;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {
        String cabecera = request.getHeader(HttpHeaders.AUTHORIZATION);
        if (cabecera == null || !cabecera.startsWith(PREFIJO)) {
            chain.doFilter(request, response);
            return;
        }
        String token = cabecera.substring(PREFIJO.length()).trim();
        try {
            Claims claims = jwtServicio.validar(token);
            if (!JwtServicio.TIPO_ACCESS.equals(claims.get(JwtServicio.CLAIM_TIPO, String.class))) {
                throw new JwtException("El token no es de acceso");
            }
            if (SecurityContextHolder.getContext().getAuthentication() == null) {
                UserDetails usuario = userDetailsService.loadUserByUsername(claims.getSubject());
                if (!usuario.isEnabled()) {
                    request.setAttribute(ATRIBUTO_ERROR, "La cuenta del usuario está inactiva");
                } else {
                    UsernamePasswordAuthenticationToken autenticacion =
                            new UsernamePasswordAuthenticationToken(usuario, null, usuario.getAuthorities());
                    autenticacion.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                    SecurityContextHolder.getContext().setAuthentication(autenticacion);
                }
            }
        } catch (ExpiredJwtException e) {
            request.setAttribute(ATRIBUTO_ERROR, "El token ha expirado. Use /api/auth/refresh para obtener uno nuevo");
        } catch (UsernameNotFoundException e) {
            request.setAttribute(ATRIBUTO_ERROR, "El usuario del token ya no existe");
        } catch (JwtException | IllegalArgumentException e) {
            request.setAttribute(ATRIBUTO_ERROR, "Token inválido: " + e.getMessage());
        }
        chain.doFilter(request, response);
    }
}
