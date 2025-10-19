package com.microservice.gateway.config;

import com.microservice.gateway.security.JwtAuthFilter;
import com.microservice.gateway.security.JwtValidator;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.web.server.SecurityWebFiltersOrder;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.web.server.SecurityWebFilterChain;

@Configuration
public class SecurityConfig {

    private final JwtValidator jwtValidator;

    public SecurityConfig(JwtValidator jwtValidator) {
        this.jwtValidator = jwtValidator;
    }

    // 2. Crie o Bean do filtro aqui
    @Bean
    public JwtAuthFilter jwtAuthFilter() {
        return new JwtAuthFilter(jwtValidator);
    }

    @Bean
    public SecurityWebFilterChain securityWebFilterChain(ServerHttpSecurity http, JwtAuthFilter jwtAuthFilter) {
        return http
                .csrf(ServerHttpSecurity.CsrfSpec::disable)
                .authorizeExchange(ex -> ex
                        .pathMatchers(
                                "/api/accounts/login",
                                "/api/accounts/register",

                                // 🔓 Swagger UI e OpenAPI (com e sem prefixo /api/*)
                                "/swagger-ui.html",
                                "/swagger-ui/**",
                                "/v3/api-docs",
                                "/v3/api-docs/**",
                                "/webjars/**",

                                // ✅ Libera Swagger de cada microserviço (mantendo /api/... prefixo)
                                "/api/accounts/v3/api-docs",
                                "/api/accounts/swagger-ui/**",
                                "/api/accounts/webjars/**",

                                "/api/products/v3/api-docs",
                                "/api/products/swagger-ui/**",
                                "/api/products/webjars/**",

                                "/api/sales/v3/api-docs",
                                "/api/sales/swagger-ui/**",
                                "/api/sales/webjars/**"
                        ).permitAll()
                        .anyExchange().authenticated()
                )
                .addFilterBefore(jwtAuthFilter, SecurityWebFiltersOrder.AUTHENTICATION)
                .build();
    }
}
