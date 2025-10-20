package com.microservico.account.models.mapper;

import com.microservico.account.models.Account;
import com.microservico.account.models.dto.RegisterDTO;
import org.springframework.stereotype.Component; // <-- MELHORIA: Torna um Bean do Spring

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