package com.microservico.sales.client;

import com.microservico.sales.models.dtos.ProductResponse;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import org.junit.jupiter.api.*;
import org.springframework.web.reactive.function.client.WebClient;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;

class ProductClientTest {

    private static MockWebServer mockWebServer;
    private ProductClient productClient;

    @BeforeAll
    static void setupServer() throws IOException {
        mockWebServer = new MockWebServer();
        mockWebServer.start();
    }

    @AfterAll
    static void shutdownServer() throws IOException {
        mockWebServer.shutdown();
    }

    @BeforeEach
    void setUp() {
        String baseUrl = mockWebServer.url("/").toString();

        WebClient.Builder builder = WebClient.builder();
        productClient = new ProductClient(builder, baseUrl);
    }

    @Test
    @DisplayName("Deve retornar ProductResponse quando o produto for encontrado")
    void shouldReturnProductResponse_WhenProductExists() throws InterruptedException {
        // Arrange — Simula resposta JSON do product-service
        String json = """
                {
                    "id": 1,
                    "name": "Teclado Mecânico",
                    "price": 250.50
                }
                """;

        mockWebServer.enqueue(new MockResponse()
                .setBody(json)
                .addHeader("Content-Type", "application/json")
                .setResponseCode(200));

        // Act
        ProductResponse response = productClient.getProductById(1L);

        // Assert
        assertNotNull(response);
        assertEquals(1L, response.id());
        assertEquals("Teclado Mecânico", response.name());
        assertEquals(250.50, response.price());

        // Verifica se chamada HTTP foi feita corretamente
        var recorded = mockWebServer.takeRequest(1, TimeUnit.SECONDS);
        assertNotNull(recorded);
        assertEquals("GET", recorded.getMethod());
        assertEquals("/products/1", recorded.getPath());
    }

    @Test
    @DisplayName("Deve retornar null quando o produto não for encontrado (404)")
    void shouldReturnNull_WhenProductNotFound() {
        mockWebServer.enqueue(new MockResponse().setResponseCode(404));

        ProductResponse response = productClient.getProductById(999L);

        assertNull(response, "Quando o produto não existe, deve retornar null");
    }
}