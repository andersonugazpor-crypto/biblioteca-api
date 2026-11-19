package pe.idat.biblioteca.validacion;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DisplayName("Validación personalizada @Isbn")
class IsbnValidatorTest {

    @Test
    @DisplayName("Acepta ISBN-13 e ISBN-10 válidos, con o sin guiones")
    void validos() {
        for (String isbn : List.of("978-0134685991", "9780132350884", "0-201-63361-2", "080442957X", "978 1 4493 7332 0")) {
            assertTrue(IsbnValidator.esValido(isbn), isbn);
        }
    }

    @Test
    @DisplayName("Rechaza dígito verificador incorrecto, longitudes y caracteres inválidos")
    void invalidos() {
        for (String isbn : List.of("9780134685990", "0201633611", "12345", "97801346859912", "ABCDEFGHIJ")) {
            assertFalse(IsbnValidator.esValido(isbn), isbn);
        }
    }

    @Test
    @DisplayName("Los ISBN de los datos de ejemplo son válidos")
    void datosDeEjemplo() {
        for (String isbn : List.of("9780132350884", "9780134685991", "9781617294945", "9780201633610", "9781492034025",
                "9780073523323", "9781449373320", "9780133594140", "9780538497817", "9780307474728", "9788420471839",
                "9786124169014")) {
            assertTrue(IsbnValidator.esValido(isbn), isbn);
        }
    }

    @Test
    @DisplayName("Normaliza quitando guiones y espacios")
    void normalizar() {
        assertEquals("9780134685991", IsbnValidator.normalizar("978-0-13-468599 1"));
        assertTrue(new IsbnValidator().isValid(null, null));
    }
}
