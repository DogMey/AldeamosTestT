package com.example.producer.validation;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("PhoneNumberValidator — E.164")
class PhoneNumberValidatorTest {

    private PhoneNumberValidator validator;

    @BeforeEach
    void setUp() {
        validator = new PhoneNumberValidator();
    }

    @ParameterizedTest(name = "válido: {0}")
    @ValueSource(strings = { "+573001234567", "+12025550123", "+441234567890" })
    @DisplayName("debe aceptar números E.164 válidos")
    void shouldAcceptValidNumbers(String phone) {
        assertThat(validator.isValid(phone, null)).isTrue();
    }

    @ParameterizedTest(name = "inválido: {0}")
    @ValueSource(strings = {
            "3001234567",        // sin '+'
            "+0573001234567",    // código de país inicia en 0
            "+57300",            // demasiado corto
            "+57 300 1234567",   // contiene espacios
            "+57abc1234"         // contiene letras
    })
    @DisplayName("debe rechazar números que no cumplen E.164")
    void shouldRejectInvalidNumbers(String phone) {
        assertThat(validator.isValid(phone, null)).isFalse();
    }
}
