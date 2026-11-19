package pe.idat.biblioteca.dto;

import org.springframework.data.domain.Page;

import java.util.List;
import java.util.function.Function;

/** Envoltorio estable para respuestas paginadas. */
public record PaginaResponse<T>(
        List<T> contenido,
        int pagina,
        int tamano,
        long totalElementos,
        int totalPaginas,
        boolean ultima) {

    public static <E, T> PaginaResponse<T> de(Page<E> pagina, Function<E, T> mapeo) {
        return new PaginaResponse<>(pagina.getContent().stream().map(mapeo).toList(),
                pagina.getNumber(), pagina.getSize(), pagina.getTotalElements(), pagina.getTotalPages(), pagina.isLast());
    }
}
