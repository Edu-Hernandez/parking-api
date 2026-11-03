package com.edu.parking_api.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

// @Configuration: indica que esta clase contiene configuración de Spring.
// @Bean: registra el objeto devuelto por el método dentro del contenedor de Spring.
// PasswordEncoder: define el contrato para codificar y verificar contraseñas.
// BCryptPasswordEncoder: proporciona la implementación concreta.

@Configuration 
public class PasswordEncoderConfig {
    
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
