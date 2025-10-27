package com.microservico.account.config;

import com.microservico.account.services.CustomUserDetailsService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.context.annotation.Import;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@Import(SecurityConfig.class)
class SecurityConfigTest {

    private SecurityConfig securityConfig;
    private CustomUserDetailsService userDetailsService;

    @BeforeEach
    void setUp() {
        userDetailsService = Mockito.mock(CustomUserDetailsService.class);
        securityConfig = new SecurityConfig(userDetailsService);
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
    @DisplayName("Deve obter AuthenticationManager do Spring")
    void shouldGetAuthenticationManager() throws Exception {
        AuthenticationManager managerMock = mock(AuthenticationManager.class);
        AuthenticationConfiguration configMock = mock(AuthenticationConfiguration.class);

        when(configMock.getAuthenticationManager()).thenReturn(managerMock);

        AuthenticationManager result = securityConfig.authenticationManager(configMock);

        assertNotNull(result);
        assertEquals(managerMock, result);
    }
}