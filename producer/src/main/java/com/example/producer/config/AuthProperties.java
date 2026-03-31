package com.example.producer.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "auth")
public class AuthProperties {

    /** Usuario permitido para obtener un JWT (credencial única de prueba). */
    private String username;

    /**
     * Contraseña del usuario anterior. Cárgada por ahora desde variable de entorno.
     */
    private String password;

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}