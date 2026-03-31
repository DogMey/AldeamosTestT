package com.example.producer.security;

import com.example.producer.config.JwtProperties;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("JwtService — generación y validación de tokens")
class JwtServiceTest {

    private JwtService jwtService;

    private static final String SECRET   = "aldeamosSuperSecretKeyForJWT_32CharsMin!";
    private static final String USERNAME = "admin";

    @BeforeEach
    void setUp() {
        JwtProperties props = new JwtProperties();
        props.setSecret(SECRET);
        props.setExpirationMs(86400000L);
        jwtService = new JwtService(props);
    }

    @Test
    @DisplayName("token generado debe ser válido y contener el username correcto")
    void generatedTokenShouldBeValidAndContainUsername() {
        String token = jwtService.generateToken(USERNAME);

        assertThat(jwtService.isTokenValid(token)).isTrue();
        assertThat(jwtService.extractUsername(token)).isEqualTo(USERNAME);
    }

    @Test
    @DisplayName("token expirado debe ser inválido")
    void expiredTokenShouldBeInvalid() {
        JwtProperties expiredProps = new JwtProperties();
        expiredProps.setSecret(SECRET);
        expiredProps.setExpirationMs(-1000L);
        String token = new JwtService(expiredProps).generateToken(USERNAME);

        assertThat(jwtService.isTokenValid(token)).isFalse();
    }

    @Test
    @DisplayName("token con firma alterada debe ser inválido")
    void tamperedTokenShouldBeInvalid() {
        String token = jwtService.generateToken(USERNAME);
        String tampered = token.substring(0, token.length() - 1) + "X";

        assertThat(jwtService.isTokenValid(tampered)).isFalse();
    }
}
