package com.example.producer.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.*;

/**
 * Validación a nivel de clase para {@code MessageRequestDto}.
 *
 * Regla de negocio:
 *  - Si messageType es IMAGEN, VIDEO o DOCUMENTO → content debe ser una URL válida (http/https).
 *  - Si messageType es TEXTO → content es obligatorio pero sin restricción de formato.
 */
@Documented
@Constraint(validatedBy = MessageContentValidator.class)
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface ValidMessageContent {

    String message() default "For multimedia messages (IMAGEN, VIDEO, DOCUMENTO), content must be a valid http/https URL";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
