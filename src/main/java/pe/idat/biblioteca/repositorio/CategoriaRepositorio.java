package pe.idat.biblioteca.repositorio;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import pe.idat.biblioteca.entidad.Categoria;

import java.util.List;

public interface CategoriaRepositorio extends JpaRepository<Categoria, Long> {

    boolean existsByNombreIgnoreCase(String nombre);

    boolean existsByNombreIgnoreCaseAndIdNot(String nombre, Long id);

    List<Categoria> findAllByOrderByNombreAsc();

    @Query("select count(l) from Libro l join l.categorias c where c.id = :categoriaId and l.activo = true")
    long contarLibrosActivos(@Param("categoriaId") Long categoriaId);
}
