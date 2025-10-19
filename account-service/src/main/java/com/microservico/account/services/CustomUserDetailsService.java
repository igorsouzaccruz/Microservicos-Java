package com.microservico.account.services;

import com.microservico.account.models.Account;
import com.microservico.account.models.UserRole;
import com.microservico.account.models.enums.Role;
import com.microservico.account.repositories.AccountRepository;
import com.microservico.account.repositories.UserRoleRepository;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CustomUserDetailsService implements UserDetailsService {

    private final AccountRepository accountRepository;
    private final UserRoleRepository userRoleRepository;

    public CustomUserDetailsService(AccountRepository accountRepository,
                                    UserRoleRepository userRoleRepository) {
        this.accountRepository = accountRepository;
        this.userRoleRepository = userRoleRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        Account acc = accountRepository.findByEmail(username)
                .orElseThrow(() -> new UsernameNotFoundException("Usuário não encontrado"));

        UserRole role = userRoleRepository.findByAccountId(acc.getId())
                .orElse(new UserRole(acc.getId(), Role.ROLE_USER));

        return new User(acc.getEmail(), acc.getPassword(),
                List.of(new SimpleGrantedAuthority(role.getRole().name())));
    }
}