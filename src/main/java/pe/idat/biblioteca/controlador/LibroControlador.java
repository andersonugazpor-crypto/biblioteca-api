package pe.idat.biblioteca.controlador;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;
import pe.idat.biblioteca.dto.EjemplaresRequest;
import pe.idat.biblioteca.dto.LibroPopularResponse;
import pe.idat.biblioteca.dto.LibroRequest;
import pe.idat.biblioteca.dto.LibroResponse;
import pe.idat.biblioteca.dto.PaginaResponse;
import pe.idat.biblioteca.servicio.LibroServicio;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/api/libros")
@Tag(name = "2. Libros", description = "Catálogo: consulta para todos los usuarios autenticados, gestión solo ADMIN")
public class LibroControlador {

    private final LibroServicio libroServicio;

    public LibroControlador(LibroServicio libroServicio) {
        this.libroServicio = libroServicio;
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN','USUARIO')")
    @Operation(summary = "Buscar en el catálogo",
            description = "Filtros opcionales: texto (título, autor o ISBN), categoriaId, disponibles. Paginado: page, size, sort")
    public PaginaResponse<LibroResponse> buscar(@RequestParam(required = false) String texto,
                                                @RequestParam(required = false) Long categoriaId,
                                                @RequestParam(defaultValue = "false") boolean disponibles,
                                                @PageableDefault(size = 10, sort = "titulo") Pageable pageable) {
        return libroServicio.buscar(texto, categoriaId, disponibles, pageable);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','USUARIO')")
    @Operation(summary = "Obtener un libro por id")
    public LibroResponse obtener(@PathVariable Long id) {
        return libroServicio.obtener(id);
    }

    @GetMapping("/populares")
    @PreAuthorize("hasAnyRole('ADMIN','USUARIO')")
    @Operation(summary = "Libros más prestados", description = "Ranking calculado con una consulta de agregación")
    public List<LibroPopularResponse> populares(@RequestParam(defaultValue = "5") int limite) {
        return libroServicio.populares(limite);
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Registrar libro (ADMIN)")
    public ResponseEntity<LibroResponse> crear(@Valid @RequestBody LibroRequest req) {
        LibroResponse creado = libroServicio.crear(req);
        URI ubicacion = ServletUriComponentsBuilder.fromCurrentRequest().path("/{id}").buildAndExpand(creado.id()).toUri();
        return ResponseEntity.created(ubicacion).body(creado);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Actualizar libro (ADMIN)")
    public LibroResponse actualizar(@PathVariable Long id, @Valid @RequestBody LibroRequest req) {
        return libroServicio.actualizar(id, req);
    }

    @PatchMapping("/{id}/ejemplares")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Cambiar el total de ejemplares (ADMIN)", description = "No puede quedar por debajo de los prestados")
    public LibroResponse ejemplares(@PathVariable Long id, @Valid @RequestBody EjemplaresRequest req) {
        return libroServicio.actualizarEjemplares(id, req);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Dar de baja un libro (ADMIN)", description = "Baja lógica; falla si tiene ejemplares prestados")
    public ResponseEntity<Void> eliminar(@PathVariable Long id) {
        libroServicio.eliminar(id);
        return ResponseEntity.noContent().build();
    }
}
