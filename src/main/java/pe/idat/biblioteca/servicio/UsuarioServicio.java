package pe.idat.biblioteca.servicio;

import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pe.idat.biblioteca.dto.CambioPasswordRequest;
import pe.idat.biblioteca.dto.PaginaResponse;
import pe.idat.biblioteca.dto.PerfilRequest;
import pe.idat.biblioteca.dto.UsuarioActualizarRequest;
import pe.idat.biblioteca.dto.UsuarioCrearRequest;
import pe.idat.biblioteca.dto.UsuarioResponse;
import pe.idat.biblioteca.entidad.NombreRol;
import pe.idat.biblioteca.entidad.Rol;
import pe.idat.biblioteca.entidad.Usuario;
import pe.idat.biblioteca.excepcion.RecursoDuplicadoException;
import pe.idat.biblioteca.excepcion.RecursoNoEncontradoException;
import pe.idat.biblioteca.excepcion.ReglaNegocioException;
import pe.idat.biblioteca.mapper.DtoMapper;
import pe.idat.biblioteca.repositorio.RefreshTokenRepositorio;
import pe.idat.biblioteca.repositorio.RolRepositorio;
import pe.idat.biblioteca.repositorio.UsuarioRepositorio;

import java.util.Set;

@Service
@Transactional
public class UsuarioServicio {

    private final UsuarioRepositorio usuarios;
    private final RolRepositorio roles;
    private final RefreshTokenRepositorio refreshTokens;
    private final PasswordEncoder passwordEncoder;

    public UsuarioServicio(UsuarioRepositorio usuarios, RolRepositorio roles, RefreshTokenRepositorio refreshTokens,
                           PasswordEncoder passwordEncoder) {
        this.usuarios = usuarios;
        this.roles = roles;
        this.refreshTokens = refreshTokens;
        this.passwordEncoder = passwordEncoder;
    }

    // ------------------------------------------------------------- ADMIN
    @Transactional(readOnly = true)
    public PaginaResponse<UsuarioResponse> listar(String texto, Pageable pageable) {
        return PaginaResponse.de(usuarios.buscar(texto == null ? "" : texto.trim(), pageable), DtoMapper::usuario);
    }

    @Transactional(readOnly = true)
    public UsuarioResponse obtener(Long id) {
        return DtoMapper.usuario(buscar(id));
    }

    /** Crea un usuario; lo usan el autorregistro (rol USUARIO) y el administrador (roles elegidos). */
    public Usuario registrar(String nombres, String apellidos, String email, String codigo, String password,
                             Set<NombreRol> nombresRol) {
        String emailNormalizado = email.trim().toLowerCase();
        if (usuarios.existsByEmailIgnoreCase(emailNormalizado)) {
            throw new RecursoDuplicadoException("El email " + emailNormalizado + " ya está registrado");
        }
        if (usuarios.existsByCodigoUniversitarioIgnoreCase(codigo.trim())) {
            throw new RecursoDuplicadoException("El código universitario " + codigo + " ya está registrado");
        }
        Usuario usuario = new Usuario(nombres.trim(), apellidos.trim(), emailNormalizado, codigo.trim().toUpperCase(),
                passwordEncoder.encode(password));
        usuario.asignarRoles(resolverRoles(nombresRol));
        return usuarios.save(usuario);
    }

    public UsuarioResponse crear(UsuarioCrearRequest req) {
        return DtoMapper.usuario(registrar(req.nombres(), req.apellidos(), req.email(), req.codigoUniversitario(),
                req.password(), req.roles()));
    }

    public UsuarioResponse actualizar(Long id, UsuarioActualizarRequest req, String emailAdmin) {
        Usuario usuario = buscar(id);
        String email = req.email().trim().toLowerCase();
        if (usuarios.existsByEmailIgnoreCaseAndIdNot(email, id)) {
            throw new RecursoDuplicadoException("El email " + email + " ya está registrado");
        }
        if (usuario.getEmail().equalsIgnoreCase(emailAdmin) && !req.roles().contains(NombreRol.ADMIN)) {
            throw new ReglaNegocioException("No puede quitarse a sí mismo el rol ADMIN");
        }
        usuario.actualizarDatos(req.nombres().trim(), req.apellidos().trim(), email);
        usuario.asignarRoles(resolverRoles(req.roles()));
        return DtoMapper.usuario(usuario);
    }

    public UsuarioResponse cambiarEstado(Long id, boolean activo, String emailAdmin) {
        Usuario usuario = buscar(id);
        if (!activo && usuario.getEmail().equalsIgnoreCase(emailAdmin)) {
            throw new ReglaNegocioException("No puede desactivar su propia cuenta");
        }
        usuario.setActivo(activo);
        if (!activo) {
            refreshTokens.revocarTodosDelUsuario(id); // cierra todas sus sesiones
        }
        return DtoMapper.usuario(usuario);
    }

    // -------------------------------------------------------- PERFIL PROPIO
    @Transactional(readOnly = true)
    public UsuarioResponse perfil(String email) {
        return DtoMapper.usuario(buscarPorEmail(email));
    }

    public UsuarioResponse actualizarPerfil(String email, PerfilRequest req) {
        Usuario usuario = buscarPorEmail(email);
        usuario.actualizarDatos(req.nombres().trim(), req.apellidos().trim(), usuario.getEmail());
        return DtoMapper.usuario(usuario);
    }

    public void cambiarPassword(String email, CambioPasswordRequest req) {
        Usuario usuario = buscarPorEmail(email);
        if (!passwordEncoder.matches(req.passwordActual(), usuario.getPassword())) {
            throw new ReglaNegocioException("La contraseña actual es incorrecta");
        }
        if (req.passwordActual().equals(req.passwordNueva())) {
            throw new ReglaNegocioException("La nueva contraseña debe ser distinta de la actual");
        }
        usuario.cambiarPassword(passwordEncoder.encode(req.passwordNueva()));
        refreshTokens.revocarTodosDelUsuario(usuario.getId()); // obliga a iniciar sesión otra vez
    }

    // ------------------------------------------------------------- apoyo
    public Usuario buscar(Long id) {
        return usuarios.findById(id).orElseThrow(() -> new RecursoNoEncontradoException("Usuario", id));
    }

    public Usuario buscarPorEmail(String email) {
        return usuarios.findByEmailIgnoreCase(email)
                .orElseThrow(() -> new RecursoNoEncontradoException("No existe el usuario " + email));
    }

    private Set<Rol> resolverRoles(Set<NombreRol> nombres) {
        Set<Rol> encontrados = roles.findByNombreIn(nombres);
        if (encontrados.size() != nombres.size()) {
            throw new RecursoNoEncontradoException("Alguno de los roles indicados no existe: " + nombres);
        }
        return encontrados;
    }
}
