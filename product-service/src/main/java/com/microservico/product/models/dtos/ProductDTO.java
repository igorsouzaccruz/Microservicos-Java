package com.microservico.product.models.dtos;

import com.microservico.product.models.enums.CategoryEnum;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import org.hibernate.validator.constraints.Length;

import java.math.BigDecimal;

public record ProductDTO(Long id,
                         @NotBlank
                         @NotNull
                         @Length(max = 150)
                         String description,
                         @NotNull
                         CategoryEnum category,
                         @Positive
                         @NotNull
                         BigDecimal price) {
}
