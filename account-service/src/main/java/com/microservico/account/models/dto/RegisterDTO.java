package com.microservico.account.models.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record RegisterDTO(
        @Email @NotBlank String email,
        @NotBlank @Size(min = 6, max = 50) String password,
        @NotBlank @Size(max = 300) String address,
        boolean admin
) {
}