package com.microservice.gateway.config;

import io.jsonwebtoken.Claims;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

@Component
public class LoggingGlobalFilter implements GlobalFilter, Ordered {

    private static final Logger log = LoggerFactory.getLogger(LoggingGlobalFilter.class);

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        String path = exchange.getRequest().getURI().getPath();
        String method = exchange.getRequest().getMethod().name();

        log.info("➡️ [{}] {}", method, path);

        Claims claims = exchange.getAttribute("jwtClaims");

        ServerHttpRequest.Builder mutatedRequest = exchange.getRequest().mutate();
        if (claims != null) {
            if (claims.get("email") != null)
                mutatedRequest.header("X-User-Email", claims.get("email").toString());

            if (claims.get("sub") != null)
                mutatedRequest.header("X-User-Id", claims.get("sub").toString());

            if (claims.get("role") != null)
                mutatedRequest.header("X-User-Role", claims.get("role").toString());
        }

        ServerWebExchange mutatedExchange = exchange.mutate()
                .request(mutatedRequest.build())
                .build();

        long start = System.currentTimeMillis();

        return chain.filter(mutatedExchange)
                .then(Mono.fromRunnable(() -> {
                    long time = System.currentTimeMillis() - start;
                    log.info("⬅️ [{} ms] Response {} {}", time,
                            exchange.getResponse().getStatusCode(), path);
                }));
    }

    @Override
    public int getOrder() {
        return 0;
    }
}