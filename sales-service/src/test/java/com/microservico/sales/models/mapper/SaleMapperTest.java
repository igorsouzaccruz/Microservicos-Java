package com.microservico.sales.models.mapper;

import com.microservico.sales.models.Sale;
import com.microservico.sales.models.dtos.SaleRequest;
import com.microservico.sales.models.dtos.SaleResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.jupiter.api.Assertions.*;

class SaleMapperTest {

    private SaleRequest request;
    private Sale sale;

    @BeforeEach
    void setup() {
        request = new SaleRequest();
        request.setProductId(10L);
        request.setUserId(5L);
        request.setQuantity(3);

        sale = new Sale();
        sale.setId(1L);
        sale.setProductId(10L);
        sale.setUserId(5L);
        sale.setQuantity(3);
        sale.setSaleDate(LocalDateTime.of(2025, 1, 10, 14, 30));
    }

    @DisplayName("Should map SaleRequest to Sale entity correctly")
    @Test
    void testToEntity_ShouldMapRequestToEntity() {
        Sale result = SaleMapper.toEntity(request);

        assertThat(result).isNotNull();
        assertThat(result.getProductId()).isEqualTo(10L);
        assertThat(result.getUserId()).isEqualTo(5L);
        assertThat(result.getQuantity()).isEqualTo(3);
    }

    @DisplayName("Should return null when mapping null SaleRequest to entity")
    @Test
    void testToEntity_WhenRequestIsNull_ShouldReturnNull() {
        Sale result = SaleMapper.toEntity(null);
        assertThat(result).isNull();
    }

    @DisplayName("Should map Sale entity to SaleResponse correctly")
    @Test
    void testToDto_ShouldMapEntityToResponse() {
        SaleResponse dto = SaleMapper.toDto(sale);

        assertThat(dto).isNotNull();
        assertThat(dto.id()).isEqualTo(1L);
        assertThat(dto.productId()).isEqualTo(10L);
        assertThat(dto.userId()).isEqualTo(5L);
        assertThat(dto.quantity()).isEqualTo(3);
        assertThat(dto.saleDate()).isEqualTo(LocalDateTime.of(2025, 1, 10, 14, 30));
    }

    @DisplayName("Should return null when mapping null Sale entity to DTO")
    @Test
    void testToDto_WhenEntityIsNull_ShouldReturnNull() {
        SaleResponse dto = SaleMapper.toDto(null);
        assertThat(dto).isNull();
    }

    @DisplayName("Should handle missing optional fields in SaleRequest correctly")
    @Test
    void testToEntity_WhenOptionalFieldsMissing_ShouldStillWork() {
        SaleRequest partial = new SaleRequest();
        partial.setProductId(null);
        partial.setUserId(null);
        partial.setQuantity(1);

        Sale result = SaleMapper.toEntity(partial);

        assertThat(result).isNotNull();
        assertThat(result.getProductId()).isNull();
        assertThat(result.getUserId()).isNull();
        assertThat(result.getQuantity()).isEqualTo(1);
    }

    @DisplayName("Should be consistent in bidirectional mapping between entity and DTO")
    @Test
    void testBidirectionalMapping_ShouldBeConsistent() {
        // Convert entity -> dto -> entity
        SaleResponse dto = SaleMapper.toDto(sale);
        Sale result = new Sale();
        result.setProductId(dto.productId());
        result.setUserId(dto.userId());
        result.setQuantity(dto.quantity());
        result.setSaleDate(dto.saleDate());

        assertThat(result.getProductId()).isEqualTo(sale.getProductId());
        assertThat(result.getUserId()).isEqualTo(sale.getUserId());
        assertThat(result.getQuantity()).isEqualTo(sale.getQuantity());
        assertThat(result.getSaleDate()).isEqualTo(sale.getSaleDate());
    }
}