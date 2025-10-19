package com.microservico.account.services;

import com.microservico.account.models.Account;
import com.microservico.account.models.UserRole;
import com.microservico.account.models.dto.LoginDTO;
import com.microservico.account.models.dto.RegisterDTO;
import com.microservico.account.models.enums.Role;
import com.microservico.account.models.mapper.AccountMapper;
import com.microservico.account.repositories.UserRoleRepository;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AuthService {
//    private final AccountService accountService;
//    private final JwtService jwtService;
//    private final AccountMapper accountMapper;
//    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
//
//    public AuthService(AccountService accountService, JwtService jwtService, AccountMapper accountMapper) {
//        this.accountService = accountService;
//        this.jwtService = jwtService;
//        this.accountMapper = accountMapper;
//    }
//
//    public void register(RegisterDTO dto) {
//        if (accountService.existsByEmail(dto.email())) {
//            throw new RuntimeException("E-mail já cadastrado");
//        }
//
//        String encodedPassword = passwordEncoder.encode(dto.password());
//        Account account = accountMapper.toEntity(dto, encodedPassword);
//        accountService.save(account);
//    }
//
//    public String login(LoginDTO dto) {
//        Account account = accountService.findByEmail(dto.email())
//                .orElseThrow(() -> new RuntimeException("Credenciais inválidas"));
//
//        if (!passwordEncoder.matches(dto.password(), account.getPassword())) {
//            throw new RuntimeException("Credenciais inválidas");
//        }
//
//        return jwtService.generateToken(account.getId(), account.getEmail());
//    }

    private final AccountService accountService;
    private final UserRoleRepository roleRepository;
    private final JwtService jwtService;
    private final BCryptPasswordEncoder passwordEncoder;

    public AuthService(AccountService accountService,
                       UserRoleRepository roleRepository,
                       JwtService jwtService,
                       BCryptPasswordEncoder passwordEncoder) {
        this.accountService = accountService;
        this.roleRepository = roleRepository;
        this.jwtService = jwtService;
        this.passwordEncoder = passwordEncoder;
    }

    public void register(RegisterDTO dto) {
        accountService.findByEmail(dto.email()).ifPresent(a -> {
            throw new RuntimeException("E-mail já cadastrado");
        });

        Account acc = new Account(dto.email(),
                passwordEncoder.encode(dto.password()),
                dto.address());

        Account saved = accountService.save(acc);

        Role role = dto.admin() ? Role.ROLE_ADMIN : Role.ROLE_USER;
        roleRepository.save(new UserRole(saved.getId(), role));
    }

    public String login(LoginDTO dto) {
        Account acc = accountService.findByEmail(dto.email())
                .orElseThrow(() -> new RuntimeException("Credenciais inválidas"));

        if (!passwordEncoder.matches(dto.password(), acc.getPassword())) {
            throw new RuntimeException("Credenciais inválidas");
        }

        Role role = roleRepository.findByAccountId(acc.getId())
                .map(UserRole::getRole).orElse(Role.ROLE_USER);

        return jwtService.generateToken(acc.getId(), acc.getEmail(), role.name());
    }
}
