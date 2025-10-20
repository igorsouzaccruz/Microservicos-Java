package com.microservico.account.services;

import com.microservico.account.models.Account;
import com.microservico.account.models.UserRole;
import com.microservico.account.models.enums.Role;
import com.microservico.account.repositories.AccountRepository;
import com.microservico.account.repositories.UserRoleRepository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.util.Optional;


import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CustomUserDetailsServiceTest {

    // Mock das dependências (repositórios)
    @Mock
    private AccountRepository accountRepository;

    @Mock
    private UserRoleRepository userRoleRepository;

    // Injeta os mocks na classe que estamos testando
    @InjectMocks
    private CustomUserDetailsService userDetailsService;

    // --- Dados de Teste ---
    private Account testAccount;
    private UserRole testUserRole;
    private String userEmail;

    @BeforeEach
    void setUp() {
        // Configura dados de teste padrão que serão usados em múltiplos testes
        userEmail = "user@test.com";

        testAccount = new Account(1L, userEmail, "senhaCriptografada123", "Rua Teste");
        testUserRole = new UserRole(testAccount.getId(), Role.ROLE_USER);
    }

    @Test
    @DisplayName("Deve carregar UserDetails com sucesso quando usuário e role existem")
    void loadUserByUsername_whenUserAndRoleExist_shouldReturnUserDetails() {
        // Arrange (Organizar)

        // 1. Diz ao mock para retornar a conta quando 'findByEmail' for chamado
        when(accountRepository.findByEmail(userEmail))
                .thenReturn(Optional.of(testAccount));

        // 2. Diz ao mock para retornar a role quando 'findByAccountId' for chamado
        when(userRoleRepository.findByAccountId(testAccount.getId()))
                .thenReturn(Optional.of(testUserRole));

        // Act (Agir)
        // 3. Executa o método a ser testado
        UserDetails userDetails = userDetailsService.loadUserByUsername(userEmail);

        // Assert (Verificar)
        // 4. Verifica se os dados no UserDetails estão corretos
        assertThat(userDetails).isNotNull();
        assertThat(userDetails.getUsername()).isEqualTo(testAccount.getEmail());
        assertThat(userDetails.getPassword()).isEqualTo(testAccount.getPassword());

        // 5. Verifica se a role (Authority) foi definida corretamente
        assertThat(userDetails.getAuthorities()).hasSize(1);

        // 6. (Opcional) Verifica se os mocks foram chamados
        verify(accountRepository, times(1)).findByEmail(userEmail);
        verify(userRoleRepository, times(1)).findByAccountId(testAccount.getId());
    }

    @Test
    @DisplayName("Deve lançar UsernameNotFoundException quando o usuário (email) não for encontrado")
    void loadUserByUsername_whenUserNotFound_shouldThrowUsernameNotFoundException() {
        // Arrange
        String emailInexistente = "notfound@test.com";
        // 1. Diz ao mock para retornar vazio quando 'findByEmail' for chamado
        when(accountRepository.findByEmail(emailInexistente))
                .thenReturn(Optional.empty());

        // Act & Assert
        // 2. Verifica se a exceção correta é lançada com a mensagem correta
        assertThatThrownBy(() -> userDetailsService.loadUserByUsername(emailInexistente))
                .isInstanceOf(UsernameNotFoundException.class)
                .hasMessage("Usuário não encontrado com o e-mail: " + emailInexistente);

        // 3. Verifica se o 'userRoleRepository' NUNCA foi chamado
        verify(userRoleRepository, never()).findByAccountId(anyLong());
    }

    @Test
    @DisplayName("Deve lançar UsernameNotFoundException quando a role não for encontrada (Erro de Integridade)")
    void loadUserByUsername_whenRoleNotFound_shouldThrowUsernameNotFoundException() {
        // Arrange
        // 1. Diz ao mock para ENCONTRAR a conta
        when(accountRepository.findByEmail(userEmail))
                .thenReturn(Optional.of(testAccount));

        // 2. Diz ao mock para NÃO encontrar a role
        when(userRoleRepository.findByAccountId(testAccount.getId()))
                .thenReturn(Optional.empty());

        // Act & Assert
        // 3. Verifica se a exceção correta (de integridade) é lançada
        assertThatThrownBy(() -> userDetailsService.loadUserByUsername(userEmail))
                .isInstanceOf(UsernameNotFoundException.class)
                .hasMessage("Erro de integridade: Usuário " + userEmail + " não possui role.");

        // 4. Verifica se ambos os repositórios foram chamados
        verify(accountRepository, times(1)).findByEmail(userEmail);
        verify(userRoleRepository, times(1)).findByAccountId(testAccount.getId());
    }
}