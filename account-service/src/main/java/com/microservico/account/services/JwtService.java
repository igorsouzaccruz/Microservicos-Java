package com.microservico.account.services;

import io.jsonwebtoken.Jwts;
import org.springframework.core.io.Resource;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.security.KeyFactory;
import java.security.PrivateKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.time.Instant;
import java.util.Base64;
import java.util.Date;

@Service
public class JwtService {

    @Value("${jwt.private-key-path}")
    private Resource privateKeyResource;

    @Value("${jwt.ttl-seconds:3600}")
    private long ttlSeconds;

    private PrivateKey loadPrivateKey() {


        try (InputStream inputStream = privateKeyResource.getInputStream()) {
            System.out.println("📁 Tentando carregar chave privada de: " + privateKeyResource);
            // 1. Leia os bytes do *stream*
            byte[] keyBytes = inputStream.readAllBytes();

            // 2. Converta os bytes para String
            String pem = new String(keyBytes, StandardCharsets.UTF_8)
                    .replace("-----BEGIN PRIVATE KEY-----", "")
                    .replace("-----END PRIVATE KEY-----", "")
                    .replaceAll("\\s+", "");

            // 3. Decodifique o Base64
            var decodedKeyBytes = Base64.getDecoder().decode(pem);

            // 4. Gere a chave
            var keySpec = new PKCS8EncodedKeySpec(decodedKeyBytes);
            return KeyFactory.getInstance("RSA").generatePrivate(keySpec);

        } catch (Exception e) {
              throw new IllegalStateException("Falha ao carregar RSA private key", e);
        }
    }

    public String generateToken(Long userId, String email, String role) {
        var now = Instant.now();
        var exp = now.plusSeconds(ttlSeconds);

        return Jwts.builder()
                .subject(String.valueOf(userId))
                .claim("email", email)
                .claim("role", role)
                .issuedAt(Date.from(now))
                .expiration(Date.from(exp))
                .signWith(loadPrivateKey(), Jwts.SIG.RS256)
                .compact();
    }
}
