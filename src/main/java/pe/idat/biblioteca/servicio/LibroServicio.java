package pe.idat.biblioteca.servicio;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pe.idat.biblioteca.dto.EjemplaresRequest;
import pe.idat.biblioteca.dto.LibroPopularResponse;
import pe.idat.biblioteca.dto.LibroRequest;
import pe.idat.biblioteca.dto.LibroResponse;
import pe.idat.biblioteca.dto.PaginaResponse;
import pe.idat.biblioteca.entidad.Categoria;
import pe.idat.biblioteca.entidad.Libro;
import pe.idat.biblioteca.excepcion.RecursoDuplicadoException;
import pe.idat.biblioteca.excepcion.RecursoNoEncontradoException;
import pe.idat.biblioteca.excepcion.ReglaNegocioException;
import pe.idat.biblioteca.mapper.DtoMapper;
import pe.idat.biblioteca.repositorio.CategoriaRepositorio;
import pe.idat.biblioteca.repositorio.LibroRepositorio;
import pe.idat.biblioteca.validacion.IsbnValidator;

import java.time.Clock;
import java.time.Year;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
@Transactional
public class LibroServicio {

    private final LibroRepositorio libros;
    private final CategoriaRepositorio categorias;
    private final Clock reloj;

    public LibroServicio(LibroRepositorio libros, CategoriaRepositorio categorias, Clock reloj) {
        this.libros = libros;
        this.categorias = categorias;
        this.reloj = reloj;
    }

    @Transactional(readOnly = true)
    public PaginaResponse<LibroResponse> buscar(String texto, Long categoriaId, boolean soloDisponibles, Pageable pageable) {
        String filtro = texto == null ? "" : texto.trim();
        return PaginaResponse.de(libros.buscarCatalogo(filtro, categoriaId, soloDisponibles, pageable), DtoMapper::libro);
    }

    @Transactional(readOnly = true)
    public LibroResponse obtener(Long id) {
        return DtoMapper.libro(buscarActivo(id));
    }

    @Transactional(readOnly = true)
    public List<LibroPopularResponse> populares(int limite) {
        int tope = Math.min(Math.max(limite, 1), 20);
        return libros.masPrestados(PageRequest.of(0, tope)).stream()
                .map(p -> new LibroPopularResponse(p.getId(), p.getTitulo(), p.getAutor(), p.getTotalPrestamos()))
                .toList();
    }

    public LibroResponse crear(LibroRequest req) {
        String isbn = IsbnValidator.normalizar(req.isbn());
        if (libros.existsByIsbn(isbn)) {
            throw new RecursoDuplicadoException("Ya existe un libro con ISBN " + isbn);
        }
        validarAnio(req.anioPublicacion());
        Libro libro = new Libro(isbn, req.titulo().trim(), req.autor().trim(), req.editorial(),
                req.anioPublicacion(), req.ejemplaresTotales());
        libro.asignarCategorias(resolverCategorias(req.categoriaIds()));
        return DtoMapper.libro(libros.save(libro));
    }

    public LibroResponse actualizar(Long id, LibroRequest req) {
        Libro libro = buscarActivo(id);
        String isbn = IsbnValidator.normalizar(req.isbn());
        if (libros.existsByIsbnAndIdNot(isbn, id)) {
            throw new RecursoDuplicadoException("Ya existe otro libro con ISBN " + isbn);
        }
        validarAnio(req.anioPublicacion());
        libro.actualizarDatos(isbn, req.titulo().trim(), req.autor().trim(), req.editorial(), req.anioPublicacion());
        libro.actualizarEjemplaresTotales(req.ejemplaresTotales());
        libro.asignarCategorias(resolverCategorias(req.categoriaIds()));
        return DtoMapper.libro(libro);
    }

    public LibroResponse actualizarEjemplares(Long id, EjemplaresRequest req) {
        Libro libro = buscarActivo(id);
        libro.actualizarEjemplaresTotales(req.ejemplaresTotales());
        return DtoMapper.libro(libro);
    }

    /** Baja lógica: conserva el historial de préstamos del libro. */
    public void eliminar(Long id) {
        buscarActivo(id).darDeBaja();
    }

    public Libro buscarActivo(Long id) {
        Libro libro = libros.findConCategoriasById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("Libro", id));
        if (!libro.isActivo()) {
            throw new RecursoNoEncontradoException("Libro", id);
        }
        return libro;
    }

    private void validarAnio(Integer anio) {
        if (anio != null && anio > Year.now(reloj).getValue()) {
            throw new ReglaNegocioException("El año de publicación no puede ser posterior al año actual");
        }
    }

    private Set<Categoria> resolverCategorias(Set<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            return new HashSet<>();
        }
        List<Categoria> encontradas = categorias.findAllById(ids);
        if (encontradas.size() != ids.size()) {
            Set<Long> faltantes = new HashSet<>(ids);
            encontradas.forEach(c -> faltantes.remove(c.getId()));
            throw new RecursoNoEncontradoException("Categorías inexistentes: " + faltantes);
        }
        return new HashSet<>(encontradas);
    }
}
