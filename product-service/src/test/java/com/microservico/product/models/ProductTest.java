package com.microservico.product.models;

import com.microservico.product.models.enums.CategoryEnum;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.Set;

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ProductTest {
    private static ValidatorFactory factory;
    private static Validator validator;

    @BeforeAll
    static void setUp() {
        factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    @AfterAll
    static void tearDown() {
        if (factory != null) {
            factory.close();
        }
    }

    @DisplayName("Should must create a validated product when all fields are correct")
    @Test
    void testProduct_WhenAllFieldsAreCorrect_ShouldBeValid() {
        // Given / Arrange
        Product product = new Product();
        product.setDescription("Teclado Mecânico RGB");
        product.setCategory(CategoryEnum.PERIPHERALS);
        product.setPrice(new BigDecimal("350.99"));

        // When / Act
        Set<ConstraintViolation<Product>> violations = validator.validate(product);

        // Then / Assert
        assertTrue(violations.isEmpty(), () -> "Shouldn't have violations: \n" + violations);
    }

    @DisplayName("Should not be valid when the description is invalid")
    @Test
    void testProduct_WhenDescriptionIsInvalid_ShouldNotBeValid() {
        // Arrange
        Product product = new Product();
        product.setCategory(CategoryEnum.ACCESSORIES);
        product.setPrice(BigDecimal.TEN);

        //(@NotNull e @NotBlank)
        product.setDescription(null);
        Set<ConstraintViolation<Product>> violations = validator.validate(product);
        assertThat(violations).hasSize(2);

        //(@NotBlank)
        product.setDescription("   ");
        violations = validator.validate(product);
        assertThat(violations).hasSize(1);
        assertThat(violations.iterator().next().getPropertyPath().toString()).hasToString("description");

        //(@Length)
        String longDescription = "a".repeat(151);
        product.setDescription(longDescription);
        violations = validator.validate(product);
        assertThat(violations).hasSize(1);
        assertThat(violations.iterator().next().getPropertyPath().toString()).hasToString("description");
    }

    @DisplayName("Should not be valid when category is null")
    @Test
    void testProduct_WhenCategoryIsNull_ShouldNotBeValid() {
        // Arrange
        Product product = new Product();
        product.setDescription("Mouse Gamer");
        product.setPrice(new BigDecimal("120.00"));
        product.setCategory(null);

        // Act
        Set<ConstraintViolation<Product>> violations = validator.validate(product);

        // Assert
        assertThat(violations).hasSize(1);
        assertThat(violations.iterator().next().getPropertyPath().toString()).hasToString("category");
    }

    @DisplayName("Should not be performed when the price is invalid")
    @Test
    void testProduct_WhenPriceIsInvalid_ShouldNotBeValid() {
        // Arrange
        Product product = new Product();
        product.setDescription("Monitor Ultrawide");
        product.setCategory(CategoryEnum.SOFTWARE);

        //(@NotNull)
        product.setPrice(null);
        Set<ConstraintViolation<Product>> violations = validator.validate(product);
        assertThat(violations).hasSize(1);
        assertThat(violations.iterator().next().getPropertyPath().toString()).hasToString("price");

        //(@Positive)
        product.setPrice(BigDecimal.ZERO);
        violations = validator.validate(product);
        assertThat(violations).hasSize(1);
        assertThat(violations.iterator().next().getPropertyPath().toString()).hasToString("price");

        //(@Positive)
        product.setPrice(new BigDecimal("-0.01"));
        violations = validator.validate(product);
        assertThat(violations).hasSize(1);
        assertThat(violations.iterator().next().getPropertyPath().toString()).hasToString("price");
    }
}