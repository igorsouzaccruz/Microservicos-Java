package com.microservico.account.services;

import com.microservico.account.models.Account;
import com.microservico.account.repositories.AccountRepository;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class AccountService {

    private final AccountRepository repository;

    public AccountService(AccountRepository repository) {
        this.repository = repository;
    }

    public Account save(Account account) {
        return repository.save(account);
    }

    public Optional<Account> findByEmail(String email) {
        return repository.findByEmail(email);
    }

    public boolean existsByEmail(String email) {
        return repository.findByEmail(email).isPresent();
    }
}
