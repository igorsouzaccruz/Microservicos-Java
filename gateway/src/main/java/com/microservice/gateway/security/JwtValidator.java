package com.microservice.gateway.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;

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

        try {
            String pem = new String(Files.readAllBytes(publicKeyResource.getFile().toPath()), StandardCharsets.UTF_8)
                    .replace("-----BEGIN PUBLIC KEY-----", "")
                    .replace("-----END PUBLIC KEY-----", "")
                    .replaceAll("\\s+", "");

            var keyBytes = Base64.getDecoder().decode(pem);
            var spec = new X509EncodedKeySpec(keyBytes);
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
