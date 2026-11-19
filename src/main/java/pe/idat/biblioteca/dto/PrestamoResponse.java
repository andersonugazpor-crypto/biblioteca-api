package pe.idat.biblioteca.dto;

import pe.idat.biblioteca.entidad.EstadoPrestamo;

import java.math.BigDecimal;
import java.time.LocalDate;

public record PrestamoResponse(
        Long id,
        Long usuarioId,
        String usuarioNombre,
        String codigoUniversitario,
        Long libroId,
        String libroTitulo,
        String isbn,
        LocalDate fechaPrestamo,
        LocalDate fechaVencimiento,
        LocalDate fechaDevolucion,
        EstadoPrestamo estado,
        boolean vencido,
        long diasRetraso,
        BigDecimal multa,
        String observaciones) {
}
