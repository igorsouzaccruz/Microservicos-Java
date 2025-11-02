package com.microservico.sales.clients;

import com.github.tomakehurst.wiremock.client.WireMock;
import com.microservico.sales.models.dtos.ProductResponse;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.contract.wiremock.AutoConfigureWireMock;
import org.springframework.cloud.openfeign.EnableFeignClients;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Teste de integração leve do FeignClient ProductClient,
 * utilizando WireMock para simular o serviço product-service.
 */
@SpringBootTest(
        classes = ProductClientTest.TestConfig.class,
        properties = {
                "feign.client.config.product-service.url=http://localhost:8089",
                "spring.main.web-application-type=none",
                "eureka.client.enabled=false"
        }
)
@AutoConfigureWireMock(port = 8089)
@EnableFeignClients(clients = ProductClient.class)
class ProductClientTest {

    @EnableAutoConfiguration
    static class TestConfig {
    }

    @Autowired
    private ProductClient productClient;

    @AfterEach
    void tearDown() {
        WireMock.reset();
    }

    @Test
    @DisplayName("Deve retornar ProductResponse quando o produto for encontrado (HTTP 200)")
    void shouldReturnProductResponse_WhenProductExists() {
        // Arrange
        WireMock.stubFor(WireMock.get("/products/1")
                .willReturn(WireMock.aResponse()
                        .withHeader("Content-Type", "application/json")
                        .withBody("""
                                { "id": 1, "name": "Teclado Mecânico", "price": 250.50 }
                                """)
                        .withStatus(200)));

        // Act
        ProductResponse response = productClient.getProductById(1L);

        // Assert
        assertThat(response).isNotNull();
        assertThat(response.id()).isEqualTo(1L);
        assertThat(response.name()).isEqualTo("Teclado Mecânico");
        assertThat(response.price()).isEqualTo(250.50);
    }

    @Test
    @DisplayName("Deve lançar exceção FeignException quando o produto não for encontrado (HTTP 404)")
    void shouldThrowFeignException_WhenProductNotFound() {
        // Arrange
        WireMock.stubFor(WireMock.get("/products/999")
                .willReturn(WireMock.aResponse()
                        .withStatus(404)));

        // Act & Assert
        try {
            productClient.getProductById(999L);
        } catch (Exception ex) {
            assertThat(ex)
                    .isInstanceOf(feign.FeignException.NotFound.class)
                    .hasMessageContaining("404");
        }
    }

    @Test
    @DisplayName("Deve lançar exceção genérica quando ocorrer erro no servidor (HTTP 500)")
    void shouldThrowException_WhenServerErrorOccurs() {
        // Arrange
        WireMock.stubFor(WireMock.get("/products/500")
                .willReturn(WireMock.aResponse()
                        .withStatus(500)
                        .withBody("Internal Server Error")));

        // Act & Assert
        try {
            productClient.getProductById(500L);
        } catch (Exception ex) {
            assertThat(ex.getMessage()).contains("Internal Server Error");
        }
    }
}