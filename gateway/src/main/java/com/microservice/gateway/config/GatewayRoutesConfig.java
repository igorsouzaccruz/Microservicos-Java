package com.microservice.gateway.config;

import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class GatewayRoutesConfig {

    private static final String PATH_REGEX = "/api/(?<remaining>.*)";
    private static final String PATH_REPLACEMENT = "/${remaining}";

    @Bean
    public RouteLocator customRouteLocator(RouteLocatorBuilder builder) {
        return builder.routes()
                .route("account-service", r -> r
                        .path("/api/accounts/**")
                        .filters(f -> f
                                .rewritePath(PATH_REGEX, PATH_REPLACEMENT)
                        )
                        .uri("lb://account-service")
                )
                .route("product-service", r -> r
                        .path("/api/products/**")
                        .filters(f -> f
                                .rewritePath(PATH_REGEX, PATH_REPLACEMENT)
                        )
                        .uri("lb://product-service")
                )
                .route("sales-service", r -> r
                        .path("/api/sales/**")
                        .filters(f -> f
                                .rewritePath(PATH_REGEX, PATH_REPLACEMENT)
                        )
                        .uri("lb://sales-service")
                )
                .build();
    }
}
