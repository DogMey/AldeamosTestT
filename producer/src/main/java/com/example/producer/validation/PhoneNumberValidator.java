package com.example.producer.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.regex.Pattern;

/**
 * Implementación del validador para @ValidPhoneNumber.
 *
 * Reglas (estándar E.164 — ITU-T):
 *  - Debe comenzar con '+'
 *  - Seguido del código de país (1-3 dígitos, no inicia en 0)
 *  - Luego entre 4 y 14 dígitos adicionales del número
 *  - Total de dígitos (sin el '+'): máximo 15
 *  - Longitud total con '+': mínimo 8, máximo 16 caracteres
 */
public class PhoneNumberValidator implements ConstraintValidator<ValidPhoneNumber, String> {

    private static final Logger log = LoggerFactory.getLogger(PhoneNumberValidator.class);

    /**
     * E.164: ^\+[1-9]\d{6,14}$
     *  - \+       → símbolo '+' obligatorio
     *  - [1-9]    → primer dígito del código de país (no puede ser 0)
     *  - \d{6,14} → entre 6 y 14 dígitos más (total: 7-15 dígitos sin el '+')
     */
    private static final Pattern E164_PATTERN = Pattern.compile("^\\+[1-9]\\d{6,14}$");

    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        if (value == null || value.isBlank()) {
            // La nulidad/blanco es responsabilidad de @NotBlank
            return true;
        }

        boolean valid = E164_PATTERN.matcher(value).matches();
        if (!valid) {
            log.debug("Phone number validation failed for value='{}'", value);
        }
        return valid;
    }
}
