package com.microservico.sales.client;

import com.microservico.sales.models.dtos.ProductResponse;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

@Service
public class ProductClient {

    private final WebClient webClient;

    public ProductClient(WebClient.Builder builder) {
        this.webClient = builder.baseUrl("lb://product-service").build();
    }

    public ProductResponse getProductById(Long id) {
        return webClient.get()
                .uri("/products/{id}", id)
                .retrieve()
                .bodyToMono(ProductResponse.class)
                .block();
    }
}