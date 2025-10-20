package com.microservico.sales.repositories;

import com.microservico.sales.models.Sale;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class SaleRepositoryTest {

    @Autowired
    private SaleRepository repository;

    private Sale sale0;

    @BeforeEach
    void setUp() {
        sale0 = new Sale();
        sale0.setProductId(100L);
        sale0.setUserId(10L);
        sale0.setQuantity(3);
    }

    @DisplayName("Given Sale Object When Save Should Return Saved Sale")
    @Test
    void testGivenSaleObject_WhenSave_ShouldReturnSavedSale() {
        // Given / Arrange
        // When / Act
        Sale savedSale = repository.save(sale0);

        // Then / Assert
        assertNotNull(savedSale, "Should have saved a sale");
        assertTrue(savedSale.getId() > 0, "ID should be greater than zero");
        assertEquals(100L, savedSale.getProductId());
        assertEquals(10L, savedSale.getUserId());
        assertEquals(3, savedSale.getQuantity());
    }

    @DisplayName("Given Sale Object When Update Should Return Updated Sale")
    @Test
    void testGivenSaleObject_WhenUpdate_ShouldReturnUpdatedSale() {
        // Given
        repository.save(sale0);

        Sale foundSale = repository.findById(sale0.getId()).orElse(null);
        assertNotNull(foundSale, "Should find saved sale");

        foundSale.setQuantity(7);
        foundSale.setAtivo(false);

        // When
        Sale updated = repository.save(foundSale);

        // Then
        assertNotNull(updated);
        assertEquals(7, updated.getQuantity());
        assertFalse(updated.getAtivo());
    }

    @DisplayName("Given Sale List When FindAll Then Return List of Sales")
    @Test
    void testGivenSaleList_WhenFindAll_ShouldReturnListOfSales() {
        // Given
        Sale sale1 = new Sale();
        sale1.setProductId(200L);
        sale1.setUserId(20L);
        sale1.setQuantity(5);
        sale1.setAtivo(true);
        sale1.setSaleDate(LocalDateTime.now());

        repository.saveAll(List.of(sale0, sale1));

        // When
        List<Sale> list = repository.findAll();

        // Then
        assertNotNull(list);
        assertEquals(2, list.size());
    }

    @DisplayName("Given Sale Object When FindById Then Return Sale")
    @Test
    void testGivenSaleObject_WhenFindById_ShouldReturnSale() {
        // Given
        repository.save(sale0);

        // When
        Sale found = repository.findById(sale0.getId()).orElse(null);

        // Then
        assertNotNull(found);
        assertEquals(sale0.getId(), found.getId());
        assertEquals(sale0.getUserId(), found.getUserId());
    }

    @DisplayName("Given Sale Object When Delete Should Remove Sale")
    @Test
    void testGivenSaleObject_WhenDelete_ShouldRemoveSale() {
        // Given
        repository.save(sale0);

        // When
        repository.deleteById(sale0.getId());

        Optional<Sale> optional = repository.findById(sale0.getId());

        // Then
        assertTrue(optional.isEmpty(), "Sale should be removed");
    }

    @DisplayName("Given UserId When FindByUserId Then Return Sale List For That User")
    @Test
    void testGivenUserId_WhenFindByUserId_ShouldReturnSaleList() {
        // Given
        Sale sale1 = new Sale();
        sale1.setProductId(101L);
        sale1.setUserId(10L);
        sale1.setQuantity(2);
        sale1.setAtivo(true);
        sale1.setSaleDate(LocalDateTime.now());

        Sale sale2 = new Sale();
        sale2.setProductId(202L);
        sale2.setUserId(20L);
        sale2.setQuantity(4);
        sale2.setAtivo(true);
        sale2.setSaleDate(LocalDateTime.now());

        repository.saveAll(List.of(sale0, sale1, sale2));

        // When
        List<Sale> userSales = repository.findByUserId(10L);

        // Then
        assertNotNull(userSales);
        assertEquals(2, userSales.size(), "Should find two sales for userId 10");
        assertTrue(userSales.stream().allMatch(s -> s.getUserId().equals(10L)));
    }
}