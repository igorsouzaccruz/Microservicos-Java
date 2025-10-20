package com.microservico.sales.clients;

import com.microservico.sales.exceptions.ResourceNotFoundException;
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
        productClient = new ProductClient(baseUrl, builder);
    }

    @Test
    @DisplayName("Deve retornar ProductResponse quando o produto for encontrado")
    void shouldReturnProductResponse_WhenProductExists() throws InterruptedException {
        // Arrange 
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

        var recorded = mockWebServer.takeRequest(1, TimeUnit.SECONDS);
        assertNotNull(recorded);
        assertEquals("GET", recorded.getMethod());
        assertEquals("/products/1", recorded.getPath());
    }

    @Test
    @DisplayName("Deve lançar ResourceNotFoundException quando o produto não for encontrado (404)")
    void shouldThrowResourceNotFoundException_WhenProductNotFound() {
        // Arrange 
        mockWebServer.enqueue(new MockResponse().setResponseCode(404));

        // Act & Assert
        ResourceNotFoundException thrown = assertThrows(
                ResourceNotFoundException.class,
                () -> productClient.getProductById(999L),
                "Deveria lançar ResourceNotFoundException para um produto inexistente"
        );
        assertEquals("ProductResponse not found with id: 999", thrown.getMessage());
    }
}