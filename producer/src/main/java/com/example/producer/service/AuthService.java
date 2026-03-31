package com.example.producer.service;

import com.example.producer.config.AuthProperties;
import com.example.producer.dto.AuthRequestDto;
import com.example.producer.dto.AuthResponseDto;
import com.example.producer.security.JwtService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final AuthProperties authProperties;
    private final JwtService jwtService;

    /**
     * Valida credenciales contra el usuario configurado en variables de entorno
     * (AUTH_USERNAME / AUTH_PASSWORD) y devuelve un JWT firmado.
     *
     * Flujo de autenticación:
     * 1. El cliente envía { username, password } a POST /auth/login.
     * 2. Comparamos contra las credenciales del entorno (sin base de datos).
     * 3. Si coinciden → generamos un JWT con el username como "subject".
     * 4. El JWT contiene: sub, iat, exp y está firmado con HMAC-SHA256.
     * 5. El cliente adjunta ese token en cada request como:
     * Authorization: Bearer <token>
     * 6. JwtAuthenticationFilter verifica la firma y la expiración en cada request.
     * Si es válido → carga el usuario en el SecurityContext del hilo actual.
     * 7. Spring Security verifica que el SecurityContext tenga Authentication
     * antes de permitir acceso a endpoints protegidos.
     */
    public AuthResponseDto login(AuthRequestDto request) {
        if (!authProperties.getUsername().equals(request.getUsername())
                || !authProperties.getPassword().equals(request.getPassword())) {
            throw new BadCredentialsException("Invalid username or password");
        }
        String token = jwtService.generateToken(request.getUsername());
        return new AuthResponseDto(token, jwtService.getExpirationMs() / 1000);
    }
}
