package com.microservico.account.models.dto;

import java.io.Serializable;

public record TokenDTO(String access_token) implements Serializable {
}