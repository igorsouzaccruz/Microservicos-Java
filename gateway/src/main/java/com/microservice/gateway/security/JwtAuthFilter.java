package com.microservice.gateway.security;

import io.jsonwebtoken.Claims;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;

public class JwtAuthFilter implements WebFilter {

    private static final Logger log = LoggerFactory.getLogger(JwtAuthFilter.class);
    private final JwtValidator jwtValidator;

    public JwtAuthFilter(JwtValidator jwtValidator) {
        this.jwtValidator = jwtValidator;
    }

    @Override
    @SuppressWarnings("NullableProblems")
    public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {
        String path = exchange.getRequest().getURI().getPath();

        // Permitir rotas públicas
        if (isPublicPath(path)) {
            log.debug("🟢 Rota pública detectada: {}", path);
            return chain.filter(exchange);
        }

        String token = extractToken(exchange);
        if (token == null) {
            log.warn("⚠ Requisição sem token JWT para {}", path);
            return handleUnauthorized(exchange);
        }

        try {
            Claims claims = jwtValidator.parse(token);
            exchange.getAttributes().put("jwtClaims", claims);
            log.debug(" JWT válido para usuário: {}", claims.getSubject());
            return chain.filter(exchange);
        } catch (Exception e) {
            log.warn(" Falha ao validar token JWT em {}: {}", path, e.getMessage());
            return handleUnauthorized(exchange);
        }
    }

    private boolean isPublicPath(String path) {
        String normalized = path.toLowerCase().replaceAll("/+$", "");
        return normalized.startsWith("/v3/api-docs")
                || normalized.startsWith("/swagger-ui")
                || normalized.equals("/api/accounts/login")
                || normalized.equals("/api/accounts/register")
                || normalized.startsWith("/actuator")
                || normalized.startsWith("/fallback");
    }

    private String extractToken(ServerWebExchange exchange) {
        String authHeader = exchange.getRequest().getHeaders().getFirst(HttpHeaders.AUTHORIZATION);
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return null;
        }
        return authHeader.substring(7);
    }

    private Mono<Void> handleUnauthorized(ServerWebExchange exchange) {
        exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
        return exchange.getResponse().setComplete();
    }
}