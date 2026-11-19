package pe.idat.biblioteca.controlador;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import pe.idat.biblioteca.dto.CategoriaRequest;
import pe.idat.biblioteca.dto.CategoriaResponse;
import pe.idat.biblioteca.servicio.CategoriaServicio;

import java.util.List;

@RestController
@RequestMapping("/api/categorias")
@Tag(name = "3. Categorías", description = "Categorías temáticas de los libros")
public class CategoriaControlador {

    private final CategoriaServicio categoriaServicio;

    public CategoriaControlador(CategoriaServicio categoriaServicio) {
        this.categoriaServicio = categoriaServicio;
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN','USUARIO')")
    @Operation(summary = "Listar categorías con su cantidad de libros")
    public List<CategoriaResponse> listar() {
        return categoriaServicio.listar();
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','USUARIO')")
    @Operation(summary = "Obtener una categoría")
    public CategoriaResponse obtener(@PathVariable Long id) {
        return categoriaServicio.obtener(id);
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Crear categoría (ADMIN)")
    public ResponseEntity<CategoriaResponse> crear(@Valid @RequestBody CategoriaRequest req) {
        return ResponseEntity.status(HttpStatus.CREATED).body(categoriaServicio.crear(req));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Actualizar categoría (ADMIN)")
    public CategoriaResponse actualizar(@PathVariable Long id, @Valid @RequestBody CategoriaRequest req) {
        return categoriaServicio.actualizar(id, req);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Eliminar categoría sin libros (ADMIN)")
    public ResponseEntity<Void> eliminar(@PathVariable Long id) {
        categoriaServicio.eliminar(id);
        return ResponseEntity.noContent().build();
    }
}
