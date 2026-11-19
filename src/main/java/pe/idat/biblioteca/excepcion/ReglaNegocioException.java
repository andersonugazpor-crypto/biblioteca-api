package pe.idat.biblioteca.excepcion;

/** Una regla de negocio impide la operación, p. ej. libro sin ejemplares (HTTP 422). */
public class ReglaNegocioException extends RuntimeException {

    public ReglaNegocioException(String mensaje) {
        super(mensaje);
    }
}
