package com.microservico.account.services;

import com.microservico.account.models.Account;
import com.microservico.account.models.UserRole;
import com.microservico.account.models.enums.Role;
import com.microservico.account.repositories.AccountRepository;
import com.microservico.account.repositories.UserRoleRepository;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.List; // <-- Importação necessária

@Service
public class CustomUserDetailsService implements UserDetailsService {

    private final AccountRepository accountRepository;
    private final UserRoleRepository userRoleRepository;

    public CustomUserDetailsService(
            AccountRepository accountRepository,
            UserRoleRepository userRoleRepository) {
        this.accountRepository = accountRepository;
        this.userRoleRepository = userRoleRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {


        Account account = accountRepository.findByEmail(username)
                .orElseThrow(() -> new UsernameNotFoundException(
                        "Usuário não encontrado com o e-mail: " + username
                ));


        UserRole userRole = userRoleRepository.findByAccountId(account.getId())
                .orElseThrow(() -> new UsernameNotFoundException(
                        "Erro de integridade: Usuário " + username + " não possui role."
                ));

        List<GrantedAuthority> authorities = List.of(
                new SimpleGrantedAuthority(userRole.getRole().name())
        );

        return new User(account.getEmail(), account.getPassword(), authorities);
    }
}