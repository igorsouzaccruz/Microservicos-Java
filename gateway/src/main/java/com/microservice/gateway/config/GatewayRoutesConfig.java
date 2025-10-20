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
                .route("account-service", r -> r
                        .path("/api/accounts/**")
                        .filters(f -> f
                                .rewritePath("/api/(?<remaining>.*)", "/${remaining}")
                        )
                        .uri("lb://account-service")
                )
                .route("product-service", r -> r
                        .path("/api/products/**")
                        .filters(f -> f
                                .rewritePath("/api/(?<remaining>.*)", "/${remaining}")
                        )
                        .uri("lb://product-service")
                )
                .route("sales-service", r -> r
                        .path("/api/sales/**")
                        .filters(f -> f
                                .rewritePath("/api/(?<remaining>.*)", "/${remaining}")
                        )
                        .uri("lb://sales-service")
                )
                .build();
    }
}
