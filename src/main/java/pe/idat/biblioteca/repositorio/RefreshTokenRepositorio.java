package pe.idat.biblioteca.repositorio;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import pe.idat.biblioteca.entidad.RefreshToken;

import java.util.Optional;

public interface RefreshTokenRepositorio extends JpaRepository<RefreshToken, Long> {

    Optional<RefreshToken> findByToken(String token);

    @Modifying
    @Query("update RefreshToken r set r.revocado = true where r.usuario.id = :usuarioId and r.revocado = false")
    int revocarTodosDelUsuario(@Param("usuarioId") Long usuarioId);
}
