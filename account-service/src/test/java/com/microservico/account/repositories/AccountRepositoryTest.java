package com.microservico.account.repositories;

import com.microservico.account.models.Account;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.dao.DataIntegrityViolationException;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@DisplayName("AccountRepository Integration Tests")
class AccountRepositoryTest {

    @Autowired
    private AccountRepository accountRepository;

    @Test
    @DisplayName("Should save and retrieve an Account successfully")
    void shouldSaveAndRetrieveAccount() {
        Account account = new Account("user1@test.com", "pass123", "Rua A, 123");
        Account saved = accountRepository.save(account);

        assertNotNull(saved.getId(), "O ID deve ser gerado automaticamente");

        Optional<Account> found = accountRepository.findById(saved.getId());
        assertTrue(found.isPresent());
        assertEquals("user1@test.com", found.get().getEmail());
    }

    @Test
    @DisplayName("Should find Account by email")
    void shouldFindByEmail() {
        Account account = new Account("unique@test.com", "senha123", "Av. Brasil, 999");
        accountRepository.save(account);

        Optional<Account> found = accountRepository.findByEmail("unique@test.com");

        assertTrue(found.isPresent(), "Conta deve ser encontrada pelo e-mail");
        assertEquals(account.getEmail(), found.get().getEmail());
    }

    @Test
    @DisplayName("Should return empty Optional when email does not exist")
    void shouldReturnEmptyWhenEmailNotFound() {
        Optional<Account> found = accountRepository.findByEmail("naoexiste@teste.com");
        assertTrue(found.isEmpty());
    }

    @Test
    @DisplayName("Should update an existing Account")
    void shouldUpdateAccount() {
        Account account = new Account("old@mail.com", "1234", "Rua Velha 10");
        Account saved = accountRepository.save(account);

        saved.setEmail("new@mail.com");
        saved.setAddress("Rua Nova 11");
        Account updated = accountRepository.save(saved);

        assertEquals("new@mail.com", updated.getEmail());
        assertEquals("Rua Nova 11", updated.getAddress());
    }

    @Test
    @DisplayName("Should delete Account successfully")
    void shouldDeleteAccount() {
        Account account = new Account("delete@test.com", "pass", "Rua Z, 1");
        Account saved = accountRepository.save(account);

        accountRepository.delete(saved);
        Optional<Account> found = accountRepository.findById(saved.getId());

        assertTrue(found.isEmpty(), "Conta deve ser removida do banco");
    }

    @Test
    @DisplayName("Should not allow duplicate email due to unique constraint")
    void shouldNotAllowDuplicateEmail() {
        Account a1 = new Account("dup@test.com", "senha1", "Rua 1");
        Account a2 = new Account("dup@test.com", "senha2", "Rua 2");

        accountRepository.save(a1);
        assertThrows(DataIntegrityViolationException.class, () -> accountRepository.saveAndFlush(a2));
    }
}