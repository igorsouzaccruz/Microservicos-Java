package com.microservico.account.services;

import com.microservico.account.models.Account;
import com.microservico.account.repositories.AccountRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AccountServiceTest {

    @Mock
    private AccountRepository repository;

    @InjectMocks
    private AccountService accountService;

    private Account account;

    @BeforeEach
    void setUp() {
        account = new Account(1L, "test@example.com", "senha123", "Rua Teste, 123");
    }

    @Test
    @DisplayName("Deve salvar e retornar uma conta com sucesso")
    void whenSaveAccount_shouldReturnSavedAccount() {
        // Arrange
        when(repository.save(account)).thenReturn(account);

        // Act
        Account savedAccount = accountService.save(account);

        // Assert
        assertThat(savedAccount).isNotNull();
        assertThat(savedAccount.getEmail()).isEqualTo("test@example.com");

        verify(repository, times(1)).save(account);
    }

    @Test
    @DisplayName("Deve encontrar uma conta pelo email quando ela existir")
    void whenFindByEmail_withExistingEmail_shouldReturnAccount() {
        // Arrange
        String email = "test@example.com";

        when(repository.findByEmail(email)).thenReturn(Optional.of(account));

        // Act
        Optional<Account> foundAccount = accountService.findByEmail(email);

        // Assert
        assertThat(foundAccount).isPresent().contains(account);
        verify(repository, times(1)).findByEmail(email);
    }

    @Test
    @DisplayName("Deve retornar Optional vazio ao buscar por email inexistente")
    void whenFindByEmail_withNonExistingEmail_shouldReturnEmpty() {
        // Arrange
        String email = "notfound@example.com";

        when(repository.findByEmail(email)).thenReturn(Optional.empty());

        // Act
        Optional<Account> foundAccount = accountService.findByEmail(email);

        // Assert
        assertThat(foundAccount).isNotPresent();
        verify(repository, times(1)).findByEmail(email);
    }

    @Test
    @DisplayName("Deve retornar true quando o email existir")
    void whenExistsByEmail_withExistingEmail_shouldReturnTrue() {
        // Arrange
        String email = "test@example.com";

        when(repository.findByEmail(email)).thenReturn(Optional.of(account));

        // Act
        boolean exists = accountService.existsByEmail(email);

        // Assert
        assertThat(exists).isTrue();
        verify(repository, times(1)).findByEmail(email);
    }

    @Test
    @DisplayName("Deve retornar false quando o email não existir")
    void whenExistsByEmail_withNonExistingEmail_shouldReturnFalse() {
        // Arrange
        String email = "notfound@example.com";
        when(repository.findByEmail(email)).thenReturn(Optional.empty());

        // Act
        boolean exists = accountService.existsByEmail(email);

        // Assert
        assertThat(exists).isFalse();
        verify(repository, times(1)).findByEmail(email);
    }
}