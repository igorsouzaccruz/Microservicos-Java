package com.microservice.gateway.security;

import io.jsonwebtoken.Claims;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import org.springframework.stereotype.Component;
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
        String authHeader = exchange.getRequest().getHeaders().getFirst(HttpHeaders.AUTHORIZATION);

        // 1. Se NÃO houver token, apenas continue a cadeia.
        // O filtro de Autorização (do SecurityConfig) decidirá se a rota é
        // pública (permitAll) ou se deve ser bloqueada (authenticated).
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return chain.filter(exchange);
        }

        try {
            String token = authHeader.substring(7);
            Claims claims = jwtValidator.parse(token);

            // 2. Coloque os claims no exchange (como você já fazia, útil para o LoggingGlobalFilter)
            exchange.getAttributes().put("jwtClaims", claims);

            // 3. (LÓGICA FALTANTE) Crie o objeto Authentication para o Spring Security
            String username = claims.getSubject(); // ou "email", dependendo do seu token

            // Ajuste "role" para o nome do claim que contém as roles/autoridades
            @SuppressWarnings("unchecked")
            List<String> roles = (List<String>) claims.get("role");

            List<SimpleGrantedAuthority> authorities = (roles != null) ?
                    roles.stream().map(SimpleGrantedAuthority::new).collect(Collectors.toList()) :
                    Collections.emptyList();

            Authentication authentication = new UsernamePasswordAuthenticationToken(username, null, authorities);

            // 4. Passe para o próximo filtro, mas com o Contexto de Segurança preenchido
            return chain.filter(exchange)
                    .contextWrite(ReactiveSecurityContextHolder.withAuthentication(authentication));

        } catch (Exception e) {
            // 5. Se o token for inválido (expirado, assinatura errada),
            // limpe o contexto e continue. O filtro de Autorização vai barrar.
            exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
            // Você pode querer retornar setComplete() se quiser barrar imediatamente
            // return exchange.getResponse().setComplete();

            // Ou apenas limpar o contexto e deixar o AuthorizationFilter decidir
            return chain.filter(exchange)
                    .contextWrite(ReactiveSecurityContextHolder.clearContext());
        }
    }
}