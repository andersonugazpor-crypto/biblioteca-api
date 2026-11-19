package pe.idat.biblioteca.excepcion;

/** Se intenta registrar un dato único que ya existe, p. ej. ISBN o email (HTTP 409). */
public class RecursoDuplicadoException extends RuntimeException {

    public RecursoDuplicadoException(String mensaje) {
        super(mensaje);
    }
}
