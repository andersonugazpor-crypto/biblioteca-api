package pe.idat.biblioteca.servicio;

import org.springframework.data.domain.Pageable;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pe.idat.biblioteca.config.PropiedadesBiblioteca;
import pe.idat.biblioteca.dto.DevolucionRequest;
import pe.idat.biblioteca.dto.PaginaResponse;
import pe.idat.biblioteca.dto.PrestamoRequest;
import pe.idat.biblioteca.dto.PrestamoResponse;
import pe.idat.biblioteca.entidad.EstadoPrestamo;
import pe.idat.biblioteca.entidad.Libro;
import pe.idat.biblioteca.entidad.Prestamo;
import pe.idat.biblioteca.entidad.Usuario;
import pe.idat.biblioteca.excepcion.RecursoNoEncontradoException;
import pe.idat.biblioteca.excepcion.ReglaNegocioException;
import pe.idat.biblioteca.mapper.DtoMapper;
import pe.idat.biblioteca.repositorio.PrestamoRepositorio;

import java.time.Clock;
import java.time.LocalDate;
import java.util.List;

/**
 * Lógica de préstamos y devoluciones. Reglas al prestar:
 * usuario activo, sin préstamos vencidos, por debajo del máximo de préstamos activos,
 * sin otro ejemplar del mismo libro y con ejemplares disponibles.
 */
@Service
@Transactional
public class PrestamoServicio {

    private final PrestamoRepositorio prestamos;
    private final UsuarioServicio usuarios;
    private final LibroServicio libros;
    private final PropiedadesBiblioteca reglas;
    private final Clock reloj;

    public PrestamoServicio(PrestamoRepositorio prestamos, UsuarioServicio usuarios, LibroServicio libros,
                            PropiedadesBiblioteca reglas, Clock reloj) {
        this.prestamos = prestamos;
        this.usuarios = usuarios;
        this.libros = libros;
        this.reglas = reglas;
        this.reloj = reloj;
    }

    public PrestamoResponse registrar(PrestamoRequest req) {
        LocalDate hoy = LocalDate.now(reloj);
        Usuario usuario = usuarios.buscar(req.usuarioId());
        Libro libro = libros.buscarActivo(req.libroId());

        if (!usuario.isActivo()) {
            throw new ReglaNegocioException("El usuario " + usuario.getNombreCompleto() + " está inactivo");
        }
        if (prestamos.existsByUsuarioIdAndEstadoAndFechaVencimientoBefore(usuario.getId(), EstadoPrestamo.ACTIVO, hoy)) {
            throw new ReglaNegocioException("El usuario tiene préstamos vencidos sin devolver");
        }
        long activos = prestamos.countByUsuarioIdAndEstado(usuario.getId(), EstadoPrestamo.ACTIVO);
        if (activos >= reglas.maxPrestamosActivos()) {
            throw new ReglaNegocioException("El usuario alcanzó el máximo de " + reglas.maxPrestamosActivos()
                    + " préstamos activos");
        }
        if (prestamos.existsByUsuarioIdAndLibroIdAndEstado(usuario.getId(), libro.getId(), EstadoPrestamo.ACTIVO)) {
            throw new ReglaNegocioException("El usuario ya tiene un ejemplar de \"" + libro.getTitulo() + "\" en préstamo");
        }

        libro.prestarEjemplar();
        int dias = req.diasPrestamo() != null ? req.diasPrestamo() : reglas.diasPrestamoPorDefecto();
        Prestamo prestamo = prestamos.save(new Prestamo(usuario, libro, hoy, dias, req.observaciones()));
        return DtoMapper.prestamo(prestamo, hoy);
    }

    public PrestamoResponse devolver(Long id, DevolucionRequest req) {
        LocalDate hoy = LocalDate.now(reloj);
        Prestamo prestamo = buscar(id);
        LocalDate fecha = req != null && req.fechaDevolucion() != null ? req.fechaDevolucion() : hoy;
        prestamo.registrarDevolucion(fecha, reglas.multaPorDia(), req != null ? req.observaciones() : null);
        return DtoMapper.prestamo(prestamo, hoy);
    }

    @Transactional(readOnly = true)
    public PaginaResponse<PrestamoResponse> listar(EstadoPrestamo estado, Long usuarioId, Pageable pageable) {
        LocalDate hoy = LocalDate.now(reloj);
        return PaginaResponse.de(prestamos.buscar(estado, usuarioId, pageable), p -> DtoMapper.prestamo(p, hoy));
    }

    @Transactional(readOnly = true)
    public List<PrestamoResponse> vencidos() {
        LocalDate hoy = LocalDate.now(reloj);
        return prestamos.vencidos(hoy).stream().map(p -> DtoMapper.prestamo(p, hoy)).toList();
    }

    /** Historial del usuario autenticado (rol USUARIO). */
    @Transactional(readOnly = true)
    public PaginaResponse<PrestamoResponse> historialDe(String email, EstadoPrestamo estado, Pageable pageable) {
        LocalDate hoy = LocalDate.now(reloj);
        Long usuarioId = usuarios.buscarPorEmail(email).getId();
        var pagina = estado == null
                ? prestamos.findByUsuarioIdOrderByFechaPrestamoDescIdDesc(usuarioId, pageable)
                : prestamos.findByUsuarioIdAndEstadoOrderByFechaPrestamoDescIdDesc(usuarioId, estado, pageable);
        return PaginaResponse.de(pagina, p -> DtoMapper.prestamo(p, hoy));
    }

    /** Un ADMIN ve cualquier préstamo; un USUARIO solo los suyos. */
    @Transactional(readOnly = true)
    public PrestamoResponse obtener(Long id, String emailSolicitante, boolean esAdmin) {
        Prestamo prestamo = buscar(id);
        if (!esAdmin && !prestamo.getUsuario().getEmail().equalsIgnoreCase(emailSolicitante)) {
            throw new AccessDeniedException("Solo puede consultar sus propios préstamos");
        }
        return DtoMapper.prestamo(prestamo, LocalDate.now(reloj));
    }

    private Prestamo buscar(Long id) {
        return prestamos.findDetalleById(id).orElseThrow(() -> new RecursoNoEncontradoException("Préstamo", id));
    }
}
