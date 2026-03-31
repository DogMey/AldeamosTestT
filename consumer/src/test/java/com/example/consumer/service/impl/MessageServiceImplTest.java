package com.example.consumer.service.impl;

import com.example.consumer.document.MessageDocument;
import com.example.consumer.dto.MessageRequestDto;
import com.example.consumer.repository.MessageRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class MessageServiceImplTest {

    @Mock
    MessageRepository messageRepository;

    @InjectMocks
    MessageServiceImpl service;

    private static final MessageRequestDto DTO = new MessageRequestDto(
            "+573001234567", "+573009876543", "TEXTO", "Hola");

    @Test
    void whenUnderLimit_savesWithoutError() {
        when(messageRepository.countByDestinationAndCreatedDateAfterAndErrorIsNull(
                eq(DTO.destination()), any(Instant.class))).thenReturn(2L);

        service.processMessage(DTO, System.currentTimeMillis());

        ArgumentCaptor<MessageDocument> captor = ArgumentCaptor.forClass(MessageDocument.class);
        verify(messageRepository).save(captor.capture());

        MessageDocument saved = captor.getValue();
        assertThat(saved.getError()).isNull();
        assertThat(saved.getDestination()).isEqualTo(DTO.destination());
        assertThat(saved.getProcessingTime()).isGreaterThanOrEqualTo(0L);
    }

    @Test
    void whenLimitReached_savesWithErrorField() {
        when(messageRepository.countByDestinationAndCreatedDateAfterAndErrorIsNull(
                eq(DTO.destination()), any(Instant.class))).thenReturn(3L);

        service.processMessage(DTO, System.currentTimeMillis());

        ArgumentCaptor<MessageDocument> captor = ArgumentCaptor.forClass(MessageDocument.class);
        verify(messageRepository).save(captor.capture());

        assertThat(captor.getValue().getError()).contains("limit").containsIgnoringCase("3");
    }

    @Test
    void whenNoReceptionTimestamp_processingTimeIsZero() {
        when(messageRepository.countByDestinationAndCreatedDateAfterAndErrorIsNull(
                any(), any())).thenReturn(0L);

        service.processMessage(DTO, 0L);

        ArgumentCaptor<MessageDocument> captor = ArgumentCaptor.forClass(MessageDocument.class);
        verify(messageRepository).save(captor.capture());
        assertThat(captor.getValue().getProcessingTime()).isEqualTo(0L);
    }
}
