package com.microservico.account.models.mapper;

import com.microservico.account.models.Account;
import com.microservico.account.models.dto.RegisterDTO;
import org.springframework.stereotype.Component;

import java.util.Objects;

@Component
public class AccountMapper {

    public Account toEntity(RegisterDTO dto) {
        if (Objects.isNull(dto)) {
            return null;
        }

        return new Account(
                dto.email(),
                null,
                dto.address()
        );
    }
}