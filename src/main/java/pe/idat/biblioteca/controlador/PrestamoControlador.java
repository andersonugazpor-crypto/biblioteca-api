package pe.idat.biblioteca.controlador;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;
import pe.idat.biblioteca.dto.DevolucionRequest;
import pe.idat.biblioteca.dto.PaginaResponse;
import pe.idat.biblioteca.dto.PrestamoRequest;
import pe.idat.biblioteca.dto.PrestamoResponse;
import pe.idat.biblioteca.entidad.EstadoPrestamo;
import pe.idat.biblioteca.servicio.PrestamoServicio;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/api/prestamos")
@Tag(name = "5. Préstamos", description = "Préstamos y devoluciones (ADMIN) e historial propio (USUARIO)")
public class PrestamoControlador {

    private final PrestamoServicio prestamoServicio;

    public PrestamoControlador(PrestamoServicio prestamoServicio) {
        this.prestamoServicio = prestamoServicio;
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Registrar préstamo (ADMIN)",
            description = "Valida usuario activo, sin vencidos, máximo de préstamos y ejemplares disponibles")
    public ResponseEntity<PrestamoResponse> registrar(@Valid @RequestBody PrestamoRequest req) {
        PrestamoResponse creado = prestamoServicio.registrar(req);
        URI ubicacion = ServletUriComponentsBuilder.fromCurrentRequest().path("/{id}").buildAndExpand(creado.id()).toUri();
        return ResponseEntity.created(ubicacion).body(creado);
    }

    @PostMapping("/{id}/devolucion")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Registrar devolución (ADMIN)", description = "Calcula la multa por días de retraso")
    public PrestamoResponse devolver(@PathVariable Long id, @Valid @RequestBody(required = false) DevolucionRequest req) {
        return prestamoServicio.devolver(id, req);
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Listar préstamos (ADMIN)", description = "Filtros opcionales: estado (ACTIVO, DEVUELTO) y usuarioId")
    public PaginaResponse<PrestamoResponse> listar(@RequestParam(required = false) EstadoPrestamo estado,
                                                   @RequestParam(required = false) Long usuarioId,
                                                   @PageableDefault(size = 10, sort = "fechaPrestamo",
                                                           direction = Sort.Direction.DESC) Pageable pageable) {
        return prestamoServicio.listar(estado, usuarioId, pageable);
    }

    @GetMapping("/vencidos")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Préstamos vencidos sin devolver (ADMIN)")
    public List<PrestamoResponse> vencidos() {
        return prestamoServicio.vencidos();
    }

    @GetMapping("/mis-prestamos")
    @PreAuthorize("hasAnyRole('ADMIN','USUARIO')")
    @Operation(summary = "Mi historial de préstamos", description = "Filtro opcional: estado")
    public PaginaResponse<PrestamoResponse> misPrestamos(Authentication auth,
                                                         @RequestParam(required = false) EstadoPrestamo estado,
                                                         @PageableDefault(size = 10) Pageable pageable) {
        return prestamoServicio.historialDe(auth.getName(), estado, pageable);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','USUARIO')")
    @Operation(summary = "Ver un préstamo", description = "ADMIN ve cualquiera; USUARIO solo los propios (si no, 403)")
    public PrestamoResponse obtener(@PathVariable Long id, Authentication auth) {
        boolean esAdmin = auth.getAuthorities().stream().anyMatch(a -> "ROLE_ADMIN".equals(a.getAuthority()));
        return prestamoServicio.obtener(id, auth.getName(), esAdmin);
    }

    @GetMapping("/usuario/{usuarioId}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Historial de un usuario (ADMIN)")
    public PaginaResponse<PrestamoResponse> deUsuario(@PathVariable Long usuarioId,
                                                      @PageableDefault(size = 10, sort = "fechaPrestamo",
                                                              direction = Sort.Direction.DESC) Pageable pageable) {
        return prestamoServicio.listar(null, usuarioId, pageable);
    }
}
