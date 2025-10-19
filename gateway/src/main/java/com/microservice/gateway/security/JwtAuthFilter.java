package com.microservice.gateway.security;

import io.jsonwebtoken.Claims;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;


public class JwtAuthFilter implements WebFilter {

    private final JwtValidator jwtValidator;

    public JwtAuthFilter(JwtValidator jwtValidator) {
        this.jwtValidator = jwtValidator;
    }

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {
        String path = exchange.getRequest().getURI().getPath();

        // ✅ Libera swagger e api-docs
        if (path.startsWith("/v3/api-docs") ||
                path.startsWith("/swagger-ui") ||
                path.contains("/api-docs") ||
                path.contains("/swagger-ui")) {
            return chain.filter(exchange);
        }

        String authHeader = exchange.getRequest().getHeaders().getFirst(HttpHeaders.AUTHORIZATION);

        // Se não houver token, apenas continue. As regras de segurança decidirão se é público ou não.
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return chain.filter(exchange);
        }

        String token = authHeader.substring(7);

        try {
            Claims claims = jwtValidator.parse(token);

            // Guarda os claims no exchange (útil para logs ou auditoria)
            exchange.getAttributes().put("jwtClaims", claims);

            // Recupera dados principais do token
            String username = claims.getSubject();
            Object roleClaim = claims.get("role");

            // Monta as autoridades conforme o formato do claim (string ou lista)
            List<SimpleGrantedAuthority> authorities;
            if (roleClaim instanceof String roleStr) {
                authorities = List.of(new SimpleGrantedAuthority(roleStr));
            } else if (roleClaim instanceof List<?> roleList) {
                authorities = roleList.stream()
                        .map(Object::toString)
                        .map(SimpleGrantedAuthority::new)
                        .collect(Collectors.toList());
            } else {
                authorities = Collections.emptyList();
            }

            Authentication authentication = new UsernamePasswordAuthenticationToken(
                    username,
                    null,
                    authorities
            );

            // Injeta o contexto de autenticação no fluxo reativo
            return chain.filter(exchange)
                    .contextWrite(ReactiveSecurityContextHolder.withAuthentication(authentication));

        } catch (Exception e) {
            // Token inválido, expirado ou assinatura incorreta
            exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
            return exchange.getResponse().setComplete();
        }
    }
}