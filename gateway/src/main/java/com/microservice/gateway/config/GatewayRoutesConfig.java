package com.microservice.gateway.config;

import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class GatewayRoutesConfig {

    @Bean
    public RouteLocator customRouteLocator(RouteLocatorBuilder builder) {
        return builder.routes()
                // 🟢 Account Service
                .route("account-service", r -> r
                        .path("/api/account/**")
                        .filters(f -> f
                                // Remove o prefixo /api/account ao redirecionar
                                .rewritePath("/api/account/(?<path>.*)", "/api/account/${path}")
                                // Exemplo: adiciona header customizado
                                .addRequestHeader("X-Gateway", "SpringCloudGateway")
                        )
                        .uri("lb://account-service") // usa nome do serviço registrado no Eureka
                )

                // 🟣 Product Service
                .route("product-service", r -> r
                        .path("/api/product/**")
                        .filters(f -> f
                                .rewritePath("/api/product/(?<path>.*)", "/${path}")
                        )
                        .uri("lb://product-service")
                )

                // 🔵 Sales Service
                .route("sales-service", r -> r
                        .path("/api/sales/**")
                        .filters(f -> f
                                .rewritePath("/api/sales/(?<path>.*)", "/${path}")
                        )
                        .uri("lb://sales-service")
                )

                // 🧠 (Opcional) Rota de fallback genérica
                .route("fallback", r -> r
                        .path("/fallback")
                        .uri("forward:/fallback-handler")
                )
                .build();
    }
}
