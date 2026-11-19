package pe.idat.biblioteca.excepcion;

/** Refresh token inexistente, revocado o vencido (HTTP 401). */
public class TokenInvalidoException extends RuntimeException {

    public TokenInvalidoException(String mensaje) {
        super(mensaje);
    }
}
