package pe.idat.biblioteca.repositorio;

import org.springframework.data.jpa.repository.JpaRepository;
import pe.idat.biblioteca.entidad.NombreRol;
import pe.idat.biblioteca.entidad.Rol;

import java.util.Collection;
import java.util.Optional;
import java.util.Set;

public interface RolRepositorio extends JpaRepository<Rol, Long> {

    Optional<Rol> findByNombre(NombreRol nombre);

    Set<Rol> findByNombreIn(Collection<NombreRol> nombres);
}
