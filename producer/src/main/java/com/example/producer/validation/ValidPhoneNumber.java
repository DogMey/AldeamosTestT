package com.example.producer.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.*;

/**
 * Valida que el número de teléfono cumpla el estándar E.164 (ITU-T E.164):
 * - Comienza con '+' seguido del código de país (1-3 dígitos)
 * - Longitud total: mínimo 8 caracteres, máximo 16 caracteres (+ hasta 15 dígitos)
 * - Solo dígitos después del '+'
 *
 * Ejemplos válidos: +573001234567 (Colombia), +1234567890 (EE.UU.)
 */
@Documented
@Constraint(validatedBy = PhoneNumberValidator.class)
@Target({ElementType.FIELD, ElementType.RECORD_COMPONENT})
@Retention(RetentionPolicy.RUNTIME)
public @interface ValidPhoneNumber {

    String message() default "Invalid phone number. Must follow E.164 format (e.g. +573001234567): " +
            "starts with '+', country code, and 6-14 digits. Max 15 digits total.";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
