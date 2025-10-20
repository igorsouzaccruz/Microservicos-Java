package com.microservico.sales.models;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SaleTest {
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
    void testSale_WhenAllFieldsAreCorrect_ShouldBeValid() {
        // Given / Arrange
        Sale sale = new Sale(1L, 2L, 3);

        // When / Act
        Set<ConstraintViolation<Sale>> violations = validator.validate(sale);

        // Then / Assert
        assertTrue(violations.isEmpty(), () -> "Shouldn't have violations: \n" + violations);
    }


    @DisplayName("Should not be valid when productId is null or not positive")
    @Test
    void testSale_WhenProductIdInvalid_ShouldNotBeValid() {
        Sale sale = new Sale();
        sale.setProductId(null);
        sale.setUserId(1L);
        sale.setQuantity(2);

        Set<ConstraintViolation<Sale>> violations = validator.validate(sale);
        assertThat(violations).hasSize(1);
        assertThat(violations.iterator().next().getPropertyPath().toString()).isEqualTo("productId");

        sale.setProductId(-10L);
        violations = validator.validate(sale);
        assertThat(violations).hasSize(1);
    }

    @DisplayName("Should not be valid when userId is null or not positive")
    @Test
    void testSale_WhenUserIdInvalid_ShouldNotBeValid() {
        Sale sale = new Sale();
        sale.setProductId(1L);
        sale.setUserId(null);
        sale.setQuantity(2);

        Set<ConstraintViolation<Sale>> violations = validator.validate(sale);
        assertThat(violations).hasSize(1);
        assertThat(violations.iterator().next().getPropertyPath().toString()).isEqualTo("userId");

        sale.setUserId(-5L);
        violations = validator.validate(sale);
        assertThat(violations).hasSize(1);
    }

    @DisplayName("Should not be valid when quantity is null or not positive")
    @Test
    void testSale_WhenQuantityInvalid_ShouldNotBeValid() {
        Sale sale = new Sale();
        sale.setProductId(1L);
        sale.setUserId(2L);
        sale.setQuantity(null);

        Set<ConstraintViolation<Sale>> violations = validator.validate(sale);
        assertThat(violations).hasSize(1);
        assertThat(violations.iterator().next().getPropertyPath().toString()).isEqualTo("quantity");

        sale.setQuantity(0);
        violations = validator.validate(sale);
        assertThat(violations).hasSize(1);
    }

    @DisplayName("Should initialize ativo=true and saleDate when using default constructor")
    @Test
    void testSale_DefaultConstructor_ShouldInitializeFields() {
        Sale sale = new Sale();
        assertThat(sale.getAtivo()).isTrue();
        assertThat(sale.getSaleDate()).isNotNull();
    }

    @DisplayName("Should initialize ativo=true and saleDate when using parameterized constructor")
    @Test
    void testSale_ParameterizedConstructor_ShouldInitializeFields() {
        Sale sale = new Sale(1L, 2L, 3);
        assertThat(sale.getAtivo()).isTrue();
        assertThat(sale.getSaleDate()).isNotNull();
    }

    @DisplayName("Should initialize ativo=true and saleDate when using parameterized constructor with id")
    @Test
    void testSale_ParameterizedConstructorWithId_ShouldInitializeFields() {
        Sale sale = new Sale(1L, 1L, 2L, 3);
        assertThat(sale.getAtivo()).isTrue();
        assertThat(sale.getSaleDate()).isNotNull();
    }

    @DisplayName("Should set ativo=true and saleDate when prePersist is called")
    @Test
    void testSale_PrePersist_ShouldSetDefaultValues() {
        Sale sale = new Sale();
        sale.setAtivo(null);
        sale.setSaleDate(null);

        sale.prePersist();

        assertThat(sale.getAtivo()).isTrue();
        assertThat(sale.getSaleDate()).isNotNull();
    }

    @DisplayName("Should correctly implement equals and hashCode")
    @Test
    void testSale_EqualsAndHashCode() {
        Sale sale1 = new Sale();
        sale1.setId(1L);

        Sale sale2 = new Sale();
        sale2.setId(1L);

        Sale sale3 = new Sale();
        sale3.setId(2L);

        assertThat(sale1).isEqualTo(sale2);
        assertThat(sale1.hashCode()).isEqualTo(sale2.hashCode());
        assertThat(sale1).isNotEqualTo(sale3);
    }

    @DisplayName("Should return readable string in toString")
    @Test
    void testSale_ToString_ShouldContainFieldNames() {
        Sale sale = new Sale(1L, 2L, 3);
        String str = sale.toString();
        assertThat(str).contains("Sale{", "productId", "userId", "quantity", "ativo", "saleDate");
    }

}