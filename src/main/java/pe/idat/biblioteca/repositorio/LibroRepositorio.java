package pe.idat.biblioteca.repositorio;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import pe.idat.biblioteca.entidad.Libro;

import java.util.List;
import java.util.Optional;

public interface LibroRepositorio extends JpaRepository<Libro, Long> {

    boolean existsByIsbn(String isbn);

    boolean existsByIsbnAndIdNot(String isbn, Long id);

    /** Carga el libro junto con sus categorías en una sola consulta (evita N+1). */
    @EntityGraph(attributePaths = "categorias")
    Optional<Libro> findConCategoriasById(Long id);

    /** Catálogo con filtros opcionales: texto (título, autor o ISBN), categoría y solo disponibles. */
    @Query(value = "select distinct l from Libro l left join l.categorias c "
            + "where l.activo = true "
            + "and (:texto = '' or lower(l.titulo) like lower(concat('%', :texto, '%')) "
            + "     or lower(l.autor) like lower(concat('%', :texto, '%')) or l.isbn like concat('%', :texto, '%')) "
            + "and (:categoriaId is null or c.id = :categoriaId) "
            + "and (:soloDisponibles = false or l.ejemplaresDisponibles > 0)",
            countQuery = "select count(distinct l) from Libro l left join l.categorias c "
                    + "where l.activo = true "
                    + "and (:texto = '' or lower(l.titulo) like lower(concat('%', :texto, '%')) "
                    + "     or lower(l.autor) like lower(concat('%', :texto, '%')) or l.isbn like concat('%', :texto, '%')) "
                    + "and (:categoriaId is null or c.id = :categoriaId) "
                    + "and (:soloDisponibles = false or l.ejemplaresDisponibles > 0)")
    Page<Libro> buscarCatalogo(@Param("texto") String texto,
                               @Param("categoriaId") Long categoriaId,
                               @Param("soloDisponibles") boolean soloDisponibles,
                               Pageable pageable);

    /** Ranking de libros más prestados (proyección con agregación). */
    @Query("select l.id as id, l.titulo as titulo, l.autor as autor, count(p) as totalPrestamos "
            + "from Prestamo p join p.libro l group by l.id, l.titulo, l.autor order by count(p) desc")
    List<LibroPopular> masPrestados(Pageable pageable);

    interface LibroPopular {
        Long getId();
        String getTitulo();
        String getAutor();
        Long getTotalPrestamos();
    }
}
