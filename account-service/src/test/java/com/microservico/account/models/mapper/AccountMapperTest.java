package com.microservico.account.models.mapper;

import com.microservico.account.models.Account;
import com.microservico.account.models.dto.RegisterDTO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;


class AccountMapperTest {
    private AccountMapper accountMapper;

    @BeforeEach
    void setUp() {
        // Instancia o mapper antes de cada teste
        accountMapper = new AccountMapper();
    }

    @Test
    @DisplayName("Deve mapear RegisterDTO para a entidade Account corretamente")
    void shouldMapRegisterDtoToAccountEntity() {
        // Arrange
        var registerDto = new RegisterDTO(
                "test@example.com",
                "senha123",
                "Rua dos Testes, 123",
                false
        );

        // Act
        Account result = accountMapper.toEntity(registerDto);

        // Assert
        assertNotNull(result, "O resultado não deve ser nulo");
        assertEquals(registerDto.email(), result.getEmail(), "O email deve ser o mesmo do DTO");
        assertEquals(registerDto.address(), result.getAddress(), "O endereço deve ser o mesmo do DTO");
        assertNull(result.getPassword(), "A senha na entidade deve ser nula após o mapeamento");
    }

    @Test
    @DisplayName("Deve retornar nulo quando o RegisterDTO de entrada for nulo")
    void shouldReturnNullWhenDtoIsNull() {
        // Arrange
        RegisterDTO nullDto = null;

        // Act
        Account result = accountMapper.toEntity(nullDto);

        // Assert
        assertNull(result, "O resultado deve ser nulo quando o DTO de entrada é nulo");
    }
}