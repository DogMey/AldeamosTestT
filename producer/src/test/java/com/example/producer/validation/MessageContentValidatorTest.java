package com.example.producer.validation;

import com.example.producer.dto.MessageRequestDto;
import com.example.producer.entity.MessageType;
import jakarta.validation.ConstraintValidatorContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("MessageContentValidator — content condicional por messageType")
class MessageContentValidatorTest {

    private MessageContentValidator validator;

    @Mock private ConstraintValidatorContext context;
    @Mock private ConstraintValidatorContext.ConstraintViolationBuilder violationBuilder;
    @Mock private ConstraintValidatorContext.ConstraintViolationBuilder.NodeBuilderCustomizableContext nodeBuilder;

    @BeforeEach
    void setUp() {
        validator = new MessageContentValidator();
        when(context.buildConstraintViolationWithTemplate(anyString())).thenReturn(violationBuilder);
        when(violationBuilder.addPropertyNode(anyString())).thenReturn(nodeBuilder);
    }

    @Test
    @DisplayName("TEXTO con cualquier contenido debe ser válido")
    void textoShouldBeValidRegardlessOfContent() {
        var dto = new MessageRequestDto("+573001234567", "+573009876543", MessageType.TEXTO, "Hola mundo");
        assertThat(validator.isValid(dto, context)).isTrue();
    }

    @Test
    @DisplayName("IMAGEN con URL https válida debe ser válido")
    void imagenWithValidUrlShouldBeValid() {
        var dto = new MessageRequestDto("+573001234567", "+573009876543",
                MessageType.IMAGEN, "https://cdn.example.com/foto.jpg");
        assertThat(validator.isValid(dto, context)).isTrue();
    }

    @Test
    @DisplayName("IMAGEN sin content debe ser inválido")
    void imagenWithNullContentShouldBeInvalid() {
        var dto = new MessageRequestDto("+573001234567", "+573009876543", MessageType.IMAGEN, null);
        assertThat(validator.isValid(dto, context)).isFalse();
    }

    @Test
    @DisplayName("DOCUMENTO con texto plano (no URL) debe ser inválido")
    void documentoWithPlainTextShouldBeInvalid() {
        var dto = new MessageRequestDto("+573001234567", "+573009876543",
                MessageType.DOCUMENTO, "esto-no-es-una-url");
        assertThat(validator.isValid(dto, context)).isFalse();
    }
}
