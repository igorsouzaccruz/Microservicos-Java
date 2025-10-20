package com.microservico.account.models;


import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Account Entity Tests")
class AccountTest {

    private static Validator validator;

    @BeforeAll
    static void setupValidator() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    @Test
    @DisplayName("Should create Account with all fields set correctly")
    void shouldCreateAccountWithAllFields() {
        Account account = new Account(1L, "user@example.com", "123456", "Rua A, 123");

        assertEquals(1L, account.getId());
        assertEquals("user@example.com", account.getEmail());
        assertEquals("123456", account.getPassword());
        assertEquals("Rua A, 123", account.getAddress());
    }

    @Test
    @DisplayName("Should allow setting and getting properties")
    void shouldSetAndGetProperties() {
        Account account = new Account();
        account.setId(10L);
        account.setEmail("test@email.com");
        account.setPassword("pass123");
        account.setAddress("Av. Central 999");

        assertAll(
                () -> assertEquals(10L, account.getId()),
                () -> assertEquals("test@email.com", account.getEmail()),
                () -> assertEquals("pass123", account.getPassword()),
                () -> assertEquals("Av. Central 999", account.getAddress())
        );
    }

    @Test
    @DisplayName("Should detect validation errors when fields are null or blank")
    void shouldFailValidationWhenFieldsInvalid() {
        Account account = new Account();
        account.setEmail(" ");
        account.setPassword(null);
        account.setAddress("");

        Set<ConstraintViolation<Account>> violations = validator.validate(account);
        assertFalse(violations.isEmpty(), "Validation should fail for invalid fields");

        // opcional: verificar se há erros específicos
        assertTrue(violations.stream().anyMatch(v -> v.getPropertyPath().toString().equals("email")));
        assertTrue(violations.stream().anyMatch(v -> v.getPropertyPath().toString().equals("password")));
        assertTrue(violations.stream().anyMatch(v -> v.getPropertyPath().toString().equals("address")));
    }

    @Test
    @DisplayName("Should validate successfully when all fields are valid")
    void shouldPassValidationWhenFieldsValid() {
        Account account = new Account("valid@example.com", "securePass", "Rua Boa 45");

        Set<ConstraintViolation<Account>> violations = validator.validate(account);
        assertTrue(violations.isEmpty(), "Validation should pass for valid fields");
    }

    @Test
    @DisplayName("Equals and hashCode should depend only on id")
    void equalsAndHashCodeShouldUseIdOnly() {
        Account a1 = new Account(1L, "a@a.com", "pass", "addr");
        Account a2 = new Account(1L, "b@b.com", "diff", "other");

        assertEquals(a1, a2);
        assertEquals(a1.hashCode(), a2.hashCode());
    }

    @Test
    @DisplayName("Equals should return false for different ids")
    void equalsShouldReturnFalseForDifferentIds() {
        Account a1 = new Account(1L, "a@a.com", "pass", "addr");
        Account a2 = new Account(2L, "a@a.com", "pass", "addr");

        assertNotEquals(a1, a2);
    }

    @Test
    @DisplayName("toString should contain all key fields")
    void toStringShouldContainAllFields() {
        Account account = new Account(5L, "john@doe.com", "pass", "Av. Main 55");
        String str = account.toString();

        assertAll(
                () -> assertTrue(str.contains("john@doe.com")),
                () -> assertTrue(str.contains("Av. Main 55")),
                () -> assertTrue(str.contains("5"))
        );
    }
}