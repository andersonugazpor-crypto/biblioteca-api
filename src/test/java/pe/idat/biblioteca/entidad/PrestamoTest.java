package pe.idat.biblioteca.entidad;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import pe.idat.biblioteca.excepcion.ReglaNegocioException;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DisplayName("Préstamo: vencimiento, retraso y multa")
class PrestamoTest {

    private static final LocalDate INICIO = LocalDate.of(2026, 11, 1);
    private static final BigDecimal MULTA_DIA = new BigDecimal("1.00");

    private Libro libro;
    private Prestamo prestamo;

    @BeforeEach
    void preparar() {
        libro = new Libro("9780132350884", "Clean Code", "Robert C. Martin", "Prentice Hall", 2008, 2);
        Usuario usuario = new Usuario("Carlos", "Quispe", "carlos@idat.edu.pe", "U20240001", "hash");
        libro.prestarEjemplar();
        prestamo = new Prestamo(usuario, libro, INICIO, 7, null);
    }

    @Test
    @DisplayName("La fecha de vencimiento es la fecha de préstamo más los días indicados")
    void fechaVencimiento() {
        assertEquals(LocalDate.of(2026, 11, 8), prestamo.getFechaVencimiento());
        assertEquals(EstadoPrestamo.ACTIVO, prestamo.getEstado());
    }

    @Test
    @DisplayName("Está vencido solo después de la fecha de vencimiento")
    void vencido() {
        assertFalse(prestamo.estaVencido(LocalDate.of(2026, 11, 8)));
        assertTrue(prestamo.estaVencido(LocalDate.of(2026, 11, 9)));
        assertEquals(3, prestamo.diasRetraso(LocalDate.of(2026, 11, 11)));
    }

    @Test
    @DisplayName("Devolución a tiempo: sin multa y el ejemplar vuelve al libro")
    void devolucionATiempo() {
        prestamo.registrarDevolucion(LocalDate.of(2026, 11, 5), MULTA_DIA, null);
        assertEquals(EstadoPrestamo.DEVUELTO, prestamo.getEstado());
        assertEquals(0, prestamo.getMulta().signum());
        assertEquals(2, libro.getEjemplaresDisponibles());
        assertFalse(prestamo.estaVencido(LocalDate.of(2026, 12, 1)));
    }

    @Test
    @DisplayName("Devolución con 4 días de retraso: multa de S/ 4.00")
    void devolucionConRetraso() {
        prestamo.registrarDevolucion(LocalDate.of(2026, 11, 12), MULTA_DIA, "Tapa dañada");
        assertEquals(new BigDecimal("4.00"), prestamo.getMulta());
        assertEquals("Tapa dañada", prestamo.getObservaciones());
    }

    @Test
    @DisplayName("No se puede devolver dos veces ni antes de la fecha de préstamo")
    void devolucionesInvalidas() {
        assertThrows(ReglaNegocioException.class,
                () -> prestamo.registrarDevolucion(LocalDate.of(2026, 10, 30), MULTA_DIA, null));
        prestamo.registrarDevolucion(LocalDate.of(2026, 11, 5), MULTA_DIA, null);
        assertThrows(ReglaNegocioException.class,
                () -> prestamo.registrarDevolucion(LocalDate.of(2026, 11, 6), MULTA_DIA, null));
    }
}
