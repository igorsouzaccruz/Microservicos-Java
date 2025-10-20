package com.microservico.product.repositories;

import com.microservico.product.models.Product;
import com.microservico.product.models.enums.CategoryEnum;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class ProductRepositoryTest {

    @Autowired
    private ProductRepository repository;

    private Product product0;

    @BeforeEach
    public void setUp() {
        product0 = new Product(
                null,
                "Some product",
                CategoryEnum.PERIPHERALS,
                new BigDecimal("1.0"));
    }

    @DisplayName("Given Product Object When Save Should Return Saved Product")
    @Test
    void testGivenProductObject_WhenSave_ShouldReturnSavedProduct() {
        // Given / Arrange
        // When / Act
        Product productSaved = repository.save(product0);
        // Then / Assert
        assertNotNull(productSaved,
                () -> "Should have a product saved");
        assertTrue(productSaved.getId() > 0,
                () -> productSaved.getId() + "Should be great then zero 0");
    }

    @DisplayName("Given Product Object When Update Should Return Updated Product")
    @Test
    void testGivenProductObject_WhenUpdate_ShouldReturnUpdatedProduct() {
        // Given / Arrange
        repository.save(product0);

        Product productSaved = repository.findById(product0.getId()).orElse(null);
        assertNotNull(productSaved,
                () -> "Should have a product saved");

        productSaved.setPrice(new BigDecimal("99.0"));
        productSaved.setDescription("New Description");
        productSaved.setCategory(CategoryEnum.COMPUTERS);

        // When / Act
        Product updatedSaved = repository.save(productSaved);

        // Then / Assert
        assertNotNull(updatedSaved,
                () -> "Should have a updated product");
        assertEquals(new BigDecimal("99.0"), productSaved.getPrice());
        assertEquals("New Description", productSaved.getDescription());
        assertEquals(CategoryEnum.COMPUTERS, productSaved.getCategory());
    }

    @DisplayName("Given Product List When FindAll Then Return Product List")
    @Test
    void testGivenListProduct_WhenFindAll_thenReturnProductList() {
        // Given / Arrange

        Product product1 = new Product(
                null,
                "Some product",
                CategoryEnum.PERIPHERALS,
                new BigDecimal("1.0"));

        repository.saveAll(List.of(product0, product1));
        // When / Act
        List<Product> productList = repository.findAll();
        // Then / Assert
        assertNotNull(productList,
                () -> "Should find a list with two products");
        assertEquals(2, productList.size());
    }

    @DisplayName("Given Product Object When FindById Then Return Product Object")
    @Test
    void testGivenProductObject_WhenFindByID_thenReturnProductObject() {
        // Given / Arrange
        repository.save(product0);
        // When / Act
        Product productSaved = repository.findById(product0.getId()).orElse(null);
        // Then / Assert
        assertNotNull(productSaved,
                () -> "Should find a product saved");
        assertEquals(product0.getId(), productSaved.getId());
    }

    @DisplayName("Given Product Object When Delete Product Then Remove Product")
    @Test
    void testGivenProductObject_WhenDeleteProduct_thenRemoveProduct() {
        // Given / Arrange
        repository.save(product0);
        // When / Act
        repository.deleteById(product0.getId());

        Optional<Product> productOptional = repository.findById(product0.getId());
        // Then / Assert
        assertTrue(productOptional.isEmpty());
    }
}