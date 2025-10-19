package com.microservico.sales.models.dtos;

import java.time.LocalDateTime;

public record SaleResponse(
        Long id,
        Long productId,
        Long userId,
        Integer quantity,
        LocalDateTime saleDate) {
}
