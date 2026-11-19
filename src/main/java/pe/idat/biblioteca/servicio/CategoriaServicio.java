package pe.idat.biblioteca.servicio;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pe.idat.biblioteca.dto.CategoriaRequest;
import pe.idat.biblioteca.dto.CategoriaResponse;
import pe.idat.biblioteca.entidad.Categoria;
import pe.idat.biblioteca.excepcion.RecursoDuplicadoException;
import pe.idat.biblioteca.excepcion.RecursoNoEncontradoException;
import pe.idat.biblioteca.excepcion.ReglaNegocioException;
import pe.idat.biblioteca.mapper.DtoMapper;
import pe.idat.biblioteca.repositorio.CategoriaRepositorio;

import java.util.List;

@Service
@Transactional
public class CategoriaServicio {

    private final CategoriaRepositorio categorias;

    public CategoriaServicio(CategoriaRepositorio categorias) {
        this.categorias = categorias;
    }

    @Transactional(readOnly = true)
    public List<CategoriaResponse> listar() {
        return categorias.findAllByOrderByNombreAsc().stream()
                .map(c -> DtoMapper.categoria(c, categorias.contarLibrosActivos(c.getId())))
                .toList();
    }

    @Transactional(readOnly = true)
    public CategoriaResponse obtener(Long id) {
        Categoria c = buscar(id);
        return DtoMapper.categoria(c, categorias.contarLibrosActivos(id));
    }

    public CategoriaResponse crear(CategoriaRequest req) {
        String nombre = req.nombre().trim();
        if (categorias.existsByNombreIgnoreCase(nombre)) {
            throw new RecursoDuplicadoException("Ya existe la categoría \"" + nombre + "\"");
        }
        Categoria c = categorias.save(new Categoria(nombre, req.descripcion()));
        return DtoMapper.categoria(c, 0);
    }

    public CategoriaResponse actualizar(Long id, CategoriaRequest req) {
        Categoria c = buscar(id);
        String nombre = req.nombre().trim();
        if (categorias.existsByNombreIgnoreCaseAndIdNot(nombre, id)) {
            throw new RecursoDuplicadoException("Ya existe la categoría \"" + nombre + "\"");
        }
        c.actualizar(nombre, req.descripcion());
        return DtoMapper.categoria(c, categorias.contarLibrosActivos(id));
    }

    public void eliminar(Long id) {
        Categoria c = buscar(id);
        if (!c.getLibros().isEmpty()) {
            throw new ReglaNegocioException("No se puede eliminar la categoría \"" + c.getNombre()
                    + "\": tiene " + c.getLibros().size() + " libro(s) asociados");
        }
        categorias.delete(c);
    }

    public Categoria buscar(Long id) {
        return categorias.findById(id).orElseThrow(() -> new RecursoNoEncontradoException("Categoría", id));
    }
}
