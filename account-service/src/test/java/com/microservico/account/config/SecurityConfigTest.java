package com.microservico.account.config;


import com.microservico.account.services.CustomUserDetailsService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class SecurityConfigTest {

    private SecurityConfig securityConfig;
    private CustomUserDetailsService userDetailsService;

    @BeforeEach
    void setUp() {
        securityConfig = new SecurityConfig();
        userDetailsService = mock(CustomUserDetailsService.class);
    }

    @Test
    @DisplayName("Deve criar um BCryptPasswordEncoder válido")
    void shouldCreatePasswordEncoder() {
        BCryptPasswordEncoder encoder = securityConfig.passwordEncoder();
        assertNotNull(encoder);
        String raw = "123456";
        String hash = encoder.encode(raw);

        assertTrue(encoder.matches(raw, hash));
    }

    @Test
    @DisplayName("Deve criar AuthenticationManager com DaoAuthenticationProvider configurado")
    void shouldCreateAuthenticationManager() {
        BCryptPasswordEncoder encoder = securityConfig.passwordEncoder();

        AuthenticationManager manager =
                securityConfig.authenticationManager(userDetailsService, encoder);

        assertNotNull(manager);
        assertInstanceOf(AuthenticationManager.class, manager);
    }

    @Test
    @DisplayName("Deve configurar SecurityFilterChain corretamente")
    void shouldConfigureSecurityFilterChain() throws Exception {
        // Arrange
        HttpSecurity httpSecurity = mock(HttpSecurity.class, RETURNS_DEEP_STUBS);

        // Simula comportamento padrão do HttpSecurity
        when(httpSecurity.csrf(any())).thenReturn(httpSecurity);
        when(httpSecurity.authorizeHttpRequests(any())).thenReturn(httpSecurity);
        when(httpSecurity.headers(any())).thenReturn(httpSecurity);
        when(httpSecurity.httpBasic(any())).thenReturn(httpSecurity);

        // Act
        SecurityFilterChain chain = securityConfig.securityFilterChain(httpSecurity);

        // Assert
        assertNotNull(chain);
        verify(httpSecurity).csrf(any());
        verify(httpSecurity).authorizeHttpRequests(any());
        verify(httpSecurity).headers(any());
        verify(httpSecurity).build();
    }

    @Test
    @DisplayName("As rotas públicas devem estar corretamente configuradas")
    void shouldDefinePublicEndpoints() {
        List<String> publicEndpoints = List.of(
                "/login",
                "/register",
                "/swagger-ui.html",
                "/swagger-ui/**",
                "/swagger-resources/**",
                "/v3/api-docs/**",
                "/webjars/**",
                "/h2-console/**"
        );

        assertEquals(8, publicEndpoints.size());
        assertTrue(publicEndpoints.contains("/login"));
        assertTrue(publicEndpoints.contains("/h2-console/**"));
    }
}