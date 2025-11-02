package com.microservice.gateway.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.core.io.Resource;
import org.springframework.test.util.ReflectionTestUtils;

import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.PublicKey;
import java.util.Base64;
import java.util.Date;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

class JwtValidatorTest {

    private JwtValidator jwtValidator;
    private Resource publicKeyResource;

    private KeyPair keyPair;

    @BeforeEach
    void setup() throws Exception {
        jwtValidator = new JwtValidator();
        publicKeyResource = Mockito.mock(Resource.class);

        // Gera um par RSA real (2048 bits)
        KeyPairGenerator keyGen = KeyPairGenerator.getInstance("RSA");
        keyGen.initialize(2048);
        keyPair = keyGen.generateKeyPair();

        // Monta PEM da chave pública
        String publicPem = "-----BEGIN PUBLIC KEY-----\n" +
                Base64.getEncoder().encodeToString(keyPair.getPublic().getEncoded()) +
                "\n-----END PUBLIC KEY-----";

        when(publicKeyResource.getInputStream())
                .thenReturn(new ByteArrayInputStream(publicPem.getBytes(StandardCharsets.UTF_8)));

        // Injeta o mock no JwtValidator
        ReflectionTestUtils.setField(jwtValidator, "publicKeyResource", publicKeyResource);
    }

    @Test
    @DisplayName("Deve carregar chave pública RSA com sucesso")
    void shouldLoadPublicKeySuccessfully() {
        PublicKey key = ReflectionTestUtils.invokeMethod(jwtValidator, "loadPublicKey");
        assertThat(key).isNotNull();
        assertThat(key.getAlgorithm()).isEqualTo("RSA");
    }

    @Test
    @DisplayName("Deve lançar exceção quando chave pública estiver inválida")
    void shouldThrowExceptionForInvalidKey() throws Exception {
        String invalidPem = "-----BEGIN PUBLIC KEY-----\nINVALIDKEYDATA\n-----END PUBLIC KEY-----";
        when(publicKeyResource.getInputStream())
                .thenReturn(new ByteArrayInputStream(invalidPem.getBytes(StandardCharsets.UTF_8)));

        JwtValidator brokenValidator = new JwtValidator();
        ReflectionTestUtils.setField(brokenValidator, "publicKeyResource", publicKeyResource);

        IllegalStateException ex = assertThrows(
                IllegalStateException.class,
                () -> ReflectionTestUtils.invokeMethod(brokenValidator, "loadPublicKey")
        );
        assertThat(ex.getMessage()).contains("Falha inesperada ao carregar chave pública RSA.");
    }

    @Test
    @DisplayName("Deve validar e decodificar JWT assinado corretamente")
    void shouldParseValidJwtToken() {
        ReflectionTestUtils.setField(jwtValidator, "publicKey", keyPair.getPublic());

        String token = Jwts.builder()
                .subject("user123")
                .claim("email", "user@test.com")
                .claim("role", "ADMIN")
                .issuedAt(new Date())
                .signWith(keyPair.getPrivate(), Jwts.SIG.RS256)
                .compact();

        Claims claims = jwtValidator.parse(token);

        assertThat(claims.getSubject()).isEqualTo("user123");
        assertThat(claims.get("email")).isEqualTo("user@test.com");
        assertThat(claims.get("role")).isEqualTo("ADMIN");
    }

    @Test
    @DisplayName("Deve lançar exceção ao tentar parsear token inválido")
    void shouldThrowExceptionWhenInvalidToken() {
        String invalidToken = "token.invalido.aqui";

        assertThrows(Exception.class, () -> jwtValidator.parse(invalidToken));
    }
}