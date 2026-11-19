package pe.idat.biblioteca.repositorio;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import pe.idat.biblioteca.entidad.EstadoPrestamo;
import pe.idat.biblioteca.entidad.Prestamo;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface PrestamoRepositorio extends JpaRepository<Prestamo, Long> {

    long countByUsuarioIdAndEstado(Long usuarioId, EstadoPrestamo estado);

    boolean existsByUsuarioIdAndLibroIdAndEstado(Long usuarioId, Long libroId, EstadoPrestamo estado);

    boolean existsByUsuarioIdAndEstadoAndFechaVencimientoBefore(Long usuarioId, EstadoPrestamo estado, LocalDate fecha);

    @EntityGraph(attributePaths = {"usuario", "libro"})
    Optional<Prestamo> findDetalleById(Long id);

    /** Historial de un usuario, del más reciente al más antiguo. */
    @EntityGraph(attributePaths = {"usuario", "libro"})
    Page<Prestamo> findByUsuarioIdOrderByFechaPrestamoDescIdDesc(Long usuarioId, Pageable pageable);

    @EntityGraph(attributePaths = {"usuario", "libro"})
    Page<Prestamo> findByUsuarioIdAndEstadoOrderByFechaPrestamoDescIdDesc(Long usuarioId, EstadoPrestamo estado, Pageable pageable);

    /** Listado administrativo con filtros opcionales de estado y usuario. */
    @EntityGraph(attributePaths = {"usuario", "libro"})
    @Query(value = "select p from Prestamo p "
            + "where (:estado is null or p.estado = :estado) and (:usuarioId is null or p.usuario.id = :usuarioId)",
            countQuery = "select count(p) from Prestamo p "
                    + "where (:estado is null or p.estado = :estado) and (:usuarioId is null or p.usuario.id = :usuarioId)")
    Page<Prestamo> buscar(@Param("estado") EstadoPrestamo estado, @Param("usuarioId") Long usuarioId, Pageable pageable);

    /** Préstamos activos cuya fecha de vencimiento ya pasó. */
    @Query("select p from Prestamo p join fetch p.usuario join fetch p.libro "
            + "where p.estado = pe.idat.biblioteca.entidad.EstadoPrestamo.ACTIVO and p.fechaVencimiento < :hoy "
            + "order by p.fechaVencimiento asc")
    List<Prestamo> vencidos(@Param("hoy") LocalDate hoy);
}
