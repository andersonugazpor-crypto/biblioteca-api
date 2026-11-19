package pe.idat.biblioteca.mapper;

import pe.idat.biblioteca.dto.CategoriaResponse;
import pe.idat.biblioteca.dto.CategoriaResumen;
import pe.idat.biblioteca.dto.LibroResponse;
import pe.idat.biblioteca.dto.PrestamoResponse;
import pe.idat.biblioteca.dto.UsuarioResponse;
import pe.idat.biblioteca.entidad.Categoria;
import pe.idat.biblioteca.entidad.Libro;
import pe.idat.biblioteca.entidad.Prestamo;
import pe.idat.biblioteca.entidad.Usuario;

import java.time.LocalDate;
import java.util.Comparator;
import java.util.TreeSet;

/**
 * Convierte entidades JPA en DTOs de respuesta. Así la API nunca expone
 * las entidades (ni datos sensibles como la contraseña) ni sus relaciones perezosas.
 */
public final class DtoMapper {

    private DtoMapper() {
    }

    public static UsuarioResponse usuario(Usuario u) {
        TreeSet<String> roles = new TreeSet<>();
        u.getRoles().forEach(r -> roles.add(r.getNombre().name()));
        return new UsuarioResponse(u.getId(), u.getNombres(), u.getApellidos(), u.getEmail(),
                u.getCodigoUniversitario(), u.isActivo(), roles, u.getFechaRegistro());
    }

    public static LibroResponse libro(Libro l) {
        return new LibroResponse(l.getId(), l.getIsbn(), l.getTitulo(), l.getAutor(), l.getEditorial(),
                l.getAnioPublicacion(), l.getEjemplaresTotales(), l.getEjemplaresDisponibles(),
                l.getEjemplaresDisponibles() > 0,
                l.getCategorias().stream()
                        .sorted(Comparator.comparing(Categoria::getNombre))
                        .map(c -> new CategoriaResumen(c.getId(), c.getNombre()))
                        .toList());
    }

    public static CategoriaResponse categoria(Categoria c, long totalLibros) {
        return new CategoriaResponse(c.getId(), c.getNombre(), c.getDescripcion(), totalLibros);
    }

    public static PrestamoResponse prestamo(Prestamo p, LocalDate hoy) {
        Usuario u = p.getUsuario();
        Libro l = p.getLibro();
        return new PrestamoResponse(p.getId(), u.getId(), u.getNombreCompleto(), u.getCodigoUniversitario(),
                l.getId(), l.getTitulo(), l.getIsbn(), p.getFechaPrestamo(), p.getFechaVencimiento(),
                p.getFechaDevolucion(), p.getEstado(), p.estaVencido(hoy), p.diasRetraso(hoy), p.getMulta(),
                p.getObservaciones());
    }
}
