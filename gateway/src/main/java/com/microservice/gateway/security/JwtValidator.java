package com.microservice.gateway.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.security.KeyFactory;
import java.security.PublicKey;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;

@Component
public class JwtValidator {

    @Value("${jwt.public-key-path}")
    private Resource publicKeyResource;

    private volatile PublicKey cachedKey;

    private PublicKey loadPublicKey() {
        if (cachedKey != null) return cachedKey;

        // ⬇️ LÓGICA ATUALIZADA AQUI ⬇️
        try (InputStream inputStream = publicKeyResource.getInputStream()) {
            // 1. Leia os bytes do *stream*
            byte[] keyBytes = inputStream.readAllBytes();

            // 2. Converta os bytes para String
            String pem = new String(keyBytes, StandardCharsets.UTF_8)
                    .replace("-----BEGIN PUBLIC KEY-----", "")
                    .replace("-----END PUBLIC KEY-----", "")
                    .replaceAll("\\s+", "");

            // 3. Decodifique o Base64
            var decodedKeyBytes = Base64.getDecoder().decode(pem);

            // 4. Gere a chave (X509EncodedKeySpec está correto para chaves públicas)
            var spec = new X509EncodedKeySpec(decodedKeyBytes);
            cachedKey = KeyFactory.getInstance("RSA").generatePublic(spec);
            return cachedKey;

        } catch (Exception e) {
            throw new IllegalStateException("Falha ao carregar chave pública RSA", e);
        }
    }

    public Claims parse(String token) {
        return Jwts.parser()
                .verifyWith(loadPublicKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }
}
