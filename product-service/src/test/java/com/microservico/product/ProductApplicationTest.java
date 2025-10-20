package com.microservico.product;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

@SpringBootTest
class ProductApplicationTest {

    @DisplayName("Application context should load successfully")
    @Test
    void contextLoads() {
        assertDoesNotThrow(() -> {
        });
    }

    @DisplayName("Main method should run without throwing exceptions")
    @Test
    void testMainMethod() {
        assertDoesNotThrow(() -> ProductApplication.main(new String[]{}));
    }
}