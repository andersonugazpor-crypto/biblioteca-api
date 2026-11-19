package pe.idat.biblioteca.seguridad;

import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pe.idat.biblioteca.entidad.Usuario;
import pe.idat.biblioteca.repositorio.UsuarioRepositorio;

/** Carga el usuario por email para Spring Security; los roles se exponen como ROLE_ADMIN / ROLE_USUARIO. */
@Service
public class UsuarioDetallesServicio implements UserDetailsService {

    private final UsuarioRepositorio usuarios;

    public UsuarioDetallesServicio(UsuarioRepositorio usuarios) {
        this.usuarios = usuarios;
    }

    @Override
    @Transactional(readOnly = true)
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        Usuario usuario = usuarios.findByEmailIgnoreCase(email)
                .orElseThrow(() -> new UsernameNotFoundException("Usuario no encontrado: " + email));
        return User.withUsername(usuario.getEmail())
                .password(usuario.getPassword())
                .disabled(!usuario.isActivo())
                .authorities(usuario.getRoles().stream()
                        .map(r -> new SimpleGrantedAuthority("ROLE_" + r.getNombre().name()))
                        .toList())
                .build();
    }
}
