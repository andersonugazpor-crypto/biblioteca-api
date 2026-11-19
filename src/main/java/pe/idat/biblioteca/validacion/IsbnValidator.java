package pe.idat.biblioteca.validacion;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

/** Verifica el dígito de control de ISBN-10 (módulo 11) e ISBN-13 (módulo 10). */
public class IsbnValidator implements ConstraintValidator<Isbn, String> {

    @Override
    public boolean isValid(String valor, ConstraintValidatorContext contexto) {
        if (valor == null || valor.isBlank()) {
            return true; // la obligatoriedad la valida @NotBlank
        }
        return esValido(valor);
    }

    public static String normalizar(String isbn) {
        return isbn == null ? null : isbn.replaceAll("[\\s-]", "").toUpperCase();
    }

    public static boolean esValido(String isbn) {
        String limpio = normalizar(isbn);
        if (limpio.matches("\\d{9}[\\dX]")) {
            return esIsbn10(limpio);
        }
        if (limpio.matches("\\d{13}")) {
            return esIsbn13(limpio);
        }
        return false;
    }

    private static boolean esIsbn10(String s) {
        int suma = 0;
        for (int i = 0; i < 10; i++) {
            char c = s.charAt(i);
            int valor = (c == 'X') ? 10 : c - '0';
            suma += valor * (10 - i);
        }
        return suma % 11 == 0;
    }

    private static boolean esIsbn13(String s) {
        int suma = 0;
        for (int i = 0; i < 13; i++) {
            int valor = s.charAt(i) - '0';
            suma += (i % 2 == 0) ? valor : valor * 3;
        }
        return suma % 10 == 0;
    }
}
