package com.microservico.account.services;

import com.microservico.account.exceptions.EmailAlreadyExistsException;
import com.microservico.account.exceptions.InvalidCredentialsException;
import com.microservico.account.models.Account;
import com.microservico.account.models.UserRole;
import com.microservico.account.models.dto.LoginDTO;
import com.microservico.account.models.dto.RegisterDTO;
import com.microservico.account.models.enums.Role;
import com.microservico.account.models.mapper.AccountMapper;
import com.microservico.account.repositories.UserRoleRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    // --- Mocks (Dependências Falsas) ---
    @Mock
    private AccountService accountService;
    @Mock
    private UserRoleRepository roleRepository;
    @Mock
    private JwtService jwtService;
    @Mock
    private BCryptPasswordEncoder passwordEncoder;
    @Mock
    private AccountMapper accountMapper;

    // --- Classe sob Teste ---
    // O Mockito tentará injetar todos os @Mocks acima no construtor do AuthService
    @InjectMocks
    private AuthService authService;

    // --- Captor de Argumento ---
    // Usado para "capturar" um objeto que é criado DENTRO do método testado
    @Captor
    private ArgumentCaptor<UserRole> userRoleCaptor;

    @Captor
    private ArgumentCaptor<Account> accountCaptor;

    // --- Dados de Teste ---
    private RegisterDTO userRegisterDTO;
    private RegisterDTO adminRegisterDTO;
    private LoginDTO loginDTO;
    private Account mappedAccount;
    private Account savedAccount;
    private UserRole userRole;

    @BeforeEach
    void setUp() {
        // Dados para registro de usuário comum
        userRegisterDTO = new RegisterDTO("user@test.com", "pass123", "Rua A", false);

        // Dados para registro de admin
        adminRegisterDTO = new RegisterDTO("admin@test.com", "pass123", "Rua B", true);

        // Dados para login
        loginDTO = new LoginDTO("user@test.com", "pass123");

        // Conta como ela vem do Mapper (sem ID, sem senha)
        mappedAccount = new Account("user@test.com", null, "Rua A");

        // Conta como ela é "salva" (com ID)
        savedAccount = new Account(1L, "user@test.com", "encodedPassword", "Rua A");

        // Role associada
        userRole = new UserRole(1L, Role.ROLE_USER);
    }

    // --- Testes do Método register() ---

    @Test
    @DisplayName("Deve registrar um novo usuário com sucesso (ROLE_USER)")
    void register_withValidData_asUser_shouldSucceed() {
        // Arrange (Organizar)

        // 1. Quando accountService.existsByEmail for chamado, retorne false (email não existe)
        when(accountService.existsByEmail(userRegisterDTO.email())).thenReturn(false);

        // 2. Quando o mapper for chamado, retorne a conta mapeada
        when(accountMapper.toEntity(userRegisterDTO)).thenReturn(mappedAccount);

        // 3. Quando o passwordEncoder for chamado, retorne uma senha "fake"
        when(passwordEncoder.encode(userRegisterDTO.password())).thenReturn("encodedPassword");

        // 4. Quando accountService.save for chamado, retorne a conta com ID
        when(accountService.save(any(Account.class))).thenReturn(savedAccount);

        // Act (Agir)
        authService.register(userRegisterDTO);

        // Assert (Verificar)

        // Verifica se a senha foi definida na conta ANTES de salvar
        verify(accountService).save(accountCaptor.capture());
        assertThat(accountCaptor.getValue().getPassword()).isEqualTo("encodedPassword");

        // Verifica se o roleRepository.save foi chamado
        verify(roleRepository, times(1)).save(userRoleCaptor.capture());

        // Verifica se o objeto UserRole capturado tem os dados corretos
        UserRole capturedRole = userRoleCaptor.getValue();
        assertThat(capturedRole.getAccountId()).isEqualTo(savedAccount.getId());
        assertThat(capturedRole.getRole()).isEqualTo(Role.ROLE_USER); // Teste da role USER
    }

    @Test
    @DisplayName("Deve registrar um novo admin com sucesso (ROLE_ADMIN)")
    void register_withValidData_asAdmin_shouldSucceed() {
        // Arrange
        when(accountService.existsByEmail(adminRegisterDTO.email())).thenReturn(false);
        when(accountMapper.toEntity(adminRegisterDTO)).thenReturn(new Account()); // Mapper retorna conta
        when(passwordEncoder.encode(anyString())).thenReturn("encodedPassword");
        when(accountService.save(any(Account.class))).thenReturn(savedAccount); // ID 1L

        // Act
        authService.register(adminRegisterDTO);

        // Assert
        // Apenas verifica a parte que muda: a Role
        verify(roleRepository, times(1)).save(userRoleCaptor.capture());
        UserRole capturedRole = userRoleCaptor.getValue();

        assertThat(capturedRole.getAccountId()).isEqualTo(1L);
        assertThat(capturedRole.getRole()).isEqualTo(Role.ROLE_ADMIN); // Teste da role ADMIN
    }

    @Test
    @DisplayName("Deve lançar EmailAlreadyExistsException se o e-mail já existir")
    void register_whenEmailAlreadyExists_shouldThrowException() {
        // Arrange
        // 1. Configura o mock para dizer que o e-mail JÁ EXISTE
        when(accountService.existsByEmail(userRegisterDTO.email())).thenReturn(true);

        // Act & Assert
        // Verificamos se a exceção correta é lançada
        assertThatThrownBy(() -> authService.register(userRegisterDTO))
                .isInstanceOf(EmailAlreadyExistsException.class)
                .hasMessage("E-mail já cadastrado");

        // Verifica se os outros métodos NUNCA foram chamados
        verify(accountMapper, never()).toEntity(any());
        verify(passwordEncoder, never()).encode(anyString());
        verify(accountService, never()).save(any());
        verify(roleRepository, never()).save(any());
    }

    // --- Testes do Método login() ---

    @Test
    @DisplayName("Deve logar com sucesso e retornar um token JWT")
    void login_withValidCredentials_shouldReturnToken() {
        // Arrange
        String expectedToken = "fake.jwt.token";

        // 1. Encontra a conta pelo e-mail
        when(accountService.findByEmail(loginDTO.email())).thenReturn(Optional.of(savedAccount));

        // 2. Verifica se a senha bate (retorna true)
        when(passwordEncoder.matches(loginDTO.password(), savedAccount.getPassword())).thenReturn(true);

        // 3. Encontra a role da conta
        when(roleRepository.findByAccountId(savedAccount.getId())).thenReturn(Optional.of(userRole));

        // 4. Gera o token
        when(jwtService.generateToken(1L, "user@test.com", "ROLE_USER")).thenReturn(expectedToken);

        // Act
        String token = authService.login(loginDTO);

        // Assert
        assertThat(token).isEqualTo(expectedToken);

        // Verifica a ordem das chamadas
        verify(accountService, times(1)).findByEmail(loginDTO.email());
        verify(passwordEncoder, times(1)).matches("pass123", "encodedPassword");
        verify(roleRepository, times(1)).findByAccountId(1L);
        verify(jwtService, times(1)).generateToken(1L, "user@test.com", "ROLE_USER");
    }

    @Test
    @DisplayName("Deve lançar InvalidCredentialsException se o e-mail não for encontrado")
    void login_whenUserNotFound_shouldThrowException() {
        // Arrange
        // 1. E-mail não encontrado
        when(accountService.findByEmail(loginDTO.email())).thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> authService.login(loginDTO))
                .isInstanceOf(InvalidCredentialsException.class)
                .hasMessage("Credenciais inválidas");

        // Garante que a verificação de senha e a busca de role nunca ocorreram
        verify(passwordEncoder, never()).matches(anyString(), anyString());
        verify(roleRepository, never()).findByAccountId(anyLong());
        verify(jwtService, never()).generateToken(any(), any(), any());
    }

    @Test
    @DisplayName("Deve lançar InvalidCredentialsException se a senha estiver incorreta")
    void login_whenPasswordIsIncorrect_shouldThrowException() {
        // Arrange
        // 1. E-mail encontrado
        when(accountService.findByEmail(loginDTO.email())).thenReturn(Optional.of(savedAccount));

        // 2. Senha NÃO bate
        when(passwordEncoder.matches(loginDTO.password(), savedAccount.getPassword())).thenReturn(false);

        // Act & Assert
        assertThatThrownBy(() -> authService.login(loginDTO))
                .isInstanceOf(InvalidCredentialsException.class)
                .hasMessage("Credenciais inválidas");

        // Garante que a busca de role nunca ocorreu
        verify(roleRepository, never()).findByAccountId(anyLong());
        verify(jwtService, never()).generateToken(any(), any(), any());
    }

    @Test
    @DisplayName("Deve lançar IllegalStateException se a Role não for encontrada (Erro de Integridade)")
    void login_whenRoleIsNotFound_shouldThrowException() {
        // Arrange
        // 1. E-mail encontrado
        when(accountService.findByEmail(loginDTO.email())).thenReturn(Optional.of(savedAccount));

        // 2. Senha BATE
        when(passwordEncoder.matches(loginDTO.password(), savedAccount.getPassword())).thenReturn(true);

        // 3. Role NÃO é encontrada (isso é um erro de dados)
        when(roleRepository.findByAccountId(savedAccount.getId())).thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> authService.login(loginDTO))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("Erro de integridade: Usuário " + savedAccount.getId() + " não possui role.");

        // Garante que o token nunca foi gerado
        verify(jwtService, never()).generateToken(any(), any(), any());
    }
}