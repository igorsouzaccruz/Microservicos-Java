package com.microservico.account.services;


import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.SignatureException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.core.io.Resource;
import org.springframework.test.util.ReflectionTestUtils;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.security.*;
import java.security.interfaces.RSAPublicKey;
import java.util.Base64;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Testes unitários para JwtService
 */
class JwtServiceTest {

    private JwtService jwtService;
    private Resource privateKeyResource;

    private PrivateKey privateKey;
    private PublicKey publicKey;

    @BeforeEach
    void setUp() throws Exception {
        // Gera um par de chaves RSA temporário para os testes
        KeyPairGenerator keyPairGen = KeyPairGenerator.getInstance("RSA");
        keyPairGen.initialize(2048);
        KeyPair keyPair = keyPairGen.generateKeyPair();
        privateKey = keyPair.getPrivate();
        publicKey = keyPair.getPublic();

        // Simula o resource com o conteúdo PEM da chave privada
        String pem = "-----BEGIN PRIVATE KEY-----\n"
                + Base64.getEncoder().encodeToString(privateKey.getEncoded())
                + "\n-----END PRIVATE KEY-----";

        privateKeyResource = mock(Resource.class);
        InputStream mockInputStream = new ByteArrayInputStream(pem.getBytes(StandardCharsets.UTF_8));
        when(privateKeyResource.getInputStream()).thenReturn(mockInputStream);

        // Instancia o JwtService e injeta os mocks
        jwtService = new JwtService();
        ReflectionTestUtils.setField(jwtService, "privateKeyResource", privateKeyResource);
        ReflectionTestUtils.setField(jwtService, "ttlSeconds", 3600L);
    }

    @Test
    @DisplayName("Deve gerar token JWT válido contendo userId, email e role")
    void shouldGenerateValidJwtToken() {
        // Act
        String token = jwtService.generateToken(1L, "igor@test.com", "ADMIN");

        // Assert
        assertNotNull(token);
        assertFalse(token.isBlank());

        // Decodifica e valida o token usando a chave pública
        Claims claims = Jwts.parser()
                .verifyWith((RSAPublicKey) publicKey)
                .build()
                .parseSignedClaims(token)
                .getPayload();

        assertEquals("1", claims.getSubject());
        assertEquals("igor@test.com", claims.get("email"));
        assertEquals("ADMIN", claims.get("role"));
        assertNotNull(claims.getExpiration());
    }

    @Test
    @DisplayName("Deve lançar IllegalStateException quando falhar ao carregar a chave privada")
    void shouldThrowIllegalStateException_WhenKeyLoadFails() throws IOException {
        // Arrange
        Resource faultyResource = mock(Resource.class);
        when(faultyResource.getInputStream()).thenThrow(new IOException("Falha de leitura"));
        ReflectionTestUtils.setField(jwtService, "privateKeyResource", faultyResource);


        // Act & Assert
        IllegalStateException exception = assertThrows(
                IllegalStateException.class,
                () -> jwtService.generateToken(1L, "teste@falha.com", "USER")
        );

        assertTrue(exception.getMessage().contains("Falha ao carregar RSA private key"));
    }

    @Test
    @Disabled("Nao finalizado")
    @DisplayName("Deve gerar tokens diferentes para chamadas subsequentes (nonce temporal)")
    void shouldGenerateDifferentTokensOnEachCall() {
        String token1 = jwtService.generateToken(1L, "user@test.com", "USER");
        String token2 = jwtService.generateToken(1L, "user@test.com", "USER");

        assertNotEquals(token1, token2);
    }

    @Test
    @DisplayName("Deve falhar ao validar o token com chave pública incorreta")
    void shouldFailToValidateTokenWithWrongPublicKey() throws Exception {
        String token = jwtService.generateToken(99L, "wrong@test.com", "ADMIN");

        // Gera um novo par de chaves diferentes
        KeyPairGenerator generator = KeyPairGenerator.getInstance("RSA");
        generator.initialize(2048);
        PublicKey wrongPublicKey = generator.generateKeyPair().getPublic();

        // Assert
        assertThrows(SignatureException.class, () -> {
            Jwts.parser()
                    .verifyWith((RSAPublicKey) wrongPublicKey)
                    .build()
                    .parseSignedClaims(token);
        });
    }
}