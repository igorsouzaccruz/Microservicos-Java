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
                                // ✅ 1. Endpoints públicos da API
                                "/api/accounts/login",
                                "/api/accounts/register",

                                // ✅ Página principal do Swagger
                                "/swagger-ui.html",

                                // ⬇️ A LINHA QUE FALTAVA ⬇️
                                // Libera os assets (CSS/JS) do Swagger
                                "/webjars/**", 

                                // ✅ Configs e docs do Swagger
                                "/swagger-ui/**",
                                "/v3/api-docs/**", 
                                "/api/{service}/v3/api-docs"
                        ).permitAll()
                        .anyExchange().authenticated()
                )
                .addFilterBefore(jwtAuthFilter, SecurityWebFiltersOrder.AUTHENTICATION)
                .build();
    }
}
