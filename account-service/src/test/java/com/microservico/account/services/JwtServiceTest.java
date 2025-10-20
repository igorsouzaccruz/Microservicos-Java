package com.microservico.account.services;


import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.io.Resource;
import org.springframework.test.util.ReflectionTestUtils;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.PublicKey;
import java.security.interfaces.RSAPublicKey;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class JwtServiceTest {

    @Mock
    private Resource privateKeyResource;

    private JwtService jwtService;
    private PublicKey publicKey;

    @BeforeEach
    void setUp() {
        jwtService = new JwtService();
        ReflectionTestUtils.setField(jwtService, "ttlSeconds", 3600L);
    }

    private void setupPathPrivateKeyMock() throws Exception {
        KeyPairGenerator keyPairGen = KeyPairGenerator.getInstance("RSA");
        keyPairGen.initialize(2048);
        KeyPair keyPair = keyPairGen.generateKeyPair();
        publicKey = keyPair.getPublic();

        String pem = "-----BEGIN PRIVATE KEY-----\n"
                + java.util.Base64.getEncoder().encodeToString(keyPair.getPrivate().getEncoded())
                + "\n-----END PRIVATE KEY-----";

        when(privateKeyResource.getInputStream()).thenAnswer(invocation ->
                new ByteArrayInputStream(pem.getBytes(StandardCharsets.UTF_8))
        );

        ReflectionTestUtils.setField(jwtService, "privateKeyResource", privateKeyResource);

        jwtService.init();
    }

    @Test
    @DisplayName("Deve gerar token JWT válido contendo userId, email e role")
    void shouldGenerateValidJwtToken() throws Exception {
        // Arrange
        setupPathPrivateKeyMock();

        // Act
        String token = jwtService.generateToken(1L, "igor@test.com", "ADMIN");

        // Assert
        assertNotNull(token);
        Claims claims = Jwts.parser()
                .verifyWith(publicKey)
                .build()
                .parseSignedClaims(token)
                .getPayload();

        assertEquals("1", claims.getSubject());
        assertEquals("igor@test.com", claims.get("email"));
        assertEquals("ADMIN", claims.get("role"));
    }

    @Test
    @DisplayName("Deve gerar tokens diferentes para chamadas subsequentes")
    void shouldGenerateDifferentTokensOnEachCall() throws Exception {
        // Arrange
        setupPathPrivateKeyMock();

        // Act
        String token1 = jwtService.generateToken(1L, "user@test.com", "USER");
        Thread.sleep(1000);
        String token2 = jwtService.generateToken(1L, "user@test.com", "USER");

        // Assert
        assertNotNull(token1);
        assertNotNull(token2);
        assertNotEquals(token1, token2);
    }

    @Test
    @DisplayName("Deve lançar IllegalStateException quando falhar ao carregar a chave privada")
    void shouldThrowIllegalStateException_WhenKeyLoadFails() throws IOException {
        // Arrange
        Resource faultyResource = mock(Resource.class);
        when(faultyResource.getInputStream()).thenThrow(new IOException("Falha de leitura simulada"));

        ReflectionTestUtils.setField(jwtService, "privateKeyResource", faultyResource);

        // Act & Assert
        IllegalStateException exception = assertThrows(
                IllegalStateException.class,
                () -> jwtService.init()
        );

        assertTrue(exception.getMessage().contains("Falha ao carregar RSA private key"));
    }
}