package com.microservico.product.config;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;

import static io.restassured.RestAssured.given;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class OpenAPIConfigTest {
    @LocalServerPort
    private int port;

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