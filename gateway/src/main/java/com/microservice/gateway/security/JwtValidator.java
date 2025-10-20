package com.microservice.gateway.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.security.KeyFactory;
import java.security.PublicKey;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;

@Component
public class JwtValidator {

    private static final Logger log = LoggerFactory.getLogger(JwtValidator.class);

    @Value("${jwt.public-key-path}")
    private Resource publicKeyResource;

    private PublicKey publicKey;

    @PostConstruct
    public void init() {
        this.publicKey = loadPublicKey();
        log.info("Chave pública RSA carregada com sucesso para validação de tokens");
    }

    private PublicKey loadPublicKey() {
        try (InputStream inputStream = publicKeyResource.getInputStream()) {
            byte[] keyBytes = inputStream.readAllBytes();

            String pem = new String(keyBytes, StandardCharsets.UTF_8)
                    .replace("-----BEGIN PUBLIC KEY-----", "")
                    .replace("-----END PUBLIC KEY-----", "")
                    .replaceAll("\\s+", "");

            byte[] decodedKey = Base64.getDecoder().decode(pem);
            var keySpec = new X509EncodedKeySpec(decodedKey);
            return KeyFactory.getInstance("RSA").generatePublic(keySpec);

        } catch (Exception e) {
            log.error(" Falha ao carregar chave pública RSA", e);
            throw new IllegalStateException("Falha ao carregar chave pública RSA", e);
        }
    }

    public Claims parse(String token) {
        return Jwts.parser()
                .verifyWith(publicKey)
                .clockSkewSeconds(30)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }
}