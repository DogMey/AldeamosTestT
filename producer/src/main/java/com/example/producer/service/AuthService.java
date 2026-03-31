package com.example.producer.service;

import com.example.producer.dto.AuthRequestDto;
import com.example.producer.dto.AuthResponseDto;

public interface AuthService {

    /**
     * Valida las credenciales recibidas y retorna un JWT firmado si son correctas.
     *
     * @param request contiene username y password
     * @return {@link AuthResponseDto} con el token y su tiempo de expiración
     * @throws org.springframework.security.authentication.BadCredentialsException si las credenciales son inválidas
     */
    AuthResponseDto login(AuthRequestDto request);
}
