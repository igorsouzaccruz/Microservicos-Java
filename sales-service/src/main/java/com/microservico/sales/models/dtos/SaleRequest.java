package com.microservico.sales.models.dtos;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public class SaleRequest {

    @NotNull
    @Positive
    private Long productId;

    private Long userId;

    @NotNull
    private Integer quantity;

    public SaleRequest() {
    }

    public SaleRequest(Long productId, Long userId, Integer quantity) {
        this.productId = productId;
        this.userId = userId;
        this.quantity = quantity;
    }

    public Long getProductId() {
        return productId;
    }

    public void setProductId(Long productId) {
        this.productId = productId;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public Integer getQuantity() {
        return quantity;
    }

    public void setQuantity(Integer quantity) {
        this.quantity = quantity;
    }
}
