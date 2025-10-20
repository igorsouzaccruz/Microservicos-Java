package com.microservico.account.services;

import com.microservico.account.exceptions.EmailAlreadyExistsException; // <-- MELHORIA
import com.microservico.account.exceptions.InvalidCredentialsException; // <-- MELHORIA
import com.microservico.account.models.Account;
import com.microservico.account.models.UserRole;
import com.microservico.account.models.dto.LoginDTO;
import com.microservico.account.models.dto.RegisterDTO;
import com.microservico.account.models.enums.Role;
import com.microservico.account.models.mapper.AccountMapper;
import com.microservico.account.repositories.UserRoleRepository;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional; // <-- MELHORIA

@Service
public class AuthService {
    private final AccountService accountService;
    private final UserRoleRepository roleRepository;
    private final JwtService jwtService;
    private final BCryptPasswordEncoder passwordEncoder;
    private final AccountMapper accountMapper; // <-- MELHORIA

    public AuthService(AccountService accountService,
                       UserRoleRepository roleRepository,
                       JwtService jwtService,
                       BCryptPasswordEncoder passwordEncoder,
                       AccountMapper accountMapper) { // <-- MELHORIA
        this.accountService = accountService;
        this.roleRepository = roleRepository;
        this.jwtService = jwtService;
        this.passwordEncoder = passwordEncoder;
        this.accountMapper = accountMapper;
    }

    @Transactional
    public void register(final RegisterDTO dto) {

        if (accountService.existsByEmail(dto.email())) {
            throw new EmailAlreadyExistsException("E-mail já cadastrado");
        }

        Account acc = accountMapper.toEntity(dto);

        acc.setPassword(passwordEncoder.encode(dto.password()));

        Account saved = accountService.save(acc);

        Role role = dto.admin() ? Role.ROLE_ADMIN : Role.ROLE_USER;
        roleRepository.save(new UserRole(saved.getId(), role));
    }

    @Transactional(readOnly = true)
    public String login(final LoginDTO dto) {

        Account acc = accountService.findByEmail(dto.email())
                .orElseThrow(() -> new InvalidCredentialsException("Credenciais inválidas"));

        if (!passwordEncoder.matches(dto.password(), acc.getPassword())) {
            throw new InvalidCredentialsException("Credenciais inválidas");
        }

        Role role = roleRepository.findByAccountId(acc.getId())
                .map(UserRole::getRole)
                .orElseThrow(() -> new IllegalStateException( // <-- MELHORIA
                        "Erro de integridade: Usuário " + acc.getId() + " não possui role."
                ));

        return jwtService.generateToken(acc.getId(), acc.getEmail(), role.name());
    }
}