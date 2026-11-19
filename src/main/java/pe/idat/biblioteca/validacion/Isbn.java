package pe.idat.biblioteca.validacion;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Validación personalizada: el valor debe ser un ISBN-10 o ISBN-13 con dígito
 * verificador correcto. Se permiten guiones y espacios.
 */
@Documented
@Constraint(validatedBy = IsbnValidator.class)
@Target({ElementType.FIELD, ElementType.PARAMETER, ElementType.RECORD_COMPONENT})
@Retention(RetentionPolicy.RUNTIME)
public @interface Isbn {

    String message() default "El ISBN no es válido (verifique los 10 o 13 dígitos)";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
