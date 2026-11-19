package pe.idat.biblioteca.servicio;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import pe.idat.biblioteca.config.PropiedadesBiblioteca;
import pe.idat.biblioteca.dto.DevolucionRequest;
import pe.idat.biblioteca.dto.PrestamoRequest;
import pe.idat.biblioteca.dto.PrestamoResponse;
import pe.idat.biblioteca.entidad.EstadoPrestamo;
import pe.idat.biblioteca.entidad.Libro;
import pe.idat.biblioteca.entidad.Prestamo;
import pe.idat.biblioteca.entidad.Usuario;
import pe.idat.biblioteca.excepcion.RecursoNoEncontradoException;
import pe.idat.biblioteca.excepcion.ReglaNegocioException;
import pe.idat.biblioteca.repositorio.PrestamoRepositorio;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("PrestamoServicio: reglas de negocio (Mockito)")
class PrestamoServicioTest {

    private static final ZoneId LIMA = ZoneId.of("America/Lima");
    private static final LocalDate HOY = LocalDate.of(2026, 11, 19);

    @Mock private PrestamoRepositorio prestamos;
    @Mock private UsuarioServicio usuarios;
    @Mock private LibroServicio libros;

    private PrestamoServicio servicio;
    private Usuario usuario;
    private Libro libro;

    @BeforeEach
    void preparar() {
        Clock reloj = Clock.fixed(Instant.parse("2026-11-19T15:00:00Z"), LIMA);
        servicio = new PrestamoServicio(prestamos, usuarios, libros,
                new PropiedadesBiblioteca(3, 7, new BigDecimal("1.00")), reloj);
        usuario = new Usuario("Jorge", "Ramos", "jorge@idat.edu.pe", "U20240003", "hash");
        usuario.asignarId(3L);
        libro = new Libro("9780132350884", "Clean Code", "Robert C. Martin", "Prentice Hall", 2008, 2);
        libro.asignarId(10L);
    }

    private void usuarioYLibroExisten() {
        when(usuarios.buscar(3L)).thenReturn(usuario);
        when(libros.buscarActivo(10L)).thenReturn(libro);
    }

    @Test
    @DisplayName("Registra el préstamo, descuenta un ejemplar y vence en 7 días por defecto")
    void registraPrestamo() {
        usuarioYLibroExisten();
        when(prestamos.existsByUsuarioIdAndEstadoAndFechaVencimientoBefore(3L, EstadoPrestamo.ACTIVO, HOY)).thenReturn(false);
        when(prestamos.countByUsuarioIdAndEstado(3L, EstadoPrestamo.ACTIVO)).thenReturn(1L);
        when(prestamos.existsByUsuarioIdAndLibroIdAndEstado(3L, 10L, EstadoPrestamo.ACTIVO)).thenReturn(false);
        when(prestamos.save(any(Prestamo.class))).thenAnswer(inv -> inv.getArgument(0));

        PrestamoResponse r = servicio.registrar(new PrestamoRequest(3L, 10L, null, null));

        assertEquals(EstadoPrestamo.ACTIVO, r.estado());
        assertEquals(HOY.plusDays(7), r.fechaVencimiento());
        assertEquals(1, libro.getEjemplaresDisponibles());
    }

    @Test
    @DisplayName("Rechaza el préstamo si el usuario está inactivo")
    void usuarioInactivo() {
        usuarioYLibroExisten();
        usuario.setActivo(false);
        assertThrows(ReglaNegocioException.class, () -> servicio.registrar(new PrestamoRequest(3L, 10L, 7, null)));
        verify(prestamos, never()).save(any(Prestamo.class));
    }

    @Test
    @DisplayName("Rechaza el préstamo si el usuario tiene préstamos vencidos")
    void conVencidos() {
        usuarioYLibroExisten();
        when(prestamos.existsByUsuarioIdAndEstadoAndFechaVencimientoBefore(3L, EstadoPrestamo.ACTIVO, HOY)).thenReturn(true);
        ReglaNegocioException ex = assertThrows(ReglaNegocioException.class,
                () -> servicio.registrar(new PrestamoRequest(3L, 10L, 7, null)));
        assertTrue(ex.getMessage().contains("vencidos"));
    }

    @Test
    @DisplayName("Rechaza el préstamo al alcanzar el máximo de 3 préstamos activos")
    void maximoAlcanzado() {
        usuarioYLibroExisten();
        when(prestamos.existsByUsuarioIdAndEstadoAndFechaVencimientoBefore(3L, EstadoPrestamo.ACTIVO, HOY)).thenReturn(false);
        when(prestamos.countByUsuarioIdAndEstado(3L, EstadoPrestamo.ACTIVO)).thenReturn(3L);
        assertThrows(ReglaNegocioException.class, () -> servicio.registrar(new PrestamoRequest(3L, 10L, 7, null)));
        assertEquals(2, libro.getEjemplaresDisponibles());
    }

    @Test
    @DisplayName("Rechaza un segundo ejemplar del mismo libro al mismo usuario")
    void mismoLibro() {
        usuarioYLibroExisten();
        when(prestamos.existsByUsuarioIdAndEstadoAndFechaVencimientoBefore(3L, EstadoPrestamo.ACTIVO, HOY)).thenReturn(false);
        when(prestamos.countByUsuarioIdAndEstado(3L, EstadoPrestamo.ACTIVO)).thenReturn(0L);
        when(prestamos.existsByUsuarioIdAndLibroIdAndEstado(3L, 10L, EstadoPrestamo.ACTIVO)).thenReturn(true);
        assertThrows(ReglaNegocioException.class, () -> servicio.registrar(new PrestamoRequest(3L, 10L, 7, null)));
    }

    @Test
    @DisplayName("La devolución tardía calcula la multa por días de retraso")
    void devolucionConMulta() {
        libro.prestarEjemplar();
        Prestamo p = new Prestamo(usuario, libro, HOY.minusDays(10), 7, null); // venció hace 3 días
        p.asignarId(50L);
        when(prestamos.findDetalleById(50L)).thenReturn(Optional.of(p));

        PrestamoResponse r = servicio.devolver(50L, new DevolucionRequest(null, null));

        assertEquals(EstadoPrestamo.DEVUELTO, r.estado());
        assertEquals(new BigDecimal("3.00"), r.multa());
        assertEquals(2, libro.getEjemplaresDisponibles());
    }

    @Test
    @DisplayName("Devolver un préstamo inexistente produce 404")
    void prestamoInexistente() {
        when(prestamos.findDetalleById(99L)).thenReturn(Optional.empty());
        assertThrows(RecursoNoEncontradoException.class, () -> servicio.devolver(99L, null));
    }
}
