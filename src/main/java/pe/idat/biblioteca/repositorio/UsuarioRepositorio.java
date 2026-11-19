package pe.idat.biblioteca.repositorio;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import pe.idat.biblioteca.entidad.Usuario;

import java.util.Optional;

public interface UsuarioRepositorio extends JpaRepository<Usuario, Long> {

    Optional<Usuario> findByEmailIgnoreCase(String email);

    boolean existsByEmailIgnoreCase(String email);

    boolean existsByEmailIgnoreCaseAndIdNot(String email, Long id);

    boolean existsByCodigoUniversitarioIgnoreCase(String codigo);

    /** Búsqueda por nombre, apellido, email o código universitario. */
    @Query("select u from Usuario u where :texto = '' "
            + "or lower(concat(u.nombres, ' ', u.apellidos)) like lower(concat('%', :texto, '%')) "
            + "or lower(u.email) like lower(concat('%', :texto, '%')) "
            + "or lower(u.codigoUniversitario) like lower(concat('%', :texto, '%'))")
    Page<Usuario> buscar(@Param("texto") String texto, Pageable pageable);
}
