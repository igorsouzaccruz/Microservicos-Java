package com.microservice.gateway.security;

import io.jsonwebtoken.Claims;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextImpl;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;

import java.nio.charset.StandardCharsets;
import java.util.List;



@Component
public class JwtAuthFilter implements WebFilter {

    private static final Logger log = LoggerFactory.getLogger(JwtAuthFilter.class);
    private final JwtValidator jwtValidator;

    public JwtAuthFilter(JwtValidator jwtValidator) {
        this.jwtValidator = jwtValidator;
    }


    @Override
    public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {
        var path = exchange.getRequest().getURI().getPath();

        if (path.startsWith("/api/account/login")
                || path.startsWith("/api/account/register")
                || path.startsWith("/swagger-ui")
                || path.startsWith("/v3/api-docs")) {
            return chain.filter(exchange);
        }

        var header = exchange.getRequest().getHeaders().getFirst("Authorization");
        if (header == null || !header.startsWith("Bearer ")) {
            return writeJsonResponse(exchange, HttpStatus.UNAUTHORIZED, "Token ausente ou inválido.");
        }

        var token = header.substring(7);

        try {
            Claims claims = jwtValidator.parse(token);
            String subject = claims.getSubject();

            Authentication auth = new UsernamePasswordAuthenticationToken(subject, null, List.of());
            SecurityContext context = new SecurityContextImpl(auth);

            return chain.filter(exchange)
                    .contextWrite(ReactiveSecurityContextHolder.withSecurityContext(Mono.just(context)));

        } catch (Exception e) {
            log.warn("Token inválido: {}", e.getMessage());
            return writeJsonResponse(exchange, HttpStatus.FORBIDDEN, "Token inválido ou expirado.");
        }
    }

    private Mono<Void> writeJsonResponse(ServerWebExchange exchange, HttpStatus status, String msg) {
        var response = exchange.getResponse();
        response.setStatusCode(status);
        response.getHeaders().setContentType(MediaType.APPLICATION_JSON);
        var json = """
                {"status": %d, "message": "%s"}""".formatted(status.value(), msg);
        var buffer = response.bufferFactory().wrap(json.getBytes(StandardCharsets.UTF_8));
        return response.writeWith(Mono.just(buffer));
    }
}
