package pe.idat.biblioteca.controlador;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import pe.idat.biblioteca.dto.CambioPasswordRequest;
import pe.idat.biblioteca.dto.EstadoUsuarioRequest;
import pe.idat.biblioteca.dto.MensajeResponse;
import pe.idat.biblioteca.dto.PaginaResponse;
import pe.idat.biblioteca.dto.PerfilRequest;
import pe.idat.biblioteca.dto.UsuarioActualizarRequest;
import pe.idat.biblioteca.dto.UsuarioCrearRequest;
import pe.idat.biblioteca.dto.UsuarioResponse;
import pe.idat.biblioteca.servicio.UsuarioServicio;

@RestController
@RequestMapping("/api/usuarios")
@Tag(name = "4. Usuarios", description = "Gestión de cuentas (ADMIN) y perfil propio (todos)")
public class UsuarioControlador {

    private final UsuarioServicio usuarioServicio;

    public UsuarioControlador(UsuarioServicio usuarioServicio) {
        this.usuarioServicio = usuarioServicio;
    }

    // --------------------------------------------------------- perfil propio
    @GetMapping("/me")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Ver mi perfil")
    public UsuarioResponse miPerfil(Authentication auth) {
        return usuarioServicio.perfil(auth.getName());
    }

    @PutMapping("/me")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Actualizar mis nombres y apellidos")
    public UsuarioResponse actualizarMiPerfil(Authentication auth, @Valid @RequestBody PerfilRequest req) {
        return usuarioServicio.actualizarPerfil(auth.getName(), req);
    }

    @PutMapping("/me/password")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Cambiar mi contraseña", description = "Revoca todos los refresh tokens del usuario")
    public MensajeResponse cambiarPassword(Authentication auth, @Valid @RequestBody CambioPasswordRequest req) {
        usuarioServicio.cambiarPassword(auth.getName(), req);
        return new MensajeResponse("Contraseña actualizada. Vuelva a iniciar sesión en sus otros dispositivos");
    }

    // ----------------------------------------------------------------- ADMIN
    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Listar/buscar usuarios (ADMIN)")
    public PaginaResponse<UsuarioResponse> listar(@RequestParam(required = false) String texto,
                                                  @PageableDefault(size = 10, sort = "apellidos") Pageable pageable) {
        return usuarioServicio.listar(texto, pageable);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Obtener usuario (ADMIN)")
    public UsuarioResponse obtener(@PathVariable Long id) {
        return usuarioServicio.obtener(id);
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Crear usuario con roles (ADMIN)")
    public ResponseEntity<UsuarioResponse> crear(@Valid @RequestBody UsuarioCrearRequest req) {
        return ResponseEntity.status(HttpStatus.CREATED).body(usuarioServicio.crear(req));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Actualizar usuario y roles (ADMIN)")
    public UsuarioResponse actualizar(@PathVariable Long id, @Valid @RequestBody UsuarioActualizarRequest req,
                                      Authentication auth) {
        return usuarioServicio.actualizar(id, req, auth.getName());
    }

    @PatchMapping("/{id}/estado")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Activar o desactivar usuario (ADMIN)", description = "Desactivar revoca sus sesiones")
    public UsuarioResponse cambiarEstado(@PathVariable Long id, @Valid @RequestBody EstadoUsuarioRequest req,
                                         Authentication auth) {
        return usuarioServicio.cambiarEstado(id, req.activo(), auth.getName());
    }
}
