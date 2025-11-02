package com.microservico.sales.config;

import com.microservico.sales.clients.ProductClient;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.context.annotation.Bean;

import static io.restassured.RestAssured.given;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest(
        webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
        properties = {
                "spring.cloud.openfeign.enabled=false",
                "eureka.client.enabled=false"
        }
)
class OpenAPIConfigTest {
    @LocalServerPort
    private int port;

    @TestConfiguration
    static class StubConfig {
        @Bean
        ProductClient productClient() {
            return id -> null; // implementação mínima apenas para satisfazer o contexto
        }
    }

    @Test
    @DisplayName("Should Display Swagger UI Page")
    void testShouldDisplaySwaggerUIPage() {
        var content = given()
                .basePath("swagger-ui/index.html")
                .port(port)
                .when()
                .get()
                .then()
                .statusCode(200)
                .extract()
                .body()
                .asString();

        assertTrue(content.contains("Swagger UI"));
    }
}