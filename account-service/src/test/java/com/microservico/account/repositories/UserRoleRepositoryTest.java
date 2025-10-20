package com.microservico.account.repositories;

import com.microservico.account.models.UserRole;
import com.microservico.account.models.enums.Role;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
public class UserRoleRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private UserRoleRepository userRoleRepository;

    @Test
    @DisplayName("Deve encontrar UserRole quando o accountId existir")
    void whenFindByAccountId_thenReturnUserRole() {
        // Arrange
        Long accountIdParaTestar = 123L;
        UserRole userRole = new UserRole(accountIdParaTestar, Role.ROLE_USER);
        entityManager.persistAndFlush(userRole); // Salva no banco de teste

        // Act
        Optional<UserRole> found = userRoleRepository.findByAccountId(accountIdParaTestar);

        // Assert
        assertThat(found).isPresent();
        assertThat(found.get().getAccountId()).isEqualTo(accountIdParaTestar);
        assertThat(found.get().getRole()).isEqualTo(Role.ROLE_USER);
    }

    @Test
    @DisplayName("Deve retornar Optional vazio quando o accountId não existir")
    void whenFindByAccountId_withNonExistentId_thenReturnEmpty() {
        // Arrange
        Long accountIdExistente = 1L;
        Long accountIdInexistente = 999L;

        UserRole userRole = new UserRole(accountIdExistente, Role.ROLE_ADMIN);
        entityManager.persistAndFlush(userRole);

        // Act
        Optional<UserRole> found = userRoleRepository.findByAccountId(accountIdInexistente);

        // Assert
        assertThat(found).isNotPresent();
    }
}