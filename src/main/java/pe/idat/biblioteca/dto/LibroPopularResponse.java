package pe.idat.biblioteca.dto;

public record LibroPopularResponse(Long id, String titulo, String autor, long totalPrestamos) {
}
