package com.example.producer.service.impl;

import com.example.producer.config.AuthProperties;
import com.example.producer.dto.AuthRequestDto;
import com.example.producer.dto.AuthResponseDto;
import com.example.producer.security.JwtService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.BadCredentialsException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("AuthServiceImpl — autenticación")
class AuthServiceImplTest {

    @Mock private AuthProperties authProperties;
    @Mock private JwtService jwtService;
    @InjectMocks private AuthServiceImpl authService;

    @BeforeEach
    void setUp() {
        when(authProperties.getUsername()).thenReturn("admin");
        when(authProperties.getPassword()).thenReturn("secret");
    }

    @Test
    @DisplayName("credenciales correctas deben retornar token Bearer")
    void validCredentialsShouldReturnToken() {
        when(jwtService.generateToken("admin")).thenReturn("fake.jwt.token");
        when(jwtService.getExpirationMs()).thenReturn(86400000L);

        AuthResponseDto response = authService.login(new AuthRequestDto("admin", "secret"));

        assertThat(response.accessToken()).isEqualTo("fake.jwt.token");
        assertThat(response.tokenType()).isEqualTo("Bearer");
    }

    @Test
    @DisplayName("credenciales incorrectas deben lanzar BadCredentialsException sin generar token")
    void invalidCredentialsShouldThrowAndNotGenerateToken() {
        assertThatThrownBy(() -> authService.login(new AuthRequestDto("admin", "wrong")))
                .isInstanceOf(BadCredentialsException.class);

        verify(jwtService, never()).generateToken(any());
    }
}
