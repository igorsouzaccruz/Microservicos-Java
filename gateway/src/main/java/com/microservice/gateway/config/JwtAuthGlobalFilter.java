package com.microservice.gateway.config;
import com.microservice.gateway.security.JwtValidator;
import io.jsonwebtoken.Claims;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;


@Component
public class JwtAuthGlobalFilter implements GlobalFilter, Ordered {

    private static final Logger log = LoggerFactory.getLogger(JwtAuthGlobalFilter.class);

    private final JwtValidator jwtValidator;

    public JwtAuthGlobalFilter(JwtValidator jwtValidator) {
        this.jwtValidator = jwtValidator;
    }

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        String path = exchange.getRequest().getURI().getPath();

        if (isPublicPath(path)) {
            return chain.filter(exchange);
        }

        String authHeader = exchange.getRequest().getHeaders().getFirst(HttpHeaders.AUTHORIZATION);
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            log.warn("🚫 Requisição sem token JWT - {}", path);
            exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
            return exchange.getResponse().setComplete();
        }

        String token = authHeader.substring(7);

        try {
            Claims claims = jwtValidator.parse(token);

            ServerHttpRequest mutatedRequest = exchange.getRequest().mutate()
                    .header("X-User-Id", claims.getSubject())
                    .header("X-User-Email", String.valueOf(claims.get("email")))
                    .header("X-User-Role", String.valueOf(claims.get("role")))
                    .build();

            ServerWebExchange mutatedExchange = exchange.mutate()
                    .request(mutatedRequest)
                    .build();

            mutatedExchange.getAttributes().put("jwtClaims", claims);

            log.debug("JWT válido para usuário: {}", claims.getSubject());
            return chain.filter(mutatedExchange);

        } catch (Exception e) {
            log.warn("Token JWT inválido: {}", e.getMessage());
            exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
            return exchange.getResponse().setComplete();
        }
    }

    private boolean isPublicPath(String path) {
        String normalized = path.toLowerCase().replaceAll("/+$", "");

        return normalized.startsWith("/v3/api-docs")
                || normalized.startsWith("/swagger-ui")
                || normalized.startsWith("/webjars")
                || normalized.equals("/api/accounts/login")
                || normalized.equals("/accounts/login")
                || normalized.equals("/api/accounts/register")
                || normalized.equals("/accounts/register")
                || normalized.startsWith("/actuator")
                || normalized.startsWith("/fallback");
    }

    @Override
    public int getOrder() {
        return -1;
    }
}