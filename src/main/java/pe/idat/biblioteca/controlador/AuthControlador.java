package pe.idat.biblioteca.controlador;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirements;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import pe.idat.biblioteca.dto.AuthResponse;
import pe.idat.biblioteca.dto.LoginRequest;
import pe.idat.biblioteca.dto.MensajeResponse;
import pe.idat.biblioteca.dto.RefreshRequest;
import pe.idat.biblioteca.dto.RegistroRequest;
import pe.idat.biblioteca.servicio.AuthServicio;

@RestController
@RequestMapping("/api/auth")
@Tag(name = "1. Autenticación", description = "Login, registro y renovación de tokens (endpoints públicos)")
@SecurityRequirements // sin candado en Swagger: no requieren token
public class AuthControlador {

    private final AuthServicio authServicio;

    public AuthControlador(AuthServicio authServicio) {
        this.authServicio = authServicio;
    }

    @PostMapping("/login")
    @Operation(summary = "Iniciar sesión", description = "Devuelve un access token JWT y un refresh token")
    public AuthResponse login(@Valid @RequestBody LoginRequest req) {
        return authServicio.login(req);
    }

    @PostMapping("/registro")
    @Operation(summary = "Registrarse como USUARIO", description = "Crea la cuenta con rol USUARIO e inicia sesión")
    public ResponseEntity<AuthResponse> registro(@Valid @RequestBody RegistroRequest req) {
        return ResponseEntity.status(HttpStatus.CREATED).body(authServicio.registrar(req));
    }

    @PostMapping("/refresh")
    @Operation(summary = "Renovar el access token", description = "Rota el refresh token: el anterior queda revocado")
    public AuthResponse refrescar(@Valid @RequestBody RefreshRequest req) {
        return authServicio.refrescar(req);
    }

    @PostMapping("/logout")
    @Operation(summary = "Cerrar sesión", description = "Revoca el refresh token indicado")
    public MensajeResponse logout(@Valid @RequestBody RefreshRequest req) {
        authServicio.logout(req);
        return new MensajeResponse("Sesión cerrada correctamente");
    }
}
