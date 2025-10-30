package com.microservico.sales.clients;

import com.microservico.sales.exceptions.ProductServiceCommunicationException;
import com.microservico.sales.exceptions.ResourceNotFoundException;
import com.microservico.sales.models.dtos.ProductResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;

@Service
public class ProductClient {

    private final WebClient webClient;

    @Autowired
    public ProductClient(WebClient.Builder builder) {
        this("lb://product-service", builder);
    }

    public ProductClient(String baseUrl, WebClient.Builder builder) {
        this.webClient = builder.baseUrl(baseUrl).build();
    }

    public ProductResponse getProductById(Long id) {
        try {
            return webClient.get()
                    .uri("/products/{id}", id)
                    .retrieve()
                    .bodyToMono(ProductResponse.class)
                    .block();
        } catch (WebClientResponseException.NotFound ex) {
            throw new ResourceNotFoundException(id, ProductResponse.class.getSimpleName());
        } catch (Exception e) {
            throw new ProductServiceCommunicationException("Failed to communicate with Product Service", e);
        }
    }
}