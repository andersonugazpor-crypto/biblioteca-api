package pe.idat.biblioteca.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.math.BigDecimal;

/**
 * Reglas configurables de la biblioteca (prefijo "biblioteca" en application.properties).
 *
 * @param maxPrestamosActivos préstamos simultáneos permitidos por usuario
 * @param diasPrestamoPorDefecto días de préstamo si no se indican
 * @param multaPorDia multa en soles por cada día de retraso
 */
@ConfigurationProperties(prefix = "biblioteca")
public record PropiedadesBiblioteca(int maxPrestamosActivos, int diasPrestamoPorDefecto, BigDecimal multaPorDia) {
}
