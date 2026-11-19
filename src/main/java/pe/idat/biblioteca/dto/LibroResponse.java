package pe.idat.biblioteca.dto;

import java.util.List;

public record LibroResponse(
        Long id,
        String isbn,
        String titulo,
        String autor,
        String editorial,
        Integer anioPublicacion,
        int ejemplaresTotales,
        int ejemplaresDisponibles,
        boolean disponible,
        List<CategoriaResumen> categorias) {
}
