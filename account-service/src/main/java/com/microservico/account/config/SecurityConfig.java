package com.microservico.account.config;

import com.microservico.account.services.CustomUserDetailsService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.ProviderManager;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
public class SecurityConfig {

    /**
     * Encoder de senha padrão — usa BCrypt com custo 10.
     */
    @Bean
    public BCryptPasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    /**
     * Configura o AuthenticationManager com o CustomUserDetailsService.
     */
    @Bean
    public AuthenticationManager authenticationManager(
            CustomUserDetailsService userDetailsService,
            BCryptPasswordEncoder passwordEncoder) {

        DaoAuthenticationProvider provider = new DaoAuthenticationProvider();
        provider.setUserDetailsService(userDetailsService);
        provider.setPasswordEncoder(passwordEncoder);

        return new ProviderManager(provider);
    }

    /**
     * Define as regras de segurança da aplicação.
     */
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable()) // desabilita CSRF (necessário para APIs REST)
                .authorizeHttpRequests(auth -> auth
                        // ✅ Endpoints públicos (login, registro e Swagger)
                        .requestMatchers(
                                // 🔓 Endpoints públicos da API
                                "/api/accounts/login",
                                "/api/accounts/register",

                                // 🔓 Swagger sem prefixo
                                "/swagger-ui.html",
                                "/swagger-ui/**",
                                "/swagger-resources/**",
                                "/v3/api-docs/**",
                                "/webjars/**",

                                // 🔓 Swagger via Gateway (com prefixo /api/accounts/)
                                "/api/accounts/swagger-ui.html",
                                "/api/accounts/swagger-ui/**",
                                "/api/accounts/swagger-resources/**",
                                "/api/accounts/v3/api-docs/**",
                                "/api/accounts/webjars/**",

                                // 🔓 H2 Console (opcional)
                                "/h2-console/**"
                        ).permitAll()
                        .anyRequest().authenticated() // todos os outros exigem token
                )
                .headers(headers -> headers.frameOptions(frame -> frame.disable())) // permite H2 console
                .httpBasic(Customizer.withDefaults()); // útil para testes rápidos

        return http.build();
    }
}