package com.microservico.account.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenAPIConfig {

    @Bean
    OpenAPI customOpenAPI() {
        return new OpenAPI().info(new Info()
                .title("Account Microservice")
                .version("v1")
                .description("Account Service (Register and Login).")
                .termsOfService(""));
    }
}
