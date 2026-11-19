package pe.idat.biblioteca.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Clock;
import java.time.ZoneId;

@Configuration
@EnableConfigurationProperties({PropiedadesJwt.class, PropiedadesBiblioteca.class})
public class AplicacionConfig {

    @Bean
    public Clock reloj() {
        return Clock.system(ZoneId.of("America/Lima"));
    }

    /** Swagger UI con botón "Authorize" para pegar el token JWT. */
    @Bean
    public OpenAPI documentacionApi() {
        String esquema = "bearerAuth";
        return new OpenAPI()
                .info(new Info()
                        .title("Biblioteca Universitaria API")
                        .version("1.0.0")
                        .description("Gestión de libros, usuarios, préstamos y devoluciones con autenticación JWT "
                                + "y roles ADMIN / USUARIO. Inicie sesión en /api/auth/login y use el accessToken en Authorize.")
                        .contact(new Contact().name("IDAT - Desarrollo de los Componentes del Negocio")))
                .addSecurityItem(new SecurityRequirement().addList(esquema))
                .components(new Components().addSecuritySchemes(esquema, new SecurityScheme()
                        .name(esquema)
                        .type(SecurityScheme.Type.HTTP)
                        .scheme("bearer")
                        .bearerFormat("JWT")));
    }
}
