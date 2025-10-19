package com.microservico.account.models.mapper;

import com.microservico.account.models.Account;
import com.microservico.account.models.dto.RegisterDTO;

import java.util.Objects;

public class AccountMapper {
    public Account toEntity(RegisterDTO dto, String encodedPassword) {
        if (Objects.isNull(dto)) {
            return null;
        }

        Account account = new Account();
        account.setEmail(dto.email());
        account.setPassword(encodedPassword);
        account.setAddress(dto.address());
        return account;
    }
}
