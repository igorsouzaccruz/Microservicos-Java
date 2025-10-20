package com.microservico.product.models.mappers;

import com.microservico.product.models.Product;
import com.microservico.product.models.dtos.ProductDTO;
import com.microservico.product.models.enums.CategoryEnum;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.jupiter.api.Assertions.*;

class ProductMapperTest {

    private ProductMapper mapper;

    @BeforeEach
    void setUp() {
        mapper = new ProductMapper();
    }

    @DisplayName("Should map Product entity to ProductDTO correctly")
    @Test
    void testToDTO_ShouldMapEntityToDTO() {
        // Arrange
        Product product = new Product();
        product.setId(1L);
        product.setDescription("Mouse Gamer");
        product.setCategory(CategoryEnum.ACCESSORIES);
        product.setPrice(BigDecimal.valueOf(199.99));

        // Act
        ProductDTO dto = mapper.toDTO(product);

        // Assert
        assertThat(dto).isNotNull();
        assertThat(dto.id()).isEqualTo(1L);
        assertThat(dto.description()).isEqualTo("Mouse Gamer");
        assertThat(dto.category()).isEqualTo(CategoryEnum.ACCESSORIES);
        assertThat(dto.price()).isEqualByComparingTo("199.99");
    }

    @DisplayName("Should return null when mapping null entity to DTO")
    @Test
    void testToDTO_WhenEntityIsNull_ShouldReturnNull() {
        ProductDTO dto = mapper.toDTO(null);
        assertThat(dto).isNull();
    }

    @DisplayName("Should map ProductDTO to Product entity correctly")
    @Test
    void testToEntity_ShouldMapDTOToEntity() {
        // Arrange
        ProductDTO dto = new ProductDTO(
                2L,
                "Teclado Mecânico",
                CategoryEnum.ACCESSORIES,
                BigDecimal.valueOf(349.90)
        );

        // Act
        Product product = mapper.toEntity(dto);

        // Assert
        assertThat(product).isNotNull();
        assertThat(product.getId()).isEqualTo(2L);
        assertThat(product.getDescription()).isEqualTo("Teclado Mecânico");
        assertThat(product.getCategory()).isEqualTo(CategoryEnum.ACCESSORIES);
        assertThat(product.getPrice()).isEqualByComparingTo("349.90");
    }

    @DisplayName("Should map ProductDTO without ID to Product entity correctly (new product case)")
    @Test
    void testToEntity_WhenDTOHasNoId_ShouldMapWithoutId() {
        // Arrange
        ProductDTO dto = new ProductDTO(
                null,
                "Mouse sem fio",
                CategoryEnum.ACCESSORIES,
                BigDecimal.valueOf(129.90)
        );

        // Act
        Product product = mapper.toEntity(dto);

        // Assert
        assertThat(product).isNotNull();
        assertThat(product.getId()).isNull();
        assertThat(product.getDescription()).isEqualTo("Mouse sem fio");
        assertThat(product.getCategory()).isEqualTo(CategoryEnum.ACCESSORIES);
        assertThat(product.getPrice()).isEqualByComparingTo("129.90");
    }

    @DisplayName("Should return null when mapping null DTO to entity")
    @Test
    void testToEntity_WhenDTOIsNull_ShouldReturnNull() {
        Product product = mapper.toEntity(null);
        assertThat(product).isNull();
    }

    @DisplayName("Should handle consistency between entity and DTO after bidirectional mapping")
    @Test
    void testBidirectionalMapping_ShouldBeConsistent() {
        // Arrange
        Product original = new Product();
        original.setId(5L);
        original.setDescription("Monitor 4K");
        original.setCategory(CategoryEnum.SOFTWARE);
        original.setPrice(BigDecimal.valueOf(1299.99));

        // Act
        ProductDTO dto = mapper.toDTO(original);
        Product result = mapper.toEntity(dto);

        // Assert
        assertThat(result.getId()).isEqualTo(original.getId());
        assertThat(result.getDescription()).isEqualTo(original.getDescription());
        assertThat(result.getCategory()).isEqualTo(original.getCategory());
        assertThat(result.getPrice()).isEqualByComparingTo(original.getPrice());
    }

}