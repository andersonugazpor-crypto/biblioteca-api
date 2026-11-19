package pe.idat.biblioteca.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import pe.idat.biblioteca.seguridad.JwtFiltroAutenticacion;
import pe.idat.biblioteca.seguridad.JwtPuntoEntrada;
import pe.idat.biblioteca.seguridad.JwtServicio;
import pe.idat.biblioteca.seguridad.ManejadorAccesoDenegado;
import pe.idat.biblioteca.seguridad.UsuarioDetallesServicio;

import java.util.List;

/**
 * Seguridad sin estado con JWT. Control de acceso en dos capas:
 * 1) reglas por ruta y método HTTP aquí; 2) {@code @PreAuthorize} en cada endpoint.
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SeguridadConfig {

    private static final String ADMIN = "ADMIN";
    private static final String USUARIO = "USUARIO";

    @Bean
    public SecurityFilterChain cadenaSeguridad(HttpSecurity http, JwtServicio jwtServicio,
                                               UsuarioDetallesServicio usuarioDetalles,
                                               JwtPuntoEntrada puntoEntrada,
                                               ManejadorAccesoDenegado accesoDenegado) throws Exception {
        http
                .csrf(csrf -> csrf.disable())
                .cors(Customizer.withDefaults())
                .sessionManagement(s -> s.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .exceptionHandling(e -> e.authenticationEntryPoint(puntoEntrada).accessDeniedHandler(accesoDenegado))
                .authorizeHttpRequests(auth -> auth
                        // Públicos
                        .requestMatchers(HttpMethod.POST, "/api/auth/login", "/api/auth/registro",
                                "/api/auth/refresh", "/api/auth/logout").permitAll()
                        .requestMatchers("/v3/api-docs/**", "/swagger-ui/**", "/swagger-ui.html", "/error").permitAll()
                        // Catálogo: lectura para todos los autenticados, escritura solo ADMIN
                        .requestMatchers(HttpMethod.GET, "/api/libros/**", "/api/categorias/**").hasAnyRole(ADMIN, USUARIO)
                        .requestMatchers("/api/libros/**", "/api/categorias/**").hasRole(ADMIN)
                        // Usuarios: perfil propio para todos, gestión solo ADMIN
                        .requestMatchers("/api/usuarios/me", "/api/usuarios/me/**").hasAnyRole(ADMIN, USUARIO)
                        .requestMatchers("/api/usuarios/**").hasRole(ADMIN)
                        // Préstamos: historial propio para todos, gestión solo ADMIN
                        .requestMatchers(HttpMethod.GET, "/api/prestamos/vencidos").hasRole(ADMIN)
                        .requestMatchers(HttpMethod.GET, "/api/prestamos/mis-prestamos").hasAnyRole(ADMIN, USUARIO)
                        .requestMatchers(HttpMethod.GET, "/api/prestamos/{id}").hasAnyRole(ADMIN, USUARIO)
                        .requestMatchers("/api/prestamos/**").hasRole(ADMIN)
                        .anyRequest().authenticated())
                .addFilterBefore(new JwtFiltroAutenticacion(jwtServicio, usuarioDetalles),
                        UsernamePasswordAuthenticationFilter.class);
        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration configuracion) throws Exception {
        return configuracion.getAuthenticationManager();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration cors = new CorsConfiguration();
        cors.setAllowedOriginPatterns(List.of("http://localhost:*"));
        cors.setAllowedMethods(List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
        cors.setAllowedHeaders(List.of("Authorization", "Content-Type"));
        UrlBasedCorsConfigurationSource fuente = new UrlBasedCorsConfigurationSource();
        fuente.registerCorsConfiguration("/api/**", cors);
        return fuente;
    }
}
