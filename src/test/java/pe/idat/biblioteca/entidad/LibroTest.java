package pe.idat.biblioteca.entidad;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import pe.idat.biblioteca.excepcion.ReglaNegocioException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;

@DisplayName("Libro: control de ejemplares")
class LibroTest {

    private Libro libro(int ejemplares) {
        return new Libro("9780134685991", "Effective Java", "Joshua Bloch", "Addison-Wesley", 2018, ejemplares);
    }

    @Test
    @DisplayName("Prestar y devolver actualiza los ejemplares disponibles")
    void prestarYDevolver() {
        Libro l = libro(2);
        l.prestarEjemplar();
        assertEquals(1, l.getEjemplaresDisponibles());
        assertEquals(1, l.getEjemplaresPrestados());
        l.devolverEjemplar();
        assertEquals(2, l.getEjemplaresDisponibles());
    }

    @Test
    @DisplayName("No se puede prestar si no hay ejemplares disponibles")
    void sinEjemplares() {
        Libro l = libro(1);
        l.prestarEjemplar();
        assertThrows(ReglaNegocioException.class, l::prestarEjemplar);
    }

    @Test
    @DisplayName("No se puede devolver más ejemplares de los que existen")
    void devolverDeMas() {
        assertThrows(ReglaNegocioException.class, libro(1)::devolverEjemplar);
    }

    @Test
    @DisplayName("El total de ejemplares no puede quedar por debajo de los prestados")
    void actualizarTotal() {
        Libro l = libro(3);
        l.prestarEjemplar();
        l.prestarEjemplar();
        l.actualizarEjemplaresTotales(5);
        assertEquals(3, l.getEjemplaresDisponibles());
        assertThrows(ReglaNegocioException.class, () -> l.actualizarEjemplaresTotales(1));
    }

    @Test
    @DisplayName("Baja lógica solo sin ejemplares prestados")
    void darDeBaja() {
        Libro prestado = libro(1);
        prestado.prestarEjemplar();
        assertThrows(ReglaNegocioException.class, prestado::darDeBaja);

        Libro libre = libro(1);
        libre.darDeBaja();
        assertFalse(libre.isActivo());
        assertThrows(ReglaNegocioException.class, libre::prestarEjemplar);
    }
}
