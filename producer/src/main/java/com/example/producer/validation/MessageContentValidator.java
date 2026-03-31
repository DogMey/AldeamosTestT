package com.example.producer.validation;

import com.example.producer.dto.MessageRequestDto;
import com.example.producer.entity.MessageType;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Set;
import java.util.regex.Pattern;

/**
 * Implementación del validador para {@code @ValidMessageContent}.
 *
 * Lógica:
 *  - Tipos multimedia: IMAGEN, VIDEO, DOCUMENTO → content debe ser URL http/https válida.
 *  - Tipo TEXTO → content es obligatorio (validado por @NotBlank en el DTO), sin restricción de formato.
 */
public class MessageContentValidator implements ConstraintValidator<ValidMessageContent, MessageRequestDto> {

    private static final Logger log = LoggerFactory.getLogger(MessageContentValidator.class);

    private static final Set<MessageType> MULTIMEDIA_TYPES = Set.of(
            MessageType.IMAGEN, MessageType.VIDEO, MessageType.DOCUMENTO
    );

    /**
     * Patrón URL http/https — cubre rutas, query strings y fragmentos.
     * RFC 3986 simplificado: esquema obligatorio http o https.
     */
    private static final Pattern URL_PATTERN = Pattern.compile(
            "^https?://(www\\.)?[-a-zA-Z0-9@:%._+~#=]{1,256}\\.[a-zA-Z0-9()]{1,6}\\b([-a-zA-Z0-9()@:%_+.~#?&/=]*)$"
    );

    @Override
    public boolean isValid(MessageRequestDto dto, ConstraintValidatorContext context) {
        if (dto == null || dto.messageType() == null) {
            // @NotNull en messageType se encarga de esto
            return true;
        }

        if (!MULTIMEDIA_TYPES.contains(dto.messageType())) {
            // TEXTO: sin restricciones sobre content
            return true;
        }

        // Multimedia: content obligatorio y debe ser URL válida
        String content = dto.content();

        if (content == null || content.isBlank()) {
            log.debug("Validation failed: content is required for messageType='{}'", dto.messageType());
            buildViolation(context, "content is required for multimedia messages (IMAGEN, VIDEO, DOCUMENTO)");
            return false;
        }

        if (!URL_PATTERN.matcher(content).matches()) {
            log.debug("Validation failed: content is not a valid URL for messageType='{}', content='{}'",
                    dto.messageType(), content);
            buildViolation(context, "content must be a valid http/https URL for multimedia messages");
            return false;
        }

        return true;
    }

    private void buildViolation(ConstraintValidatorContext context, String message) {
        context.disableDefaultConstraintViolation();
        context.buildConstraintViolationWithTemplate(message)
                .addPropertyNode("content")
                .addConstraintViolation();
    }
}
